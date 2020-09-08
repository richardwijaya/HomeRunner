package com.google.vr.sdk.samples.hellovr;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class StreetViewLoader extends AsyncTask<String, Void, Void> {

    protected String filePath;
    protected Bitmap streetViewBitmap;
    protected MainActivity mainActivity;
    protected Intent intent;

    public StreetViewLoader(Intent intent, MainActivity mainActivity){
        this.intent = intent;
        this.mainActivity = mainActivity;
        filePath = "whole_streetview.png";
    }

    @Override
    protected Void doInBackground(String... strings) {
        Bitmap[] bitmaps = new Bitmap[strings.length];
        for(int i = 0; i<strings.length; i++) {
            try {
                URL url = new URL(strings[i]);

                InputStream input = url.openStream();
                bitmaps[i] = BitmapFactory.decodeStream(input);
            } catch (Exception ex) {

            }
        }
        int width = bitmaps[0].getWidth();
        int height = bitmaps[0].getHeight();

        width *= bitmaps.length;

        Bitmap allBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(allBitmap);
        int left = 0;
        for (int i = 0; i < bitmaps.length; i++) {
            left = (i == 0 ? 0 : left+bitmaps[i].getWidth());
            canvas.drawBitmap(bitmaps[i], left, 0, null);
        }
        streetViewBitmap = allBitmap;

        try {
            File file = new File(mainActivity.getCacheDir(), filePath);
            if(file.exists()){
                file.delete();
                file = new File(mainActivity.getCacheDir(), filePath);
            }
            Log.d("File Created","Okay");
            FileOutputStream fOS = new FileOutputStream(file);
            streetViewBitmap.compress(Bitmap.CompressFormat.PNG, 0, fOS);
            fOS.flush();
            fOS.close();
        }catch(Exception e){
            Log.e("Create File", "Failed");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i("OnPostExecute", "Okay");
        mainActivity.originET.setText("");
        intent.putExtra("bitmap_texture", filePath);
        mainActivity.startActivity(intent);
    }

}