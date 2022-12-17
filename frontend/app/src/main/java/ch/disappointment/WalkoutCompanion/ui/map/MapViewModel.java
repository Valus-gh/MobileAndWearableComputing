package ch.disappointment.WalkoutCompanion.ui.map;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.osmdroid.util.GeoPoint;

import ch.disappointment.WalkoutCompanion.persistence.model.Track;

public class MapViewModel extends ViewModel {
    private final MutableLiveData<Track> currentTrack;

    public MapViewModel() {
        this.currentTrack = new MutableLiveData<>();
    }

    public MutableLiveData<Track> track() {
        return currentTrack;
    }

    public void setTrack(Track t){
        this.currentTrack.setValue(t);
    }
}
