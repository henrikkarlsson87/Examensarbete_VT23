package com.test.androidperformancetestapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private String latitude, longitude;
    private TextView fibonacciText, gpsText, apiText, saveFileText, readFileText, errorText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fibonacciText = findViewById(R.id.fibonacciTimeText);
        gpsText = findViewById(R.id.gpsTimeText);
        apiText = findViewById(R.id.apiTimeText);
        saveFileText = findViewById(R.id.saveFileTimeText);
        readFileText = findViewById(R.id.readFileTimeText);
        errorText = findViewById(R.id.errorText);
        Button fibonacciBtn = findViewById(R.id.fibonacciBtn);
        Button gpsBtn = findViewById(R.id.GPSBtn);
        Button apiBtn = findViewById(R.id.APIBtn);

        fibonacciBtn.setOnClickListener(view -> {
            long start = System.nanoTime();
            fibonacci(40);
            String text = "Fibonacci: "+(System.nanoTime()-start)/1000000+"ms";
            fibonacciText.setText(text);
        });
        gpsBtn.setOnClickListener(view -> getLocation());
        apiBtn.setOnClickListener(view -> callAPI());
    }

    private int fibonacci(int n) {
        if(n<=1) {
            return n;
        } else {
            return fibonacci(n-1)+fibonacci(n-2);
        }
    }

    public void getLocation() {
        long start = System.nanoTime();
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                },100);
            }
            fusedLocationProviderClient.getCurrentLocation(100,null).addOnSuccessListener(location -> {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                String text = "GPS: "+(System.nanoTime()-start)/1000000+"ms";
                gpsText.setText(text);
            });
        } catch (Exception e) {
            errorText.setText(e.getMessage());
        }
    }

    private void callAPI() {
        long start = System.nanoTime();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.open-meteo.com/v1/gfs?latitude="+latitude.substring(0,7)+"&longitude="+longitude.substring(0,7)+"&hourly=temperature_2m,relativehumidity_2m,dewpoint_2m,apparent_temperature,pressure_msl,surface_pressure,precipitation,snowfall,precipitation_probability,weathercode,snow_depth,freezinglevel_height,visibility,cloudcover,cloudcover_low,cloudcover_mid,cloudcover_high,evapotranspiration,et0_fao_evapotranspiration,vapor_pressure_deficit,cape,lifted_index,windspeed_10m,windspeed_80m,winddirection_10m,winddirection_80m,windgusts_10m,soil_temperature_0_to_10cm,soil_temperature_10_to_40cm,soil_temperature_40_to_100cm,soil_temperature_100_to_200cm,soil_moisture_0_to_10cm,soil_moisture_10_to_40cm,soil_moisture_40_to_100cm,soil_moisture_100_to_200cm&past_days=92&forecast_days=16";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    String text = "API: "+(System.nanoTime()-start)/1000000+"ms";
                    apiText.setText(text);
                    saveToFile(response);
                }, e -> errorText.setText(e.getMessage()));
        queue.add(stringRequest);
    }

    private void saveToFile(String contents) {
        long start = System.nanoTime();
        try (FileOutputStream fos = getApplicationContext().openFileOutput("test.txt", Context.MODE_PRIVATE)) {
            fos.write(contents.getBytes());
            String text = "Save file: "+(System.nanoTime()-start)/1000000+" ms";
            saveFileText.setText(text);
            readFromFile();
        } catch (Exception e){
            errorText.setText(e.getMessage());
        }
    }
    private void readFromFile() throws FileNotFoundException {
        long start = System.nanoTime();
        FileInputStream fis = getApplicationContext().openFileInput("test.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            errorText.setText(e.getMessage());
        } finally {
            String contents = stringBuilder.toString();
            String text = "Read file: "+(System.nanoTime()-start)/1000000+" ms";
            readFileText.setText(text);
            File file = new File(getApplicationContext().getFilesDir(), "test.txt");
            file.delete();
        }
    }
}