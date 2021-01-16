package id.ac.unpar.informatika.homerunner;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;

public class DirectionsExtractor {

    protected String jsonText;
    protected ArrayList<JSONObject> arrSteps;

    public DirectionsExtractor(String jsonText){
        this.jsonText = jsonText;
        arrSteps = new ArrayList<>();
    }

    public void extractJSONDir(){
        String jsonTemp = "";
        JSONObject jsonDir;

        try {
            BufferedReader bufferedReader = new BufferedReader(new StringReader(jsonText));
            String curLine = bufferedReader.readLine();

            while (curLine != null){
                jsonTemp += curLine;
                curLine = bufferedReader.readLine();
            }

            jsonDir = new JSONObject(jsonTemp);

            JSONArray jsonArrRoute = jsonDir.getJSONArray("routes");

            for(int i = 0; i < jsonArrRoute.length(); i++){
                JSONObject jsonRoute = jsonArrRoute.getJSONObject(i);

                JSONArray jsonArrLegs = jsonRoute.getJSONArray("legs");

                for(int j = 0; j < jsonArrLegs.length(); j++){
                    JSONObject jsonLegs = jsonArrLegs.getJSONObject(j);


                    JSONArray jsonArrSteps = jsonLegs.getJSONArray("steps");

                    for(int k = 0; k < jsonArrSteps.length(); k++){
                        arrSteps.add(jsonArrSteps.getJSONObject(k));
                    }
                }
            }
        }catch (Exception ex){

        }
    }




}
