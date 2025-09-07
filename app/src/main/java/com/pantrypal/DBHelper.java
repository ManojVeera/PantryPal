package com.pantrypal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "PantryPal.db";
    private static final int DB_VERSION = 2;

    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_TYPE = "user_type"; // customer or seller

    private static final String TABLE_PRODUCTS = "products";
    private static final String COL_PRODUCT_ID = "id";
    private static final String COL_PRODUCT_NAME = "name";
    private static final String COL_PRODUCT_DESC = "description";
    private static final String COL_PRODUCT_PRICE = "price";
    private static final String COL_PRODUCT_IMAGE_URL = "image_url";
    private static final String COL_PRODUCT_CATEGORY = "category";
    private static final String COL_PRODUCT_SELLER_EMAIL = "seller_email";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_TYPE + " TEXT DEFAULT 'customer')";
        db.execSQL(createUsers);

        String createCurrentUser = "CREATE TABLE current_user (email TEXT PRIMARY KEY)";
        db.execSQL(createCurrentUser);

        String createProducts = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PRODUCT_NAME + " TEXT, " +
                COL_PRODUCT_DESC + " TEXT, " +
                COL_PRODUCT_PRICE + " REAL, " +
                COL_PRODUCT_IMAGE_URL + " TEXT, " +
                COL_PRODUCT_CATEGORY + " TEXT, " +
                COL_PRODUCT_SELLER_EMAIL + " TEXT)";
        db.execSQL(createProducts);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS current_user");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    public boolean insertUser(String name, String email, String password, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        String hashedPassword = hashPassword(password);
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, hashedPassword);
        values.put(COL_TYPE, type);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
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

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, hashedPassword});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public String getUserType(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_TYPE + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=?",
                new String[]{email});
        String type = null;
        if (cursor.moveToFirst()) {
            type = cursor.getString(0);
        }
        cursor.close();
        return type;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=?",
                new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public void setLoggedInUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM current_user");
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

    public void clearLoggedInUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM current_user");
        db.close();
    }

    public boolean insertProduct(String name, String description, double price, String imageUrl, String category, String sellerEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME, name);
        values.put(COL_PRODUCT_DESC, description);
        values.put(COL_PRODUCT_PRICE, price);
        values.put(COL_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COL_PRODUCT_CATEGORY, category);
        values.put(COL_PRODUCT_SELLER_EMAIL, sellerEmail);
        long result = db.insert(TABLE_PRODUCTS, null, values);
        return result != -1;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE_URL)));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY)));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COL_PRODUCT_CATEGORY + " FROM " + TABLE_PRODUCTS, null);
        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public List<Product> getMyProducts(String sellerEmail) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PRODUCT_SELLER_EMAIL + "=?", new String[]{sellerEmail});
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE_URL)));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY)));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }
}