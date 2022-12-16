package ch.disappointment.WalkoutCompanion.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import java.util.Objects;

import ch.disappointment.WalkoutCompanion.R;


public class ProfileFragment extends Fragment {
    private ProfileViewModel profileViewModel;
    private MapView map;
    private FusedLocationProviderClient locationProvider;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        profileViewModel = viewModelProvider.get(ProfileViewModel.class);

        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        map = (MapView) root.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        locationProvider = LocationServices.getFusedLocationProviderClient(requireContext());
        updateLastLocation(requireContext());

        return root;
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


    private void updateLastLocation(Context ctx) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            Toast t = new Toast(requireContext());
            t.setText("You have not granted location permission. Some feature may not work.");
            return;
        }
        IMapController controller = map.getController();
        controller.setZoom(9.5);

        Task<Location> task = locationProvider.getLastLocation();
        task.addOnSuccessListener(location -> {
            if(location == null){
                Toast t = new Toast(ctx);
                t.setText("Your location is unknown");
                return;
            }

            double lat = location.getLatitude();
            double lon = location.getLongitude();
            double alt = location.getAltitude();
            Log.i("LOCATION", "last known location: " + lat + ", " + lon + ", " + alt);

            GeoPoint current = new GeoPoint(lat, lon, alt);
            controller.setCenter(current);
        });

        task.addOnFailureListener(error -> {
            Toast toast = new Toast(ctx);
            toast.setText(error.toString());
            toast.show();
        });

    }
}