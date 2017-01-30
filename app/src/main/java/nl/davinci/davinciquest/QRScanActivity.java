package nl.davinci.davinciquest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

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

    //check to seeif the app has permission to te camera
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
                else
                {
                    Toast.makeText(QRScanActivity.this, R.string.error_camera_refused, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                    startActivity(i);
                }
                return;
            }
            // You can check for other permissions
        }
    }

    public void QrScanner()
    {
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
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

    //shows a webview of the scanned qr was a url, else if it was a markerid, send it back to the map
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
            Intent intent = new Intent();
            intent.putExtra("markerId", result.getText());
            setResult(RESULT_OK, intent);
            onBackPressed();
        }
    }
}