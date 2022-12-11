package ch.disappointment.WalkoutCompanion.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ch.disappointment.WalkoutCompanion.api.ApiService;
import ch.disappointment.WalkoutCompanion.persistence.model.DailySteps;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DailyStepsLocalDaoService extends SQLiteOpenHelper implements DailyStepsDaoService<DailySteps> {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "walkout_companion";

    public static final String TABLE_NAME = "daily_user_steps";
    public static final String KEY_ID = "id";
    public static final String KEY_USER = "user";
    public static final String KEY_STEPS = "steps";
    public static final String KEY_DAY = "day";

    public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " ("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_USER + " TEXT, "
            + KEY_DAY + " TEXT, "
            + KEY_STEPS + " INTEGER);";

    public DailyStepsLocalDaoService(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }

    @Override
    public void get(Context context, String day, Consumer<DailySteps> consumer){

        DailyStepsLocalDaoService service = new DailyStepsLocalDaoService(context);
        SQLiteDatabase database = service.getWritableDatabase();

        String[] columns = new String[]{
                DailyStepsLocalDaoService.KEY_DAY,
                DailyStepsLocalDaoService.KEY_STEPS
        };

        String selection = "user=? AND day=?";

        String[] selectionArgs = new String[]{
                ApiService.getInstance(context).getLoggedUser().getUsername(),
                day
        };

        Cursor cursor = database.query(
                DailyStepsLocalDaoService.TABLE_NAME,
                columns, selection, selectionArgs,
                null, null, null);

        if(cursor.getCount() == 0) {
            consumer.accept(null);
            return;
        }

        cursor.moveToFirst();

        DailySteps record = new DailySteps(
                cursor.getInt(1),
                cursor.getString(0));

        cursor.close();
        database.close();

        Log.d("STORED STEPS: ", record.toString());

        consumer.accept(record);
    }

    @Override
    public void getAll(Context context, Consumer<List<DailySteps>> onSuccess) {

        DailyStepsLocalDaoService service = new DailyStepsLocalDaoService(context);
        SQLiteDatabase database = service.getReadableDatabase();

        List<DailySteps> list = new ArrayList<>();

        String[] columns = new String[]{
                DailyStepsLocalDaoService.KEY_DAY,
                DailyStepsLocalDaoService.KEY_STEPS
        };

        String selection = "user=?";

        String[] selectionArgs = new String[]{
                ApiService.getInstance(context).getLoggedUser().getUsername()
        };

        String orderBy = "id desc";

        Cursor cursor = database.query(
                DailyStepsLocalDaoService.TABLE_NAME,
                columns, selection, selectionArgs,
                null, null, orderBy);

        cursor.moveToFirst();

        for(int i = 0; i < cursor.getCount(); i++){
            list.add(new DailySteps(cursor.getInt(1), cursor.getString(0)));
            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        Log.d("STORED STEPS: ", String.valueOf(list));

        onSuccess.accept(list);
    }

    @Override
    public void getAllExceptUser(Context context, Consumer<List<DailySteps>> onSuccess) {

        DailyStepsLocalDaoService service = new DailyStepsLocalDaoService(context);
        SQLiteDatabase database = service.getReadableDatabase();

        List<DailySteps> list = new ArrayList<>();

        String[] columns = new String[]{
                DailyStepsLocalDaoService.KEY_DAY,
                DailyStepsLocalDaoService.KEY_STEPS
        };

        String selection = "user!=?";

        String[] selectionArgs = new String[]{
                ApiService.getInstance(context).getLoggedUser().getUsername()
        };

        String orderBy = "id desc";

        Cursor cursor = database.query(
                DailyStepsLocalDaoService.TABLE_NAME,
                columns, selection, selectionArgs,
                null, null, orderBy);

        cursor.moveToFirst();

        for(int i = 0; i < cursor.getCount(); i++){
            list.add(new DailySteps(cursor.getInt(1), cursor.getString(0)));
            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        Log.d("STORED STEPS: ", String.valueOf(list));

        onSuccess.accept(list);
    }

    @Override
    public void insert(Context context, DailySteps record, Runnable onDone){

        DailyStepsLocalDaoService service = new DailyStepsLocalDaoService(context);
        SQLiteDatabase database = service.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DailyStepsLocalDaoService.KEY_USER, ApiService.getInstance(context).getLoggedUser().getUsername());
        values.put(DailyStepsLocalDaoService.KEY_DAY, record.date);
        values.put(DailyStepsLocalDaoService.KEY_STEPS, record.steps);

        long id = database.insert(DailyStepsLocalDaoService.TABLE_NAME, null, values);

        database.close();

        onDone.run();
    }

    @Override
    public void update(Context context, DailySteps record, Runnable onDone){

        DailyStepsLocalDaoService service = new DailyStepsLocalDaoService(context);
        SQLiteDatabase database = service.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DailyStepsLocalDaoService.KEY_DAY, record.date);
        values.put(DailyStepsLocalDaoService.KEY_STEPS, record.steps);

        String selection = "user=? AND day=?";

        String[] selectionArgs = new String[]{
                ApiService.getInstance(context).getLoggedUser().getUsername(),
                record.date
        };

        long rows = database.update(DailyStepsLocalDaoService.TABLE_NAME, values, selection, selectionArgs);

        database.close();

        onDone.run();
    }

    @Override
    public void delete(Context context, DailySteps record, Runnable onDone){

        DailyStepsLocalDaoService service = new DailyStepsLocalDaoService(context);
        SQLiteDatabase database = service.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DailyStepsLocalDaoService.KEY_DAY, record.date);
        values.put(DailyStepsLocalDaoService.KEY_STEPS, record.steps);

        String selection = "user=? AND day=?";

        String[] selectionArgs = new String[]{
                ApiService.getInstance(context).getLoggedUser().getUsername(),
                record.date
        };

        long rows = database.delete(DailyStepsLocalDaoService.TABLE_NAME, selection, selectionArgs);

        database.close();

        onDone.run();
    }
}
