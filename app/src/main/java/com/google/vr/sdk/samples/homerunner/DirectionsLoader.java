package com.google.vr.sdk.samples.homerunner;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DirectionsLoader extends AsyncTask<String,Void,Void> {

    protected MainActivity mainActivity;
    protected String filePath, jsonText;

    public DirectionsLoader(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        filePath = "dir_route.json";
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


        } catch (Exception ex) {

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        try {
            File file = new File(mainActivity.getCacheDir(), filePath);
            if(file.exists()){
                file.delete();
                file = new File(mainActivity.getCacheDir(), filePath);
                file.createNewFile();
            }
            FileOutputStream fOS = new FileOutputStream(file);

            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(jsonText);
            fileWriter.close();

            fOS.flush();
            fOS.close();
        }catch(Exception e){
            Log.e("Create File", "Failed");
        }
    }
}
