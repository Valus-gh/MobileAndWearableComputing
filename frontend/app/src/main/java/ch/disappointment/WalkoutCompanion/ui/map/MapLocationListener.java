package ch.disappointment.WalkoutCompanion.ui.map;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.gms.location.LocationListener;

import org.osmdroid.util.GeoPoint;

import java.time.Instant;

import ch.disappointment.WalkoutCompanion.persistence.TracksDaoService;
import ch.disappointment.WalkoutCompanion.persistence.model.Track;

public class MapLocationListener implements LocationListener {
    private ViewModelStoreOwner storeOwner;
    private Context context;
    private TracksDaoService tracksDaoService;
    private Long trackId;

    MapLocationListener(ViewModelStoreOwner storeOwner, Context ctx, Long trackId) {
        super();
        this.storeOwner = storeOwner;
        this.context = ctx;
        this.trackId = trackId;
        this.tracksDaoService = new TracksDaoService(ctx);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        ViewModelProvider provider = new ViewModelProvider(storeOwner);
        MapViewModel viewModel = provider.get(MapViewModel.class);
        GeoPoint point = new GeoPoint(
                location.getLatitude(),
                location.getLongitude()
        );

        tracksDaoService.addPoint(context, trackId, Instant.now(), point);
        Track t = tracksDaoService.getTrack(context, trackId);
        viewModel.setTrack(t);
    }
}
