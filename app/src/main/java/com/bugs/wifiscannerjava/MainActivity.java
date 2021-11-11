package com.bugs.wifiscannerjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ListView wifiList;
    private WifiManager wifiManager;
    private final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 1;
    WifiReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiList = findViewById(R.id.wifi_list);
        Button scanButton = findViewById(R.id.scan_button);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()){
            Toast.makeText(getApplicationContext(), "Turning WiFi ON....", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cek dahulu apakah aplikasi sudah mendapat ijin access_coarse_location
                // jika belum, maka kita meminta izin dahulu
                // jika sudah, maka lakukan scanning jaringan wifi
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION
                    );
                } else {
                    wifiManager.startScan();
                }
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        wifiReceiver = new WifiReceiver(wifiManager, wifiList);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, intentFilter);
        getWifi();
    }

    private void getWifi(){
        // jika os marshmallow ke atas
        // lek ga, gas scan!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // kasih tau gaes!
            Toast.makeText(MainActivity.this, "version >= marshmallow", Toast.LENGTH_SHORT).show();

            // jika access_coarse_location belum diizinkan, minta izin!!!
            // lek masih dibawah? gass scan!
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
            } else {
                Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            }
        } else {
            Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
    }

    // hasil saat minta izin
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // lek code request e podo mbe code request koeh,
        if (requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION) {
            // lek diijinno, kasih tau! mbe gas scan
            // lek ga, kasih tau lek ga diijinno!
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            } else {
                Toast.makeText(MainActivity.this, "permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}