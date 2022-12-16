package ch.disappointment.WalkoutCompanion.ui.map;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.osmdroid.util.GeoPoint;

import ch.disappointment.WalkoutCompanion.persistence.model.Track;

public class MapViewModel extends ViewModel {
    private final MutableLiveData<GeoPoint> currentLocation;
    private final MutableLiveData<Track> currentTrack;

    public MapViewModel() {
        this.currentLocation = new MutableLiveData<>();
        this.currentTrack = new MutableLiveData<>();
    }

    public void setCurrentLocation(GeoPoint location) {
        this.currentLocation.postValue(location);
    }

    public MutableLiveData<GeoPoint> currentLocation() {
        return currentLocation;
    }

    public MutableLiveData<Track> track() {
        return currentTrack;
    }

    public void setTrack(Track t){
        this.currentTrack.setValue(t);
    }

    public void addGeoPoint(GeoPoint p) {
        Track t = currentTrack.getValue();
        if(t == null)
            return;

        t.addPoint(p);
    }
}
