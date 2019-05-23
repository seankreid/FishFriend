package com.fishfriend.fishfriend_final;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.Window;
import android.content.Intent;
import java.lang.*;
import java.util.*;
import java.lang.Object.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutionException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.*;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.annotations.*;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.geojson.GeoJson;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {
    FirebaseDatabase database;
    DatabaseReference ref;
    private MapView mapView;
    private FirebaseAuth mAuth;
    private Marker marker;
    private Button dropPin;
    private MapboxMap map;
    boolean canAdd = true;
    static ArrayList<String[]> buoys = new ArrayList<String[]>();
    static ArrayList<String[]> umarks = new ArrayList<String[]>();
    static boolean tstate = false;
    static boolean fstate = false;
    double currentlat = 0.0000;
    double currentlon = 0.0000;
    String userid = "none";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userid = "none";
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("marker");
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getEmail();
        Mapbox.getInstance(this, "pk.eyJ1IjoiamFzb25zYnJhdW4iLCJhIjoiY2ptaWI4bHkzMDM1eTNxcWxpdzhxZDhkZCJ9.JoQJGtq9YXrufENaFLIbvg");
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String[] mark = new String[3];
                    Double lati = ds.child("lat").getValue(Double.class);
                    Double longi = ds.child("lon").getValue(Double.class);
                    String mail = ds.child("username").getValue(String.class);
                    mark[0] = lati.toString();
                    mark[1] = longi.toString();
                    mark[2] = mail;
                    umarks.add(mark);
                }
                fstate = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        dropPin = (Button)findViewById(R.id.addmarker);
        Button buttonclick = (Button)findViewById(R.id.signin_btn);
        buttonclick.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent1 = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent1);
            }
        });
        dropPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropPin.setEnabled(false);
                DatabaseReference myRef = database.getReference("marker");
                String markerID = myRef.push().getKey();
                uMarker mark = new uMarker(userid,currentlat,currentlon,Calendar.getInstance().getTime().toGMTString());
                myRef.child(markerID).setValue(mark);
            }
        });
    }
    @Override
    public void onMapReady(MapboxMap mapboxMap){
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        Icon buoyIcon = iconFactory.fromResource(R.drawable.buoy);
        IconFactory iconFactory2 = IconFactory.getInstance(MainActivity.this);
        Icon buoyIcon2 = iconFactory.fromResource(R.drawable.marker);
        map = mapboxMap;
        map.addOnMapClickListener(this);
        try{ new getBuoys().execute().get(); }
        catch (Exception e){}
        if (fstate == true) {
            int len2 = umarks.size();
            for (int i = 0; i < len2; i++) {
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(umarks.get(i)[0]), Double.parseDouble(umarks.get(i)[1])))
                        .title("User Marker")
                        .snippet("User's Email: " + umarks.get(i)[2])
                        .icon(buoyIcon2));
            }
        }
        if (tstate == true) {
            int leng = buoys.size();
            for (int i = 0; i < leng; i++) {
                String snip = "\nWindspeed: " + buoys.get(i)[3] + " Meters per Second\nWave Height: " + buoys.get(i)[4] + " Meters\nAir Temperature: " + buoys.get(i)[5] + " Celsius\nWater Temperature: " + buoys.get(i)[6] + " Celsius";
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(buoys.get(i)[1]), Double.parseDouble(buoys.get(i)[2])))
                        .title(buoys.get(i)[0])
                        .snippet(snip)
                        .icon(buoyIcon));
            }
        }
    }
    @Override
    public void onMapClick(@NonNull LatLng point){
        //IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        //Icon mark = iconFactory.fromResource(R.drawable.marker);
        if (!canAdd){
            marker.remove();
            marker = map.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Untitled")
            );
            currentlat = point.getLatitude();
            currentlon = point.getLongitude();
            dropPin.setEnabled(true);
        }
        else{
            marker = map.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Untitled")
            );
            currentlat = point.getLatitude();
            currentlon = point.getLongitude();
            dropPin.setEnabled(true);
            canAdd = false;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private class getBuoys extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL("https://www.ndbc.noaa.gov/data/latest_obs/latest_obs.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                in.readLine();
                in.readLine();
                String line;
                while((line = in.readLine()) != null){
                    String[] splited = line.trim().replaceAll(" +", " ").split(" ");
                    String sDate1 = splited[3] + "/" + splited[4] + "/" + splited[5] + "/" + splited[6] + "/" + splited[7];
                    String[] buoy = new String[7];
                    try{ java.util.Date date1 = new java.text.SimpleDateFormat("yyyy/MM/dd/hh/mm").parse(sDate1); }
                    catch(Exception e){System.out.println(e.getMessage());}

                    String stationId = splited[0];
                    String lat = splited[1];
                    String lon = splited[2];
                    String windSpeed = splited[9];
                    String waveHeight = splited[11];
                    String airTemperature = splited[17];
                    String waterTemperature = splited[18];

                    if(windSpeed.toLowerCase().equals("mm")){windSpeed = "Data N/A"; }
                    if(waveHeight.toLowerCase().equals("mm")){waveHeight = "Data N/A"; }
                    if(airTemperature.toLowerCase().equals("mm")){ airTemperature = "Data N/A"; }
                    if(waterTemperature.toLowerCase().equals("mm")){ waterTemperature = "Data N/A"; }
                    buoy[0] = stationId;
                    buoy[1] = lat;
                    buoy[2] = lon;
                    buoy[3] = windSpeed;
                    buoy[4] = waveHeight;
                    buoy[5] = airTemperature;
                    buoy[6] = waterTemperature;
                    buoys.add(buoy);
                }
                in.close();
            }
            catch (MalformedURLException e){ }
            catch (IOException e){ }
            tstate = true;
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            tstate = true;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}