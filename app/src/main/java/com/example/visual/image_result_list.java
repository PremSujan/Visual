package com.example.visual;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class image_result_list extends AppCompatActivity {


    ImageButton imagebutton_1;
    ImageButton imagebutton_2;
    ImageButton imagebutton_3;
    ImageButton imagebutton_4;
    ImageButton imagebutton_5;
    String[] urls_string = {"","","","",""};
    String[] shop_urls =   {"","","","",""};
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_results);
        imagebutton_1 =findViewById(R.id.result_1);
        imagebutton_1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uriUrl = Uri.parse(shop_urls[0]);
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                }
        );
        imagebutton_2 = findViewById(R.id.result_2);
        imagebutton_2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uriUrl = Uri.parse(shop_urls[1]);
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                }
        );
        imagebutton_3 = findViewById(R.id.result_3);
        imagebutton_3.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uriUrl = Uri.parse(shop_urls[2]);
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                }
        );
        imagebutton_4 = findViewById(R.id.result_4);
        imagebutton_4.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uriUrl = Uri.parse(shop_urls[3]);
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                }
        );
        imagebutton_5 = findViewById(R.id.result_5);
        imagebutton_5.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uriUrl = Uri.parse(shop_urls[4]);
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                }
        );
        Bundle bundle = getIntent().getExtras();
        String bing_search_key =(String)bundle.get("keyword");
        BingSearchTask temp_object = new BingSearchTask(this,bing_search_key,urls_string,shop_urls);
        temp_object.execute();
    }


    public void loadimagesfromurl(String[] url_string){
        new DownloadImageTask(imagebutton_1).execute(url_string[0]);
        new DownloadImageTask(imagebutton_2).execute(url_string[1]);
        new DownloadImageTask(imagebutton_3).execute(url_string[2]);
        new DownloadImageTask(imagebutton_4).execute(url_string[3]);
        new DownloadImageTask(imagebutton_5).execute(url_string[4]);
    }

}



class BingSearchTask extends AsyncTask<Void, Void, JsonArray> {


    private String[] urls;
    private String[] shops;
    private String searchterm;
    image_result_list parent;
    public BingSearchTask(image_result_list par,String searchStr,String[] str,String[] shop_str) {
        Log.v("Inside Bing search task","during calling, passeed att");
        searchterm = searchStr;
        this.urls = str;
        this.shops = shop_str;
        this.parent = par;
    }


    @Override
    protected JsonArray doInBackground(Void... voids) {


        Log.v("Inside do in background","initialising variables");
        String subscriptionKey = "c9913d6f7f0642ec8672ce8faa49c29e";
        String host = "https://api.cognitive.microsoft.com";
        String path = "/bing/v7.0/images/search";
        String searchTerm = searchterm;
        String numOfResultsStr =  "&$top=10";
        URL url = null;
        try {
            url = new URL(host + path + "?q=" +  URLEncoder.encode(searchTerm, "UTF-8")+ "%27" + numOfResultsStr + "&$format=json" + "&$imageType=Shopping");
            Log.v("url is",""+ url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.v("Bing search task","url done");
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection)url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("Bing search task","connection done");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
        connection.setRequestProperty("Accept","*/*");
        Log.v("Bing search task","connection request prop done");
        // receive JSON body
        InputStream stream = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        } ;
        try {
            int responseCode = connection.getResponseCode(); //can call this instead of con.connect()
            if (responseCode >= 400 && responseCode <= 499) {
                Log.v("Bad status: ",""+responseCode); //provide a more meaningful exception message
            }
            else {
                stream = connection.getInputStream();
                //etc...
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("Bing search task","got stream result");
        String response = new Scanner(stream).useDelimiter("\\A").next();
        // construct result object for return
        SearchResults results = new SearchResults(new HashMap<String, String>(), response);

        // extract Bing-related HTTP headers
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String header : headers.keySet()) {
            if (header == null) continue;      // may have null key
            if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
                results.relevantHeaders.put(header, headers.get(header).get(0));
                //throw new JSONException("json exception");
            }
        }
        try {
            stream.close();
            Log.v("closing stream","closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(results.jsonResponse).getAsJsonObject();
        //get the first image result from the JSON object, along with the total
        //number of images returned by the Bing Image Search API.
        //String total = json.get("totalEstimatedMatches").getAsString();
        JsonArray json_results = json.getAsJsonArray("value");
        JsonObject first_result = (JsonObject)json_results.get(0);
        String resultURL = first_result.get("thumbnailUrl").getAsString();
        Log.v("result url",resultURL);
        //Log.v("number of results",""+total);
        return json_results;
    }

    @Override
    protected void onPostExecute(JsonArray result) {
        Log.v("starting postexecute","go...");
        JsonObject result_1 = (JsonObject) result.get(0);
        JsonObject result_2 = (JsonObject) result.get(1);
        JsonObject result_3 = (JsonObject) result.get(2);
        JsonObject result_4 = (JsonObject) result.get(3);
        JsonObject result_5 = (JsonObject) result.get(4);

        urls[0] = result_1.get("thumbnailUrl").getAsString();
        urls[1] = result_2.get("thumbnailUrl").getAsString();
        urls[2] = result_3.get("thumbnailUrl").getAsString();
        urls[3] = result_4.get("thumbnailUrl").getAsString();
        urls[4] = result_5.get("thumbnailUrl").getAsString();

        shops[0] = result_1.get("hostPageDisplayUrl").getAsString();
        shops[1] = result_2.get("hostPageDisplayUrl").getAsString();
        shops[2] = result_3.get("hostPageDisplayUrl").getAsString();
        shops[3] = result_4.get("hostPageDisplayUrl").getAsString();
        shops[4] = result_5.get("hostPageDisplayUrl").getAsString();

        Log.v("urls for images",urls[0]);
        Log.v("urls for images",urls[1]);
        Log.v("urls for images",urls[2]);
        Log.v("urls for images",urls[3]);
        Log.v("urls for images",urls[4]);

        Log.v("urls for shops",shops[0]);
        Log.v("urls for shops",shops[1]);
        Log.v("urls for shops",shops[2]);
        Log.v("urls for shops",shops[3]);
        Log.v("urls for shops",shops[4]);

        parent.loadimagesfromurl(urls);
//        parent.imagebutton_1.setImageBitmap(parent.getBitmapFromURL(urls[0]));
//        parent.imagebutton_2.setImageBitmap(parent.getBitmapFromURL(urls[1]));
//        parent.imagebutton_3.setImageBitmap(parent.getBitmapFromURL(urls[2]));
//        parent.imagebutton_4.setImageBitmap(parent.getBitmapFromURL(urls[3]));
//        parent.imagebutton_5.setImageBitmap(parent.getBitmapFromURL(urls[4]));

    }

}


 class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageButton bmImage;

    public DownloadImageTask(ImageButton bmImage) {
        this.bmImage = bmImage;
    }
    @Override
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}


class SearchResults{
    HashMap<String, String> relevantHeaders;
    String jsonResponse;
    SearchResults(HashMap<String, String> headers, String json) {
        relevantHeaders = headers;
        jsonResponse = json;
    }
}