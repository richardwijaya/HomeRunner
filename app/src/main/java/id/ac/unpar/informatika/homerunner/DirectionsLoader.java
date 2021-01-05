package id.ac.unpar.informatika.homerunner;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DirectionsLoader extends AsyncTask<String,Void,Void> {

    protected String jsonText;
    protected Activity activity;

    public DirectionsLoader(Activity activity){
        this.activity = activity;
        jsonText = "";
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream inputStream = conn.getInputStream();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String curLine = bufferedReader.readLine();;

            while(curLine != null){
                jsonText += curLine;
                curLine = bufferedReader.readLine();

            }

            conn.disconnect();

        } catch (Exception ex) {}

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        DirectionsExtractor dirExtractor = new DirectionsExtractor(jsonText);
        Log.d("JSONText",jsonText);

        dirExtractor.extractJSONDir();

        Intent intent = new Intent(activity, HelloVrActivity.class);

        Log.d("Steps from Extractor", dirExtractor.arrSteps.get(0).toString());

        StreetViewLoader streetViewLoader = new StreetViewLoader(activity, intent, dirExtractor.arrSteps);

        streetViewLoader.execute();
    }
}
