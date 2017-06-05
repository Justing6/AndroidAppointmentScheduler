package com.example.justin.AppointmentSystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.widget.Toast;

/**
 * Created by Justin on 3/17/2015.
 */
public class AddReservation extends FragmentActivity {
    public static final String PUBLIC_STATIC_STRING_IDENTIFIER = "string";
    public static final int STATIC_INTEGER_VALUE = 1;
        EditText phonebox;
        EditText fnamebox;
        EditText lnamebox;
        EditText emailbox;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.add_reservation);

            emailbox = (EditText) findViewById(R.id.emailbox);
            fnamebox = (EditText) findViewById(R.id.fnamebox);
            phonebox = (EditText) findViewById(R.id.phonebox);
            lnamebox = (EditText) findViewById(R.id.lnamebox);
            fnamebox.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(getBaseContext().INPUT_METHOD_SERVICE);
            imm.showSoftInput(fnamebox, InputMethodManager.SHOW_IMPLICIT);
            phonebox.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            emailbox.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_CENTER:
                            case KeyEvent.KEYCODE_ENTER:
                                if (inputValidate()) {
                                    String tempperson[] = new String[]{fnamebox.getText().toString(),lnamebox.getText().toString(),phonebox.getText().toString(),emailbox.getText().toString()};
                                    Intent i = new Intent(AddReservation.this, FindSlot.class);
                                    i.putExtra("Stringarr", tempperson);
                                    startActivityForResult(i, STATIC_INTEGER_VALUE);
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
                        String tempperson[] = new String[]{fnamebox.getText().toString(),lnamebox.getText().toString(),phonebox.getText().toString(),emailbox.getText().toString()};
                        Intent i = new Intent(AddReservation.this, FindSlot.class);
                        i.putExtra("Stringarr", tempperson);
                        startActivityForResult(i, STATIC_INTEGER_VALUE);
                    }
                }
            });
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
                        }
                        break;
                    }
                }
            }
        public boolean inputValidate() {
            boolean error = false;
            if (fnamebox.getText().toString().matches("")
                    || lnamebox.getText().toString().matches("")
                    || phonebox.getText().toString().matches("")
                    || emailbox.getText().toString().matches("")) {
                Toast.makeText(AddReservation.this, "Please fill in all fields.", Toast.LENGTH_LONG).show();
                error = true;
            } else {
                String namechk = "[A-Za-z ]*";
                String phonechk = "\\(\\d{3}\\) \\d{3}-\\d{4}";
                Pattern name = Pattern.compile(namechk);
                Pattern phone = Pattern.compile(phonechk);
                Matcher f = name.matcher(fnamebox.getText().toString());
                Matcher l = name.matcher(lnamebox.getText().toString());
                Matcher p = phone.matcher(phonebox.getText().toString());
                if ((!f.matches() || !l.matches()) && !error) {
                    Toast.makeText(AddReservation.this, "Please enter only letters in the name fields.", Toast.LENGTH_LONG).show();
                    error = true;
                } else if (!p.matches() && !error) {
                    Toast.makeText(AddReservation.this, "Please enter a formatted 10 digit phone number.", Toast.LENGTH_LONG).show();
                    error = true;
                } else if (!isEmailValid(emailbox.getText().toString()) && !error) {
                    Toast.makeText(AddReservation.this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
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
    }

