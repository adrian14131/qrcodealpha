package com.ozog.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String JSON_URL = "http://ux.up.krakow.pl/~adrian.ozog/testjson";
    Button mButton, mJsonButton;
    TextView mTextView, mJsonTextView;

    JSONObject jsonObject;
    JSONArray playersJson;
    private static final int REQUEST_CAMERA = 123;
    private IntentIntegrator qrScan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.scanButton);
        mButton.setOnClickListener(this);
        mButton.setEnabled(false);
        mJsonButton = findViewById(R.id.jsonButton);
        mJsonButton.setOnClickListener(this);
        mTextView = findViewById(R.id.textView);
        mJsonTextView = findViewById(R.id.jsonTextView);

        qrScan = new IntentIntegrator(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isCameraPermission()) {
                Toast.makeText(this, "Camera permission allow", Toast.LENGTH_LONG).show();
                mButton.setEnabled(true);
            } else {
                //requestCameraPermission();
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.INTERNET
                }, REQUEST_CAMERA);

            }

        }

    }

    private void jsonLoad(){

        DownloadJsonTask downloadJson = new DownloadJsonTask();

        String jsonContent = "";

        try {
            jsonContent = downloadJson.execute(JSON_URL).get();
            Log.e("JSON", "jsonLoad: "+jsonContent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (jsonContent != null){
            try {
                jsonObject = new JSONObject(jsonContent);
                playersJson = jsonObject.getJSONArray("players");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (playersJson != null) {
            for(int i=0; i<playersJson.length(); i++){

                String line = "";
                JSONObject tempJsonObject = playersJson.optJSONObject(i);
                if (tempJsonObject != null){
                    if(tempJsonObject.has("nick") && tempJsonObject.has("name") && tempJsonObject.has("lvl")){

                        try {
                            line = "Imię: "+tempJsonObject.getString("name")+" nick: "+tempJsonObject.getString("nick")+" lvl: "+tempJsonObject.getInt("lvl");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                stringBuilder.append(line + "\n");
            }
        }


        mJsonTextView.setText(stringBuilder.toString()+"\n"+jsonContent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "Result not found", Toast.LENGTH_LONG).show();

            }
            else{

                mTextView.setText(result.getContents());
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean isCameraPermission() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);

    }

    /*private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission allow", Toast.LENGTH_LONG).show();
                    mButton.setEnabled(true);
                } else {
                    Toast.makeText(this, "Camera permission deny", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scanButton:
                //qrScan.setOrientationLocked(false);
                qrScan.initiateScan();
                break;
            case R.id.jsonButton:
                jsonLoad();
                break;
        }
    }


    //Async Task do pobrania JSON'a
    public class DownloadJsonTask extends AsyncTask<String, Void, String> {

        public String streamToString(InputStream is){
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));


                String line = "";

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return sb.toString();

        }
        @Override
        protected String doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                URLConnection connection = url.openConnection();

                InputStream is = connection.getInputStream();

                return streamToString(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Zwrócić null czy pusty string ("") ?
            return null;
        }
    }
}