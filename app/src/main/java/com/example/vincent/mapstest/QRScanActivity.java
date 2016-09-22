package com.example.vincent.mapstest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.CAMERA},
                1);
        QrScanner();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(QRScanActivity.this, "Toegang tot camera geweigerd. Het scannen van QR codes werkt niet zonder toegang tot de camera.", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                    startActivity(i);
                }
                return;
            }
            // je kan hier nog checken voor andere permissions
        }
    }

    public void QrScanner()
    {
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result)
    {
        Log.e("handler", result.getText());
        Log.e("handler", result.getBarcodeFormat().toString());


        if(URLUtil.isHttpsUrl(result.getText()) || URLUtil.isHttpUrl(result.getText()))
        {
            Intent i = new Intent(getApplicationContext(),WebViewActivity.class);
            i.putExtra("url", result.getText());
            startActivity(i);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Scan Result");
            builder.setMessage(result.getText());
            AlertDialog alert1 = builder.create();
            alert1.show();
            mScannerView.resumeCameraPreview(this);
        }
    }
}
