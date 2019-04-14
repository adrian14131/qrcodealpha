package com.ozog.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    Button mButton;
    TextView mTextView;
    private static final int REQUEST_CAMERA = 123;
    private IntentIntegrator qrScan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.scanButton);
        mButton.setOnClickListener(this);
        mButton.setEnabled(false);
        mTextView = findViewById(R.id.textView);

        qrScan = new IntentIntegrator(this);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isCameraPermission()) {
                Toast.makeText(this, "Camera permission allow", Toast.LENGTH_LONG).show();
                mButton.setEnabled(true);
            } else {
                requestCameraPermission();
            }
        }


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

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

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

        }
    }
}