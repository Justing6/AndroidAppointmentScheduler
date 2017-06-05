package com.example.justin.AppointmentSystem;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Justin on 3/17/2015.
 */
public class LookupReservation extends FragmentActivity {
    public static final String PUBLIC_STATIC_STRING_IDENTIFIER = "string";
    public static final int STATIC_INTEGER_VALUE = 1;
    EditText fnamebox;
    EditText lnamebox;
    EditText emailbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookup_reservation);

        emailbox = (EditText) findViewById(R.id.emailbox);
        fnamebox = (EditText) findViewById(R.id.fnamebox);
        lnamebox = (EditText) findViewById(R.id.lnamebox);
        fnamebox.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(getBaseContext().INPUT_METHOD_SERVICE);
        imm.showSoftInput(fnamebox, InputMethodManager.SHOW_IMPLICIT);
        emailbox.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            if (inputValidate()) {
                                new HttpAsyncTask().execute(buildAdd());
                            }
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputValidate()) {
                    new HttpAsyncTask().execute(buildAdd());
                }
            }
        });

    }
    public String buildAdd() {
        String fname = fnamebox.getText().toString().trim();
        fname = fname.replaceAll(" ", "*");
        String lname = lnamebox.getText().toString().trim();
        lname = lname.replaceAll(" ", "*");
        String email = emailbox.getText().toString().trim();
        email=email.replaceAll("#", "");
        String url = "http://cptgschedule.ddns.net/jsonlookup.cfm?fname="+fname+"&lname="
                +lname+"&email="+email;
        Log.d("URL", url);
        return url;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (STATIC_INTEGER_VALUE) : {
                if (resultCode == FindSlot.RESULT_OK) {
                    String close = data.getStringExtra(PUBLIC_STATIC_STRING_IDENTIFIER);
                    if(close.equals("true"))
                        finish();
                    else if(close.equals("rerun"))
                        if (inputValidate()) {
                            new HttpAsyncTask().execute(buildAdd());
                        }
                }
                break;
            }
        }
    }
    public boolean inputValidate() {
        boolean error = false;
        if (fnamebox.getText().toString().matches("")
                || lnamebox.getText().toString().matches("")
                || emailbox.getText().toString().matches("")) {
            Toast.makeText(LookupReservation.this, "Please fill in all fields.", Toast.LENGTH_LONG).show();
            error = true;
        } else {
            String namechk = "[A-Za-z ]*";
            String phonechk = "\\(\\d{3}\\) \\d{3}-\\d{4}";
            Pattern name = Pattern.compile(namechk);
            Pattern phone = Pattern.compile(phonechk);
            Matcher f = name.matcher(fnamebox.getText().toString());
            Matcher l = name.matcher(lnamebox.getText().toString());
            if ((!f.matches() || !l.matches()) && !error) {
                Toast.makeText(LookupReservation.this, "Please enter only letters in the name fields.", Toast.LENGTH_LONG).show();
                error = true;
            } else if (!isEmailValid(emailbox.getText().toString()) && !error) {
                Toast.makeText(LookupReservation.this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
                error = true;
            }
        }
        return !error;
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
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
                if (json.getString("function").equals("lookup")) {
                    if(json.getBoolean("found")){
                        Intent i = new Intent(LookupReservation.this, DisplayInfo.class);
                        i.putExtra("Json", json.toString());
                        startActivityForResult(i, STATIC_INTEGER_VALUE);
                    }else
                        Toast.makeText(getBaseContext(), "No future appointments found for the information provided.", Toast.LENGTH_LONG).show();
                }
                else
                    Log.d("failed","Function called was "+json.getString("function"));
                //etResponse.setText(json.toString(1));

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}

