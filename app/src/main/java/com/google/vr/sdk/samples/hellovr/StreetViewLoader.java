package com.google.vr.sdk.samples.hellovr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

public class StreetViewLoader extends AsyncTask<String, Void, Bitmap>{

    protected HelloVrActivity helloVrActivity;

    public StreetViewLoader(HelloVrActivity hVA) {
        helloVrActivity = hVA;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        int angle = 0;
        int counter = 0;
        Bitmap[] arrBitmap = new Bitmap[4];
        try{
            while(angle <= 360){
                String temp = strings[0] + "&heading=" + angle;
                URL url = new URL(strings[0]);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);

                InputStream input = conn.getInputStream();

                conn.connect();

                arrBitmap[counter] = BitmapFactory.decodeStream(input);

                angle += 90;
                counter++;
            }
        }catch (Exception ex){
        }

        Bitmap result = Bitmap.createBitmap(arrBitmap[0].getWidth() * 2, arrBitmap[0].getHeight() * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        for (int i = 0; i < arrBitmap.length; i++) {
            canvas.drawBitmap(arrBitmap[i], arrBitmap[i].getWidth() * (i % 2), arrBitmap[i].getHeight() * (i / 2), paint);
        }
        
        return result;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
//        try{
//            this.helloVrActivity.roomTex = new Texture(this.helloVrActivity,bitmap);
//        }catch(IOException ex){
//
//        }

    }
}
