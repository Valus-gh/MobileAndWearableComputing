package ch.disappointment.WalkoutCompanion.persistence.model;

import com.anychart.scales.Geo;

import org.osmdroid.util.GeoPoint;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public class Track {
    public static class TrackNode {
        private GeoPoint point;
        private Instant time;

        public GeoPoint getPoint() {
            return point;
        }

        public void setPoint(GeoPoint point) {
            this.point = point;
        }

        public Instant getTime() {
            return time;
        }

        public void setTime(Instant time) {
            this.time = time;
        }
    }

    private Long id;

    private List<TrackNode> points;

    public void setPoints(List<TrackNode> points) {
        this.points = points;
    }

    public List<TrackNode> getPoints() {
        return points;
    }

    public void addPoint(TrackNode point) {
        this.points.add(point);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant startsAt() {
        Instant minDate = null;
        for(TrackNode point: points){
            if(minDate == null || point.time.isBefore(minDate))
                minDate = point.time;
        }

        return minDate;
    }

    public Instant endsAt() {
        Instant maxDate = null;
        for(TrackNode point: points){
            if(maxDate == null || point.time.isAfter(maxDate))
                maxDate = point.time;
        }

        return maxDate;
    }
}