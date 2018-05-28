package com.example.peppelt.provacar1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ros on 22/05/2018.
 */

public class ListaBusActivity extends Activity implements RemoteCallListener<String>{

    private String ar;
    private String indlat;
    private String indlon;
    private String codice;
    private String data;
    private String ora;
    private String indirizzo;

    private JSONArray codPercorsi;
    private JSONArray dataPercorsi;
    private JSONArray oraArrivoPercorsi;
    private JSONArray oraPartenzaPercorsi;
    private JSONArray numeroPullman;

    //the URL having the json data
    private String JSON_URL = " ";
    //listview object
    ListView listView;
    //the hero list where we will store all the hero objects after parsing json
    ArrayAdapter<String> aa;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trasporto_alternativo);
        Intent i = getIntent();
        Bundle bb = i.getBundleExtra("bundle");
        ar = bb.getString("ar");
        indlat = bb.getString("indlat");
        indlon = bb.getString("indlon");
        codice = bb.getString("cod");
        indirizzo = bb.getString("indirizzo");
        data=bb.getString("data");
        ora=bb.getString("ora");

        aa= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        //transit è il primo della lista
        aa.add("soluzione google transit");

        // TRANSIT
        //initializing listview and hero list
        listView =(ListView) findViewById(R.id.listViewTrasportoAlternativo);


         //servlet per i pullman nel nostro database
        RequestHttpAsyncTask rh = new RequestHttpAsyncTask(ListaBusActivity.this);
        try {
            JSONObject js = new JSONObject();
            js.put("url", getString(R.string.host) + "servletTrasportoAlternativo");
            js.put("cod", codice.toString());
            js.put("data", data.toString());
            js.put("ora", ora.toString());
            js.put("indirizzo", indirizzo.toString());
            js.put("ar", ar.toString());
            js.put("indlat", indlat.toString());
            js.put("indlon", indlon.toString());

            rh.execute(js);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    public void onRemoteCallListenerComplete(String dati) {
    // qui mi prendo i risultati della servlet (i percorsi dei pullman coicidenti con la nostra partenza destinazione e data )
        try{

            //se non abbiamo trovato niente nel db
            if(dati.equals("niente")){
             Toast.makeText(getApplicationContext(),"non ci sono risultati nel nostro database dei pullman", Toast.LENGTH_LONG).show();
             }

             else {
                JSONObject js = new JSONObject(dati);
                 codPercorsi = js.getJSONArray("codPercorsi");
                 dataPercorsi = js.getJSONArray("dataPercorsi");
                 oraArrivoPercorsi = js.getJSONArray("oraArrivo");
                 oraPartenzaPercorsi = js.getJSONArray("oraPartenza");
                 numeroPullman = js.getJSONArray("numero");

                 for (int i = 0; i < codPercorsi.length(); i++) {
                     aa.add("pullman n°" + numeroPullman.getString(i) + " partenza:" + oraPartenzaPercorsi.getString(i));
                 }
             }

        }
        catch(JSONException e){
            e.printStackTrace();

        }

        //dopo aver preso tutte le soluzioni e messe in aa, setto l'adapter
        listView.setAdapter(aa);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                if (arg2==0){ //qua ci vado a fare google transit
                    Intent i = new Intent(ListaBusActivity.this ,GoogleTransitActivity.class);
                    Bundle b = new Bundle();
                    b.putString("ar",ar);
                    b.putString("indlat",indlat);
                    b.putString("indlon",indlon);
                    i.putExtra("bundle", b);
                    startActivity(i);
                }
// le varie soluzioni della servlet che rimandano ad un'activity con i dettagli del percorso cliccato
            else {


                }

            }

        });
    }
    }
