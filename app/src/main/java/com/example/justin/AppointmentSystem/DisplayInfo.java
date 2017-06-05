package com.example.justin.AppointmentSystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Justin on 3/18/2015.
 */
public class DisplayInfo extends FragmentActivity{
    public static final String PUBLIC_STATIC_STRING_IDENTIFIER = "string";
    public static final int STATIC_INTEGER_VALUE = 1;


    TextView infoprompt;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_info);
       infoprompt = (TextView) findViewById(R.id.infoprompt);
       Bundle extras = getIntent().getExtras();
       if (extras != null) {
           try {
               JSONObject json = new JSONObject(extras.getString("Json"));
               infoprompt.setText("Your next appointment is scheduled\nfrom " + json.getString("nicetime")+" to "+json.getString("nicetimeend") + " on\n" + json.getString("nicedate") + ".");
           } catch (JSONException e) {
               e.printStackTrace();
           }
       }

       AlertDialog.Builder builder = new AlertDialog.Builder(this);

       builder.setTitle("Cancel Appointment");
       builder.setMessage("Are you sure you want to cancel your appointment?");

       builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

           public void onClick(DialogInterface dialog, int which) {
               Bundle extras = getIntent().getExtras();
               if (extras != null) {
                   try {
                       JSONObject json = new JSONObject(extras.getString("Json"));
                       new HttpAsyncTask().execute("http://cptgschedule.ddns.net/jsondelete.cfm?event="+json.getString("event"));
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
               }
               dialog.dismiss();
           }

       });

       builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

           @Override
           public void onClick(DialogInterface dialog, int which) {
               // Do nothing
               dialog.dismiss();
           }
       });

       final AlertDialog alert = builder.create();
       findViewById(R.id.cancelbutton).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               {
                   alert.show();
               }
           }
       });


       findViewById(R.id.reschedulebutton).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               {
                   Bundle extras = getIntent().getExtras();
                   if (extras != null) {
                       Intent i = new Intent(getBaseContext(), RescheduleReservation.class);
                       i.putExtra("Json", extras.getString("Json"));
                       startActivityForResult(i, STATIC_INTEGER_VALUE);
                   }

               }
           }
       });
    }

    public static String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
                Log.d("GET", "Converted Successfully");
            } else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (STATIC_INTEGER_VALUE) : {
                if (resultCode == RESULT_OK) {
                    String close = data.getStringExtra(PUBLIC_STATIC_STRING_IDENTIFIER);
                    if(close.equals("true"))
                        finish();
                    else if(close.equals("supertrue")){
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, "true");
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }else if(close.equals("rerun")) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, "rerun");
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                }
                break;
            }
        }
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }


        inputStream.close();
        return result;

    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            // Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            try {
                JSONObject json = new JSONObject(result);
                if(json.getBoolean("success"))
                   Log.d("Success","Delete successful for event "+json.getString("event")+'.');
                Intent resultIntent = new Intent();
                resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, "true");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
