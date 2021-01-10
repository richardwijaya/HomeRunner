package id.ac.unpar.informatika.homerunner;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class StreetViewLoader extends AsyncTask<Void, Void, Void> {

    protected Bitmap streetViewBitmap;
    protected Activity activity;
    protected Intent intent;
    protected ArrayList<JSONObject> arrSteps;
    protected int imgCount;

    public StreetViewLoader(Activity activity, Intent intent, ArrayList<JSONObject> arrSteps) {
        this.intent = intent;
        this.activity = activity;
        this.arrSteps = arrSteps;
        this.imgCount = 0;
    }

    @Override
    protected Void doInBackground(Void... aVoid) {
        for(int i = 0; i < arrSteps.size(); i++){
            int length = 4;

            boolean isEnd = (i == arrSteps.size() - 1 ? true : false);

            createStreetViewImage(generateStreetViewURL(length, true));

            if(isEnd) {
                createStreetViewImage(generateStreetViewURL(length,false));
            }
            publishProgress();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        imgCount++;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        intent.putExtra("length", imgCount);

        int[] stepsDistance = new int[arrSteps.size()];
        for (int i = 0; i < arrSteps.size(); i++){
            try{
                stepsDistance[i] = arrSteps.get(i).getJSONObject("distance").getInt("value");
            } catch (Exception ex){}
        }

        intent.putExtra("stepDistances", stepsDistance);

        activity.startActivity(intent);
    }

    protected String[] generateStreetViewURL(int svUrlLength, boolean isStart){
        int heading = 0;

        String[] urlArr = new String[svUrlLength];

        String location = (isStart ? getLocation("start_location") : getLocation("end_location"));

        String svTempURL = "https://maps.googleapis.com/maps/api/streetview?size=600x300&location="+
                location +"&key="+ activity.getString(R.string.key) + "&heading=";

        for(int i = 0 ; i < svUrlLength ; i++){
            urlArr[i] = svTempURL + heading;
            Log.d("StreetViewURL", urlArr[i]);
            heading += 90;
        }

        return urlArr;
    }

    public String generateFilePath() {
        return ("streetview" + imgCount + ".png");
    }

    public String getLocation(String startOrEndKey){
        double startLoc = 0.0;
        double endLoc = 0.0;
        try {
            Log.d("Steps", arrSteps.get(0).toString());

            startLoc = arrSteps.get(imgCount).getJSONObject(startOrEndKey).getDouble("lat");
            endLoc = arrSteps.get(imgCount).getJSONObject(startOrEndKey).getDouble("lng");
        } catch (Exception ex){  Log.e("Get Location", "Failed"); }

        return startLoc + "," + endLoc;
    }

    public void createStreetViewImage(String[] urls){
        Bitmap[] bitmaps = new Bitmap[urls.length];
        for (int j = 0; j < urls.length; j++) {
            try {
                URL url = new URL(urls[j]);
                InputStream input = url.openStream();
                bitmaps[j] = BitmapFactory.decodeStream(input);
            } catch (Exception ex) {}
        }

        int width = bitmaps[0].getWidth();
        int height = bitmaps[0].getHeight();

        width *= bitmaps.length;

        Bitmap allBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(allBitmap);
        int left = 0;
        for (int j = 0; j < bitmaps.length; j++) {
            left = (j == 0 ? 0 : left + bitmaps[j].getWidth());
            canvas.drawBitmap(bitmaps[j], left, 0, null);
        }
        streetViewBitmap = allBitmap;

        try {
            File file = new File(activity.getCacheDir(), generateFilePath());
            if (file.exists()) {
                file.delete();
                file = new File(activity.getCacheDir(), generateFilePath());
            }
            FileOutputStream fOS = new FileOutputStream(file);
            streetViewBitmap.compress(Bitmap.CompressFormat.PNG, 0, fOS);
            fOS.flush();
            fOS.close();
        } catch (Exception e) {}
    }

}