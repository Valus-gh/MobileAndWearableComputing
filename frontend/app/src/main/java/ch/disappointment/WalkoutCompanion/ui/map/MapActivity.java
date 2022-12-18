package ch.disappointment.WalkoutCompanion.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import ch.disappointment.WalkoutCompanion.R;
import ch.disappointment.WalkoutCompanion.api.ApiService;
import ch.disappointment.WalkoutCompanion.persistence.TracksDaoService;
import ch.disappointment.WalkoutCompanion.persistence.model.Track;


/**
 * Activity used to show a map.
 * It can be opened in 2 modes:
 *  view, just to show an already existing track,
 *  new, upon open, the activity will start recording a new track
 *
 * Intent extras:
 *  mode: OpenModes
 *  track_name: [optional name of the track, required when mode == new]
 *  track_id: [optional id of the track, required when mode == view]
 */
public class MapActivity extends AppCompatActivity {
    public enum OpenModes {
        VIEW, NEW
    }

    public static String EXTRA_KEY_MODE = "mode";
    public static String EXTRA_KEY_TRACK_NAME = "track_name";
    public static String EXTRA_KEY_TRACK_ID = "track_id";

    /* 2 secs */
    // Interval for location updates
    private static long UPDATE_INTERVAL = 2 * 1000;

    private OpenModes mode;
    private String trackName;
    private Long trackId;

    private MapViewModel viewModel;
    private FusedLocationProviderClient locationProvider;

    // OSMDroid: https://github.com/osmdroid/osmdroid
    private MapView map;
    private IMapController controller;
    private Polyline trackPolyline;
    private Marker trackStartMarker;
    private Marker trackEndMarker;

    private FloatingActionButton fab;

    private MapLocationListener locationListener;
    private TracksDaoService tracksDaoService;


    private static double defaultZoom = 18;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // create a db connection
        tracksDaoService = new TracksDaoService(this);

        // retrieve the open mod
        Intent intent = getIntent();
        mode = (OpenModes) intent.getSerializableExtra(EXTRA_KEY_MODE);

        // retrieve viewModel
        ViewModelProvider provider = new ViewModelProvider(this);
        viewModel = provider.get(MapViewModel.class);

        // retrieve map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        controller = map.getController();
        controller.setZoom(defaultZoom);

        // setup FAB
        fab = (FloatingActionButton) findViewById(R.id.mapFab);
        switch (mode) {
            case NEW:
                // retrieve track name
                trackName = intent.getStringExtra(EXTRA_KEY_TRACK_NAME);
                setupForTracking();
                break;
            case VIEW:
                // retrieve track id
                trackId = intent.getLongExtra(EXTRA_KEY_TRACK_ID, -1);
                setupForViewing();
        }

        // receive updates for the track and draw them on screen
        viewModel.track().observe(this, track -> {
            if (track == null || track.getPoints() == null || track.getPoints().isEmpty())
                return;

            List<Track.TrackNode> points = track.getPoints();

            // draw track start marker
            if (trackStartMarker == null)
                trackStartMarker = newMarker();
            trackStartMarker.setPosition(points.get(0).getPoint());

            if (points.size() > 1) {
                // draw polyline
                if (trackPolyline == null)
                    trackPolyline = newPolyline();

                List<GeoPoint> geoPointList = track.getPoints().stream()
                        .map(Track.TrackNode::getPoint)
                        .collect(Collectors.toList());

                trackPolyline.setPoints(geoPointList);

                // draw end marker
                if (trackEndMarker == null)
                    trackEndMarker = newMarker();

                GeoPoint last = points.get(points.size() - 1).getPoint();

                trackEndMarker.setPosition(last);

                // set position to end marker
                controller.setCenter(last);
            }
        });

        map.invalidate();
    }

    /**
     * Create a new marker with default settings
     * @return the new marker
     */
    private Marker newMarker() {
        Marker m = new Marker(map);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlayManager().add(m);
        return m;
    }

    /**
     * Create a new polyline with default settings
     * @return the new polyline
     */
    private Polyline newPolyline() {
        Polyline p = new Polyline();
        map.getOverlayManager().add(p);
        return p;
    }


    private void setupForTracking() {
        // shows the FAB, since we are recording a new track we can stop it
        fab.show();
        fab.setOnClickListener(view -> {
            // navigate back to track list
            tryStopTracking(this::finish);
        });

        startTracking();
    }


    private void setupForViewing() {
        Track t = tracksDaoService.getTrack(this, trackId);
        viewModel.setTrack(t);
        fab.hide();
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

    /**
     * Opens a dialog to confirm the user wants to stop tracking
     * @param onOk callback to be called if the user confirms
     */
    private void tryStopTracking(Runnable onOk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stop Tracking?");
        builder.setMessage("This action will stop tracking your movements. Current track will be saved");

        builder.setPositiveButton("OK", (dialog, which) -> {
            locationProvider.removeLocationUpdates(locationListener);
            onOk.run();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });

        builder.show();
    }

    /**
     * Starts tracking the user's movements
     * MissingPermission is used because we check for permissions with a method,
     * but IDEs don't know that
     */
    @SuppressLint("MissingPermission")
    private void startTracking() {
        // create a new track in the db
        Track t = tracksDaoService.createEmptyTrack(
                this,
                ApiService.getInstance(this).getLoggedUser().getUsername(),
                trackName
        );

        viewModel.setTrack(t);

        // setup the location listeners
        if (checkLocationPermissions()) {
            locationProvider = LocationServices.getFusedLocationProviderClient(this);
            locationListener = new MapLocationListener(this, this, t.getId());
            locationProvider.getLastLocation().addOnSuccessListener(location -> {
                if (location != null)
                    tracksDaoService.addPoint(this,
                            t.getId(),
                            Instant.now(),
                            new GeoPoint(location.getLatitude(), location.getLongitude())
                    );
            });
            locationProvider.requestLocationUpdates(
                    new LocationRequest
                            .Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                            .build(),
                    locationListener,
                    Looper.myLooper()
            );
        }

    }

    /**
     * Prevents the user from leaving the activity while tracking,
     * and asks for confirmation
     */
    @Override
    public void onBackPressed() {
        if (mode == OpenModes.NEW) {
            tryStopTracking(super::onBackPressed);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Checks if the user has granted the location permissions
     * @return true if the user has granted the permissions, false otherwise
     */
    private boolean checkLocationPermissions() {
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
}