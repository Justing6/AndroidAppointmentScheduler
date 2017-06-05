package com.example.justin.AppointmentSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.os.Handler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

public class FindSlot extends FragmentActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final String PUBLIC_STATIC_STRING_IDENTIFIER = "string";


    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    public String pickedtime;
    public String pickeddate;
    public String returnedslot;
    public static final String FINDID = "http://cptgschedule.ddns.net/jsonfinddate.cfm?date=";
    public static final SimpleDateFormat dateparse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final SimpleDateFormat niceformat = new SimpleDateFormat("h:mm a' on 'EEEE, MMMM d, yyyy");
    TextView FindSlotField;
    TextView findIDDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findid);
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),false);
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);

        FindSlotField = (TextView) findViewById(R.id.FindSlotField);
        findIDDate = (TextView) findViewById(R.id.findIDDate);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
        pickeddate = df.format(c.getTime());
        pickedtime = tf.format(c.getTime());
        Log.d("Debug", FINDID+pickeddate+'T'+pickedtime);
        Log.d("Debug", "Currently selected: "+niceformat.format(c.getTime()));

        findIDDate.setText("Currently searching for times near:\n"+niceformat.format(c.getTime())+'.');

        new HttpAsyncTask().execute(FINDID+pickeddate+'T'+pickedtime);



        findViewById(R.id.dateButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                datePickerDialog.setVibrate(false);
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

        findViewById(R.id.confirmbutton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new HttpAsyncTask().execute(buildAdd());
            }
        });
        findViewById(R.id.submitButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                FindSlotField.setText("Searching for an open time...");
                String searchurl = FINDID+pickeddate+'T'+pickedtime;
                Log.d("Debug", searchurl);
                // FindSlotField.setText(searchurl);
                //Toast.makeText(MainActivity.this, searchurl, Toast.LENGTH_LONG).show();
                new HttpAsyncTask().execute(searchurl);
            }
        });

        findViewById(R.id.timeButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.setVibrate(false);
                timePickerDialog.setCloseOnSingleTapMinute(false);
                timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
            }
        });
    }
    public String buildAdd() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String person[] = extras.getStringArray("Stringarr");
            String fname = person[0].trim();
            fname=fname.replaceAll(" ", "*");
            String lname = person[1].trim();
            lname = lname.replaceAll(" ","*");
            String phone = person[2].trim();
            phone=phone.replaceAll(" ", "*");
            String email = person[3].trim();
            email=email.replaceAll("#", "");
            String url = "http://cptgschedule.ddns.net/jsonadd.cfm?fname="+fname+"&lname="
                +lname+"&phone="+phone+"&email="+email+"&date="+returnedslot;
            Log.d("URL", url);
            return url;
        }else
            return null;
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

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        pickeddate = year + "-" + String.format("%02d", (month+1)) + "-" + String.format("%02d", day);
        try {
            findIDDate.setText("Currently searching for times near:\n"+niceformat.format(dateparse.parse(pickeddate + 'T' + pickedtime))+'.');
            //Toast.makeText(getBaseContext(), pickeddate + 'T' + pickedtime, Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        pickedtime = String.format("%02d", hourOfDay)+":"+String.format("%02d", minute) + ":00";
        try {
            findIDDate.setText("Currently searching for times near:\n"+niceformat.format(dateparse.parse(pickeddate + 'T' + pickedtime))+'.');
           // Toast.makeText(getBaseContext(), pickeddate + 'T' + pickedtime, Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

                //String str = json.getString("date");
                if (json.getString("function").equals("findID")){
                    String output = "The next available appointment\nis at " + json.getString("nicetime") + " on\n" + json.getString("nicedate") + ".";
                    Log.d("Debug", output);
                    returnedslot = json.getString("date");
                    FindSlotField.setText(output);
                }else if(json.getString("function").equals("add")){
                    if(json.getBoolean("success")){
                        Toast.makeText(FindSlot.this, "Your appointment has been confirmed.", Toast.LENGTH_LONG).show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, "true");
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                        }, 1000);

                    }
                    else if(json.getBoolean("same")){
                        Toast.makeText(FindSlot.this, "You already have an appointment at this time.", Toast.LENGTH_LONG).show();
                    }else if(!json.getBoolean("valid")){
                        Toast.makeText(FindSlot.this, "Please choose a date under "+json.getString("maxdate")+" days in the future.", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(FindSlot.this, "We're sorry, but this time has filled. The next open time has been displayed.", Toast.LENGTH_LONG).show();
                        new HttpAsyncTask().execute(FINDID + pickeddate + 'T' + pickedtime);
                    }
                }
                else
                    FindSlotField.setText("Function called was" +json.getString("function"));
                //etResponse.setText(json.toString(1));

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


}
