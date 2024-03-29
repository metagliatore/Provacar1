package com.example.peppelt.provacar1;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class GoogleTransitActivity extends Activity {

    private String ar;

    private String indlat;
    private String indlon;
    private String partenzalat;
    private String partenzalon;
    private String arrivolat;
    private String arrivolon;
    private String arrival_time;
    //the URL having the json data
    private String JSON_URL = " ";

    //listview object
    ListView listView;

    //the hero list where we will store all the hero objects after parsing json
    List<Dati> datiList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trasporto_alternativo);
        Intent i = getIntent();
        Bundle bb = i.getBundleExtra("bundle");
        ar = bb.getString("ar");
        indlat=bb.getString("indlat");
        indlon=bb.getString("indlon");
        //Toast.makeText(getApplicationContext(), ar + " " + indlat + "  " + indlon, Toast.LENGTH_LONG).show();

        // TRANSIT

        //initializing listview and hero list
        listView = (ListView) findViewById(R.id.listViewTrasportoAlternativo);
        datiList = new ArrayList<>();

        //this method will fetch and parse the data
        loadHeroList();
    }
    private void loadHeroList() {
        // se ar==1 vuol dire che è un viaggio di andata verso l'unisa, else è un viaggio di ritorno quindi da unisa a casa
        if(ar.equals("1")){
            partenzalat=indlat; //cordinate di casa del richiedente passaggio
            partenzalon=indlon;
            arrivolat="40.77372"; //cordinate di unisa
            arrivolon="14.794522";
        }
        else{
            partenzalat="40.77372"; //cordinate di unisa
            partenzalon="14.794522";
            arrivolat=indlat; //cordinate di casa del richiedente passaggio
            arrivolon=indlon;
        }
        //creaiamo la nostra url per transit
        JSON_URL="https://maps.googleapis.com/maps/api/directions/json?origin="+partenzalat+","+partenzalon+"&destination="+arrivolat+","+arrivolon+"&departure_time=now&mode=transit&language=it&key=AIzaSyBdpa0bBULmR-ILjQ8wF_FCJ3OLKRPnQB8";
        //getting the progressbar
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //making the progressbar visible
        progressBar.setVisibility(View.VISIBLE);

        //creating a string request to send request to the url
        StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //hiding the progressbar after completion
                        progressBar.setVisibility(View.INVISIBLE);


                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);

                            //we have the array named hero inside the object
                            //so here we are getting that json array
                            JSONArray routesArray = obj.getJSONArray("routes");

                            //now looping through all the elements of the json array
                            for (int i = 0; i < routesArray.length(); i++) {
                                //getting the json object of the particular index inside the array
                                JSONObject heroObject = routesArray.getJSONObject(i);
                                JSONArray legsArray = heroObject.getJSONArray("legs");
                                for (int j = 0; j < legsArray.length(); j++) {

                                    JSONObject legsObject = legsArray.getJSONObject(j);
                                    JSONArray stepsArray = legsObject.getJSONArray("steps");
                                    // getting arrival time for toast
                                    JSONObject ArrivalTimeObject = legsObject.getJSONObject("arrival_time");
                                    arrival_time = ArrivalTimeObject.getString("text");

                                    for (int k = 0; k < stepsArray.length(); k++) {

                                        JSONObject leg = stepsArray.getJSONObject(k);
                                        JSONObject distanceObject = leg.getJSONObject("distance");
                                        String distance = distanceObject.getString("text");
                                        JSONObject durationObject = leg.getJSONObject("duration");
                                        String duration = durationObject.getString("text");
                                        String html_instructions = leg.getString("html_instructions");
                                        String html=html_instructions.replaceAll("<b>","\t");
                                        String html1=html.replaceAll("</b>","\t");
                                        String html2=html1.replaceAll("<div style=\"font-size:0.9em\">","\t");
                                        String html3=html2.replaceAll("</div>","\t");
                                        //creating a hero object and giving them the values from json object
                                        Dati dati = new Dati(distance,duration,html3);

                                        //adding the hero to herolist
                                        datiList.add(dati);



                                    }
                                }
                            }
                            Toast.makeText(getApplicationContext(), "arrivo previsto per le ore:" +arrival_time, Toast.LENGTH_LONG).show();


                            //creating custom adapter object
                            ListViewAdapter adapter = new ListViewAdapter(datiList, getApplicationContext());

                            //adding the adapter to listview
                            listView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }


}
