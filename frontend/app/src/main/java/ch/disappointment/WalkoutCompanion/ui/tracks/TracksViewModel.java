package ch.disappointment.WalkoutCompanion.ui.tracks;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import ch.disappointment.WalkoutCompanion.persistence.model.Track;

public class TracksViewModel extends ViewModel {
    private MutableLiveData<ArrayList<Track>> tracks;
    
    public TracksViewModel(){
        tracks = new MutableLiveData<>(new ArrayList<>());
    }

    public MutableLiveData<ArrayList<Track>> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks.setValue(tracks);
    }
}
