//package com.google.vr.sdk.samples.hellovr;
//
//
//import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//
//    protected EditText originET, destET;
//    protected TextView fromTV, toTV;
//    protected Button startButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        startButton = (Button) findViewById(R.id.startBtn);
//
//        originET = (EditText) findViewById(R.id.originET);
//        destET = (EditText) findViewById(R.id.destET);
//    }
//
//    @Override
//    public void onClick(View view) {
//        if(view.getId() == startButton.getId()){
//            Intent intent = new Intent(this,HelloVrActivity.class);
//
//            startActivity(intent);
//        }
//    }
//}
