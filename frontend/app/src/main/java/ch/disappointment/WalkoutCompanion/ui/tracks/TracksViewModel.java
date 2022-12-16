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
        ArrayList<Track.TrackNode> nodes = new ArrayList<>();
        Track.TrackNode node1 = new Track.TrackNode();
        Track.TrackNode node2 = new Track.TrackNode();
        node1.setTime(Instant.now());
        node2.setTime(Instant.now());
        
        nodes.add(node1);
        nodes.add(node2);
        
        Track t1 = new Track();
        t1.setPoints(nodes);
        
        tracks = new MutableLiveData<>(new ArrayList<>());
        tracks.postValue(new ArrayList<>(Arrays.asList(t1)));
    }

    public MutableLiveData<ArrayList<Track>> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks.setValue(tracks);
    }
}
