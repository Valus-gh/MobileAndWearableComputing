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

/**
 * LocationListener implementation that is used to update the current position on the map
 */
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
        // update the current position in the view model
        ViewModelProvider provider = new ViewModelProvider(storeOwner);
        MapViewModel viewModel = provider.get(MapViewModel.class);
        GeoPoint point = new GeoPoint(
                location.getLatitude(),
                location.getLongitude()
        );

        // add the new point to the track and update the track in the database
        tracksDaoService.addPoint(context, trackId, Instant.now(), point);
        Track t = tracksDaoService.getTrack(context, trackId);
        viewModel.setTrack(t);
    }
}
