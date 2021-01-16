package id.ac.unpar.informatika.homerunner;


import android.app.Activity;
import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends Activity implements View.OnClickListener {

    protected EditText originET, destET;
    protected TextView oriTV, destTV;
    protected Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.startBtn);
        originET = (EditText) findViewById(R.id.originET);
        destET = (EditText) findViewById(R.id.destET);
        oriTV = (TextView) findViewById(R.id.originTV);
        destTV = (TextView) findViewById(R.id.destTV);

        oriTV.setText("Origin");
        destTV.setText("Destination");

        startButton.setText("Start Running");
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == startButton.getId()){

            DirectionsLoader dirLoader = new DirectionsLoader(this);

            String dirURL = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=walking&origin=" + originET.getText().toString() + "&destination=" +
                    destET.getText().toString() + "&key="+ getString(R.string.key);

            Log.d("doInBackground", dirURL);

            originET.setText("");
            destET.setText("");

            dirLoader.execute(dirURL);
        }
    }


}
