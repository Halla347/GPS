package com.example.gps;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.w3c.dom.Text;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements LocationListener {
    SwipeRefreshLayout swipeRefreshLayout;
    private TextView text_network;
    private TextView text_gps;

    private static String TAG = "2020";
    private static final int MY_PERMISSION_ACCES_FINE_LOCATION = 1;
    private static final int MY_PERMISSION_ACCES_COARSE_LOCATION = 2;
    private TextView bestprovider;
    private TextView longitude;
    private TextView latitude;
    private TextView archivaldata;
    private LocationManager locationManager;
    private Criteria criteria;
    private Location location;
    private String bp;
    private int amount;


    private MapView osm;
    private MapController mapController;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.refreshLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(findViewById(R.id.toolbar));
        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        text_network = findViewById(R.id.text_network);
        text_gps = findViewById(R.id.text_gps);
        bestprovider = findViewById(R.id.bestprovider);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        archivaldata = findViewById(R.id.archival_data);

        swipeRefreshLayout.setOnRefreshListener(() ->{
            swipeRefreshLayout.setRefreshing(false);
            boolean connsection = isNetworkAvailable();
            if (connsection){
                text_network.setText("internet connect");
                text_network.setTextColor(Color.GREEN);
            }
            else {
                text_network.setText("no internet");
                text_network.setTextColor(Color.RED);
            }
            if (bp != null){
                text_gps.setText("gps connect");
                text_gps.setTextColor(Color.GREEN);
            }else {
                text_gps.setText("no gps");
                text_gps.setTextColor(Color.RED);
            }


        });
        swipeRefreshLayout.setColorSchemeColors(Color.YELLOW);

        criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        bp = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        location = locationManager.getLastKnownLocation(bp);


        locationManager.requestLocationUpdates(""+bp,
                500,
                0.5f,
                (LocationListener) this);
        bestprovider.setText("best provider: " + bp);
        location = locationManager.getLastKnownLocation(bp);

        if (location != null) {
            longitude.setText("longitude: " + location.getLongitude());
            latitude.setText("latitude: " + location.getLatitude());
        } else {
            longitude.setText("longitude: --");
            latitude.setText("latitude: --");
            Log.d("GPSA", "No last known location available yet");
        }

        archivaldata.setText("Mesarument readings:\n\n");
        Log.d("GPSA", bp + " " + location.getLongitude() + " " + location.getLatitude());
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)  {
            requestPermissions(new String[]{Manifest.permission.INTERNET},1 );

        }
        osm = findViewById(R.id.osm);
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);

        mapController = (MapController) osm.getController();
        mapController.setZoom(12);

        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        mapController.setCenter(geoPoint);

        mapController.animateTo(geoPoint);
        addMarkerToMap (geoPoint);
        osm.setMapListener(new MapListener() {

            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(TAG, "onScroll: ");
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(TAG, "onZoom: ");
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.sms){
            Intent intent = new Intent(this, SendSms.class);
            intent.putExtra("latitude", location.getLatitude());
            intent.putExtra("longitude", location.getLongitude());
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.save_corinates){
            View root = getWindow().getDecorView().getRootView();
            Bitmap bitmap = takeScreenshot(root);
            Uri uri = saveBitmapToGallery(this, bitmap);
            Toast.makeText(this, "Screenshot saved to " + uri, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (item.getItemId() == R.id.share_cordinates){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "My location: " + location.getLongitude() + ": " + location.getLatitude());
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Share location"));
            return true;
        }
        return false;
    }

    public void addMarkerToMap(GeoPoint ceter) {
        Marker marker = new Marker(osm);
        marker.setPosition(ceter);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getResources().getDrawable(R.drawable.marker));
        osm.getOverlays().add(marker);
        osm.invalidate();
        marker.setTitle("My position");
    }

    public static Bitmap takeScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    public static Uri saveBitmapToGallery(Context context, Bitmap bitmap) {
        String filename = "screenshot_" + System.currentTimeMillis() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Screenshots/");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) return null;

        try {
            OutputStream out = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            context.getContentResolver().update(uri, values, null, null);

            return uri;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSION_ACCES_FINE_LOCATION:{
                if (permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("GPSA", requestCode + " "+ permissions[0] + grantResults[0]);
                    Log.d(TAG, "[er,ossopms ACCES_fine_loacation was granted");
                    Toast.makeText(this, "PERmisson ACCES_FINE_LOCATION WAS GRANTED", Toast.LENGTH_SHORT).show();
                    this.recreate();
                }else {
                    Log.d(TAG, "permisssion acces fine location denied");
                    Toast.makeText(this, "PERmisson ACCES_FINE_LOCATION WAS Denied", Toast.LENGTH_SHORT).show();

                }
                break;
            }
            case MY_PERMISSION_ACCES_COARSE_LOCATION:{
                if (permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("GPSA", requestCode + " "+ permissions[0] + grantResults[0]);
                    Log.d(TAG, "[er,ossopms ACCESS_COARSE_LOCATION was granted");
                    Toast.makeText(this, "PERmisson ACCESS_COARSE_LOCATION WAS GRANTED", Toast.LENGTH_SHORT).show();
                    this.recreate();
                }else {
                    Log.d(TAG, "permisssion ACCESS_COARSE_LOCATION denied");
                    Toast.makeText(this, "PERmisson ACCESS_COARSE_LOCATION WAS Denied", Toast.LENGTH_SHORT).show();

                }
                break;
            }
            default: Log.d(TAG, "another permissions");
        }
    }

    private boolean isNetworkAvailable() {
        return true;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        bp = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            location = locationManager.getLastKnownLocation(bp);
            bestprovider.setText("best provider" + bp);
            longitude.setText("longitude" + location.getLongitude());
            latitude.setText("latitude" + location.getLatitude());
            archivaldata.setText(archivaldata.getText() + " " + location.getLongitude() + " " + location.getLatitude());
            amount+=1;
            Log.d("GPSA", amount + " pomiar"  + bp + " " + location.getLongitude() + " " + location.getLatitude());
        }
    }
}
