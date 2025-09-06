package com.pantrypal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "PantryPal.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    public void setLoggedInUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM current_user"); // remove old login if any
        ContentValues values = new ContentValues();
        values.put("email", email);
        db.insert("current_user", null, values);
        db.close();
    }
    public String getLoggedInUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT email FROM current_user LIMIT 1", null);
        String email = null;
        if (cursor.moveToFirst()) {
            email = cursor.getString(0);
        }
        cursor.close();
        return email;
    }

    // Clear logged-in user on sign out
    public void clearLoggedInUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM current_user");
        db.close();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createTable);

        // Table to store currently logged-in user
        String createCurrentUser = "CREATE TABLE current_user (email TEXT PRIMARY KEY)";
        db.execSQL(createCurrentUser);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Insert user
    // Insert user with hashed password
    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 🔐 Hash password before storing
        String hashedPassword = hashPassword(password);

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", hashedPassword);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Utility: Hash password with SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // Check login
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password); // hash input password
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, hashedPassword});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }



    // Check email already exists
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=?",
                new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

}
