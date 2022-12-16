package ch.disappointment.WalkoutCompanion.persistence.model;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class Track {
    private List<GeoPoint> points;

    public void setPoints(List<GeoPoint> points) {
        this.points = points;
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public void addPoint(GeoPoint point) {
        this.points.add(point);
    }
}
