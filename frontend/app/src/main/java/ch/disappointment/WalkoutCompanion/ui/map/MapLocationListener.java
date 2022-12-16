package ch.disappointment.WalkoutCompanion.ui.map;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.anychart.scales.Geo;
import com.google.android.gms.location.LocationListener;

import org.osmdroid.util.GeoPoint;

import java.util.Map;

public class MapLocationListener implements LocationListener {
    private ViewModelStoreOwner context;

    MapLocationListener(ViewModelStoreOwner ctx) {
        super();
        this.context = ctx;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        ViewModelProvider provider = new ViewModelProvider(context);
        MapViewModel viewModel = provider.get(MapViewModel.class);
        viewModel.setCurrentLocation(new GeoPoint(
                location.getLatitude(),
                location.getLongitude()
        ));
    }
}
