package tebogo11.MADProject.placestostay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.renderscript.ScriptGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {


    class PlaceToStayFromOnline extends AsyncTask<String,Void,String>
    {

        @Override
        public String doInBackground(String... urlforsearch) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlforsearch[0]);
                conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                if(conn.getResponseCode() == 200)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String result = "", line;
                    while((line = br.readLine()) != null)
                    {
                        result += line;
                    }
                    return result;
                } else
                {
                    return "HTTP ERROR :" + conn.getResponseCode();
                }
            }
            catch (IOException e) {
                return e.toString();
            }
            finally {
                if(conn!=null)
                {
                    conn.disconnect();
                }
            }
        }

        public void onPostExecute(String result)
        {
            try {
                JSONArray jsonArr = new JSONArray(result);
                items = new ItemizedIconOverlay<OverlayItem>(MainActivity.this, new ArrayList<OverlayItem>(), markerGestureListner);

                for(int i=0;i<jsonArr.length(); i++)
                {
                    JSONObject currentObj = jsonArr.getJSONObject(i);
                    String name = currentObj.getString("name"),
                            type = currentObj.getString("type");
                    double price = currentObj.getDouble("price"),
                            longitude = currentObj.getDouble("lon"),
                            latitude = currentObj.getDouble("lat");
                    String popuptext = "Name : " + name + "\nType : " + type + "\nPrice - £ " + price;
                    OverlayItem newPlacetoStay = new OverlayItem(name,popuptext, new GeoPoint(latitude, longitude));
                    items.addItem(newPlacetoStay);
                    mv.getOverlays().add(items);
                }

                markerGestureListner = new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        Toast.makeText(MainActivity.this,"Name : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        Toast.makeText(MainActivity.this, item.getSnippet(), Toast.LENGTH_SHORT).show();
                        return true;

                    }
                };

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    MapView mv;
    ItemizedIconOverlay<OverlayItem> items;
    ItemizedIconOverlay.OnItemGestureListener<OverlayItem> markerGestureListner;
    double lon , lat;

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_main);
        LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mv = findViewById(R.id.map1);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

    }

    public void onStart() {
        super.onStart();
        mv.setMultiTouchControls(true);
        mv.getController().setZoom(13.0);

    }
    @Override
    public void onResume() {

        super.onResume();
        try {
            new FileWriter(Environment.getExternalStorageDirectory() + "/placestostay.txt", true);
            BufferedReader reader = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory() + "/placestostay.txt"));
        String line = "";
            items = new ItemizedIconOverlay<OverlayItem>(this, new ArrayList<OverlayItem>(), markerGestureListner);
            while((line = reader.readLine()) != null)
            {
                String [] components = line.split(",");
                if(components.length==5)
                {
                    String name = components[0];
                    String type = components[1];
                    String price = components[2];
                    double itemLatitude = Double.parseDouble(components[3]);
                    double itemLongitude = Double.parseDouble(components[4]);
                    String popuptext = "Name : " + name + "\nType : " + type + "\nPrice - £ " + price;
                    OverlayItem newPlacetoStay = new OverlayItem(name,popuptext, new GeoPoint(itemLatitude, itemLongitude));
                    items.addItem(newPlacetoStay);
                    mv.getOverlays().add(items);
                }
            }
            reader.close();
    } catch(IOException e) {
            System.out.println("I/O Error " + e);
    }

        markerGestureListner = new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                Toast.makeText(MainActivity.this,"Name : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                Toast.makeText(MainActivity.this, item.getSnippet(), Toast.LENGTH_SHORT).show();
                return true;

            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean onlyUsersPTS = prefs.getBoolean("onlyUsersPTS", true);
        PlaceToStayFromOnline load = new PlaceToStayFromOnline();
        if(!onlyUsersPTS) {
            load.execute("https://www.hikar.org/course/ws/get.php?year=20&format=json");
        } else{
            load.execute("https://www.hikar.org/course/ws/get.php?year=20&username=user012&format=json");
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        lat = location.getLatitude();
        lon = location.getLongitude();
        mv.getController().setCenter(new GeoPoint(lat,lon));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("com.tebogo11.latitude", String.valueOf(lat));
        editor.putString("com.tebogo11.longitude", String.valueOf(lon));
        editor.commit();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        lat = 51.05;
        lon = -0.72;
        mv.getController().setCenter(new GeoPoint(lat,lon));

    }


    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.AddPlace)
        {
            Intent intent = new Intent(this,AddPlaceToStayActivity.class);
            startActivityForResult(intent, 0);
            return true;
        } else if(item.getItemId() == R.id.Setting)
        {
            Intent intent = new Intent(this,PrefsActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            if(resultCode ==Activity.RESULT_OK)
            {
                Bundle extras = intent.getExtras();
                String name = extras.getString("com.tebogo11.name");
                String type = extras.getString("com.tebogo11.type");
                double price = extras.getDouble("com.tebogo11.price");
                double itemLatitude = extras.getDouble("com.tebogo11.addlat");
                double itemLongitude = extras.getDouble("com.tebogo11.addlong");
                items = new ItemizedIconOverlay<OverlayItem>(this, new ArrayList<OverlayItem>(), markerGestureListner);
                String popuptext = "Name : " + name + "\nType : " + type + "\nPrice - £ " + price;
                OverlayItem newPlacetoStay = new OverlayItem(name,popuptext, new GeoPoint(itemLatitude, itemLongitude));
                items.addItem(newPlacetoStay);
                mv.getOverlays().add(items);

            }
        }
    }

}
