//package com.google.vr.sdk.samples.hellovr;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.os.AsyncTask;
//import android.util.Log;
//
//import java.io.InputStream;
//import java.net.URL;
//
//class StreetViewLoader extends AsyncTask<String, Bitmap, Void> {
//
//    protected Bitmap streetViewBitmap;
//
//    @Override
//    protected Void doInBackground(String... strings) {
//        Bitmap[] bitmaps = new Bitmap[4];
//        for(int i = 0; i<strings.length; i++) {
//            try {
//                URL url = new URL(strings[i]);
//
//                InputStream input = url.openStream();
//                bitmaps[i] = BitmapFactory.decodeStream(input);
//            } catch (Exception ex) {
//
//            }
//        }
//        int width = bitmaps[0].getWidth(), height = bitmaps[0].getHeight();
//
//        width *= bitmaps.length;
//
//        Bitmap allBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(allBitmap);
//        int left = 0;
//        for (int i = 0; i < bitmaps.length; i++) {
//            left = (i == 0 ? 0 : left+bitmaps[i].getWidth());
//            canvas.drawBitmap(bitmaps[i], left, 0, null);
//        }
//        streetViewBitmap = allBitmap;
//        return null;
//    }
//
//    protected Bitmap getStreetViewBitmap(){
//        return streetViewBitmap;
//    }
//}