package ch.disappointment.WalkoutCompanion.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import org.osmdroid.util.GeoPoint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.disappointment.WalkoutCompanion.persistence.model.Track;

public class TracksDaoService extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "walkout_companion";

    public static final String TRACKS_TABLE_NAME = "user_tracks";
    public static final String TRACKS_KEY_ID = "id";
    public static final String TRACKS_KEY_USER = "user";
    public static final String TRACKS_KEY_NAME = "name";

    public static final String POINTS_TABLE_NAME = "geopoints";
    public static final String POINTS_KEY_TIME = "time";
    public static final String POINTS_KEY_LAT = "lat";
    public static final String POINTS_KEY_LON = "lon";
    public static final String POINTS_FK_TRACK = "track_id";

    public static final String CREATE_TRACKS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS "
            + TRACKS_TABLE_NAME + " ("
            + TRACKS_KEY_ID + " INTEGER PRIMARY KEY, "
            + TRACKS_KEY_NAME + " TEXT, "
            + TRACKS_KEY_USER + " TEXT);";

    public static final String CREATE_POINTS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS "
            + POINTS_TABLE_NAME + " ("
            + POINTS_KEY_TIME + " INTEGER PRIMARY KEY, "
            + POINTS_KEY_LAT + " REAL, "
            + POINTS_KEY_LON + " REAL, "
            + POINTS_FK_TRACK + " INTEGER, "
            + "FOREIGN KEY (" + POINTS_FK_TRACK + ") references " + TRACKS_TABLE_NAME + "(" + TRACKS_KEY_ID + ") );";

    public TracksDaoService(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRACKS_TABLE_SQL);
        db.execSQL(CREATE_POINTS_TABLE_SQL);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL(CREATE_TRACKS_TABLE_SQL);
        db.execSQL(CREATE_POINTS_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public Track getTrack(Context ctx, Long trackId) {
        TracksDaoService service = new TracksDaoService(ctx);
        SQLiteDatabase database = service.getWritableDatabase();

        String trackSelection = "id=?";
        String[] trackSelectionArgs = new String[]{trackId.toString()};
        String[] trackColumns = new String[]{
                TRACKS_KEY_NAME
        };

        Cursor tracksCursor = database.query(TRACKS_TABLE_NAME,
                trackColumns, trackSelection, trackSelectionArgs,
                null, null, null);

        if(tracksCursor.getCount() == 0){
            return null;
        }
        tracksCursor.moveToFirst();
        String trackName = tracksCursor.getString(0);
        tracksCursor.close();

        String pointsSelection = "track_id=?";
        String[] pointsSelectionArgs = new String[]{trackId.toString()};
        String[] pointsColumns = new String[]{
                POINTS_KEY_LAT,
                POINTS_KEY_LON,
                POINTS_KEY_TIME
        };

        Cursor pointsCursor = database.query(
                POINTS_TABLE_NAME,
                pointsColumns, pointsSelection, pointsSelectionArgs,
                null, null, null);

        Track t = new Track();
        ArrayList<Track.TrackNode> points = new ArrayList<>();
        if (pointsCursor.getCount() != 0) {
            pointsCursor.moveToFirst();
            for (int i = 0; i < pointsCursor.getCount(); i++) {
                double lat = pointsCursor.getDouble(0);
                double lon = pointsCursor.getDouble(1);
                long time = pointsCursor.getLong(2);

                GeoPoint gp = new GeoPoint(lat, lon);
                Track.TrackNode node = new Track.TrackNode();
                node.setPoint(gp);
                node.setTime(Instant.ofEpochSecond(time));
                points.add(node);
                pointsCursor.moveToNext();
            }
        }

        pointsCursor.close();
        database.close();

        t.setPoints(points
                .stream()
                .sorted((p1, p2) -> (int) (p1.getTime().getEpochSecond() - p2.getTime().getEpochSecond()))
                .collect(Collectors.toList())
        );

        t.setId(trackId);
        t.setName(trackName);

        return t;
    }

    public ArrayList<Track> listTracks(Context ctx, String username) {
        TracksDaoService service = new TracksDaoService(ctx);
        SQLiteDatabase database = service.getWritableDatabase();

        String selection = "user=?";

        String[] selectionArgs = new String[]{username};
        String[] columns = new String[]{
                TRACKS_KEY_ID
        };

        Cursor cursor = database.query(
                TRACKS_TABLE_NAME,
                columns, selection, selectionArgs,
                null, null, null);

        ArrayList<Track> tracks = new ArrayList<>();
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                long trackId = cursor.getInt(0);
                Track t = this.getTrack(ctx, trackId);
                tracks.add(t);
                cursor.moveToNext();
            }
        }

        cursor.close();
        database.close();

        return tracks;
    }

    // returns the ID
    public Track createEmptyTrack(Context ctx, String username, String trackName) {
        TracksDaoService service = new TracksDaoService(ctx);
        SQLiteDatabase database = service.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRACKS_KEY_USER, username);
        values.put(TRACKS_KEY_NAME, trackName);

        long id = database.insert(TRACKS_TABLE_NAME, null, values);

        database.close();
        return getTrack(ctx, id);
    }

    public void addPoint(Context ctx, Long trackId, Instant now, GeoPoint gp) {
        TracksDaoService service = new TracksDaoService(ctx);
        SQLiteDatabase database = service.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(POINTS_KEY_LAT, gp.getLatitude());
        cv.put(POINTS_KEY_LON, gp.getLongitude());
        cv.put(POINTS_KEY_TIME, now.getEpochSecond());
        cv.put(POINTS_FK_TRACK, trackId);

        database.insert(POINTS_TABLE_NAME, null, cv);

        database.close();
    }
}
