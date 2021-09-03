package com.example.scanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {
    TextView viewText;
    TextView viewText2;
    TextView viewText3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        viewText=findViewById(R.id.textRes1);
        viewText2=findViewById(R.id.textRes2);
        viewText3=findViewById(R.id.textRes3);
        Bundle bundle=getIntent().getExtras();
        String x=bundle.getString("result1");
        String y=bundle.getString("result2");
        String z=bundle.getString("result3");
        viewText.setText(x);
        viewText2.setText(y);
        viewText3.setText(z);
    }

}