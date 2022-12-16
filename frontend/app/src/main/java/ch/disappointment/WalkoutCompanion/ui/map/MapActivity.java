package ch.disappointment.WalkoutCompanion.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import ch.disappointment.WalkoutCompanion.R;


public class MapActivity extends AppCompatActivity {
    private MapViewModel viewModel;
    private FusedLocationProviderClient locationProvider;

    private MapView map;
    private Marker currentLocationMarker;
    private FloatingActionButton fab;


    private static double defaultZoom = 18;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        // retrieve viewModel
        ViewModelProvider provider = new ViewModelProvider(this);
        viewModel = provider.get(MapViewModel.class);

        // retrieve map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // setup FAB
        fab = (FloatingActionButton) findViewById(R.id.mapFab);
        fab.setOnClickListener(view -> {

        });

        // setup the location listeners
        if(checkLocationPermissions()) {
            locationProvider = LocationServices.getFusedLocationProviderClient(this);
            locationProvider.requestLocationUpdates(
                    new LocationRequest
                            .Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                            .build(),
                    new MapLocationListener(this),
                    Looper.myLooper()
            );

            updateLastLocation(this);
        }


        viewModel.currentLocation().observe(this, this::setMapMarkerPosition);
    }


    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void setMapMarkerPosition(GeoPoint geoPoint){
        if(this.currentLocationMarker == null) {
            this.currentLocationMarker = new Marker(map);
            this.currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(currentLocationMarker);
        }

        Log.i("LOCATION_UPDATE", geoPoint.toString());
        this.currentLocationMarker.setPosition(geoPoint);
        map.invalidate();
    }

    private boolean checkLocationPermissions(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            Toast t = new Toast(this);
            t.setText("You have not granted location permission. Some feature may not work.");
            return false;
        }

        return true;
    }

    private void updateLastLocation(Context ctx) {
        IMapController controller = map.getController();
        controller.setZoom(defaultZoom);

        if(checkLocationPermissions()) {
            @SuppressLint("MissingPermission")
            Task<Location> task = locationProvider.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location == null) {
                    Toast t = new Toast(ctx);
                    t.setText("Your location is unknown");
                    return;
                }

                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double alt = location.getAltitude();
                Log.i("LOCATION", "last known location: " + lat + ", " + lon + ", " + alt);

                GeoPoint current = new GeoPoint(lat, lon, alt);
                controller.setZoom(defaultZoom);
                controller.setCenter(current);
            });

            task.addOnFailureListener(error -> {
                Toast toast = new Toast(ctx);
                toast.setText(error.toString());
                toast.show();
            });
        }
    }
}