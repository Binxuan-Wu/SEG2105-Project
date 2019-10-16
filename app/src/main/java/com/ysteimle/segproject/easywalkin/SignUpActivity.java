package com.ysteimle.segproject.easywalkin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class SignUpActivity extends AppCompatActivity implements OnItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Configuring the spinner for the account type selection
        Spinner accountSpinner = (Spinner) findViewById(R.id.SignUpScreenAccountTypeSpinner);
        accountSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Added to let class implement OnItemSelected Listener
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // Added to let class implement OnItemSelected Listener
    }

    public void onClickPreviousUserMsg (View view) {
        finish(); // Finish activity and return to main screen (which is the Log in Activity)
    }

}
