package ch.disappointment.WalkoutCompanion.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ch.disappointment.WalkoutCompanion.persistence.model.User;

import java.util.function.Consumer;

public class TokensDaoService extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "walkout_companion";

    public static final String TOKENS_TABLE_NAME = "user_tokens";
    public static final String TOKENS_KEY_ID = "username";
    public static final String TOKENS_KEY_TOKEN = "token";

    public static final String CREATE_TOKENS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS "
            + TOKENS_TABLE_NAME + " ("
            + TOKENS_KEY_ID + " TEXT PRIMARY KEY, "
            + TOKENS_KEY_TOKEN + " TEXT); ";

    public TokensDaoService(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TOKENS_TABLE_SQL);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL(CREATE_TOKENS_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void getLastUserExcept(Context context, String username, Consumer<User> consumer) {
        TokensDaoService service = new TokensDaoService(context);
        SQLiteDatabase database = service.getReadableDatabase();

        String selection = "username!=?";
        String[] selectionArgs = new String[]{username};

        String[] columns = new String[]{TOKENS_KEY_ID, TOKENS_KEY_TOKEN};
        Cursor cursor = database.query(
                TOKENS_TABLE_NAME,
                columns, selection, selectionArgs, null, null, null, "1"
        );

        if (cursor.getCount() == 0) {
            consumer.accept(null);
            cursor.close();
            database.close();
            return;
        }

        cursor.moveToFirst();
        User user = new User(cursor.getString(0), cursor.getString(1));

        cursor.close();
        database.close();

        Log.d("Read user: ", user.toString());

        consumer.accept(user);
    }

    public void getUser(Context context, String username, Consumer<User> consumer) {
        TokensDaoService service = new TokensDaoService(context);
        SQLiteDatabase database = service.getReadableDatabase();

        String[] columns = new String[]{TOKENS_KEY_ID, TOKENS_KEY_TOKEN};

        String selection = "username=?";

        String[] selectionArgs = new String[]{username};

        Cursor cursor = database.query(
                TOKENS_TABLE_NAME,
                columns, selection, selectionArgs,
                null, null, null);

        if (cursor.getCount() == 0) {
            consumer.accept(null);
            return;
        }

        cursor.moveToFirst();


        User user = new User(cursor.getString(0), cursor.getString(1));

        cursor.close();
        database.close();

        Log.d("Read user: ", user.toString());

        consumer.accept(user);
    }

    public void setToken(Context context, User user, Runnable onDone) {
        TokensDaoService service = new TokensDaoService(context);
        SQLiteDatabase database = service.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TOKENS_KEY_ID, user.getUsername());
        values.put(TOKENS_KEY_TOKEN, user.getToken());

        Cursor cursor = database.query(TOKENS_TABLE_NAME,
                new String[]{TOKENS_KEY_TOKEN},
                TOKENS_KEY_ID + "=?",
                new String[]{user.getUsername()},
                null, null, null
        );

        if (cursor.getCount() == 0) {
            database.insert(TOKENS_TABLE_NAME, null, values);
        } else {
            database.update(TOKENS_TABLE_NAME, values, TOKENS_KEY_ID + "=?", new String[]{user.getUsername()});
        }

        cursor.close();
        database.close();

        onDone.run();
    }

    public void deleteToken(Context context, String username, Runnable onDone) {
        TokensDaoService service = new TokensDaoService(context);
        SQLiteDatabase database = service.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TOKENS_KEY_ID, username);

        String selection = "user=?";

        String[] selectionArgs = new String[]{username};

        database.delete(TOKENS_TABLE_NAME, selection, selectionArgs);

        database.close();
        onDone.run();
    }

    public void deleteAllExcept(Context context, String username) {
        TokensDaoService service = new TokensDaoService(context);
        SQLiteDatabase database = service.getWritableDatabase();

        String selection = "username!=?";

        String[] selectionArgs = new String[]{username};

        database.delete(TOKENS_TABLE_NAME, selection, selectionArgs);
        database.close();
    }
}
