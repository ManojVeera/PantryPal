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
    private static final int DB_VERSION = 5;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_TYPE = "user_type";

    // Products table
    private static final String TABLE_PRODUCTS = "products";
    private static final String COL_PRODUCT_ID = "id";
    private static final String COL_PRODUCT_NAME = "name";
    private static final String COL_PRODUCT_DESC = "description";
    private static final String COL_PRODUCT_PRICE = "price";
    private static final String COL_PRODUCT_IMAGE_URL = "image_url";
    private static final String COL_PRODUCT_CATEGORY = "category";
    private static final String COL_PRODUCT_SELLER_EMAIL = "seller_email";
    private static final String COL_PRODUCT_QUANTITY = "quantity";

    // Cart table
    private static final String TABLE_CART = "cart_items";
    private static final String COL_CART_ID = "cart_id";
    private static final String COL_CART_USER_EMAIL = "user_email";
    private static final String COL_CART_PRODUCT_ID = "product_id";
    private static final String COL_CART_QUANTITY = "quantity";

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
                COL_TYPE + " TEXT, " +
                "phone TEXT, " +
                "address TEXT)";
        db.execSQL(createUsers);

        String createProducts = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PRODUCT_NAME + " TEXT, " +
                COL_PRODUCT_DESC + " TEXT, " +
                COL_PRODUCT_PRICE + " REAL, " +
                COL_PRODUCT_IMAGE_URL + " TEXT, " +
                COL_PRODUCT_CATEGORY + " TEXT, " +
                COL_PRODUCT_SELLER_EMAIL + " TEXT, " +
                COL_PRODUCT_QUANTITY + " INTEGER)";
        db.execSQL(createProducts);

        String createCartTable = "CREATE TABLE " + TABLE_CART + " (" +
                COL_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CART_USER_EMAIL + " TEXT, " +
                COL_CART_PRODUCT_ID + " INTEGER, " +
                COL_CART_QUANTITY + " INTEGER)";
        db.execSQL(createCartTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + COL_PRODUCT_QUANTITY + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_TYPE + " TEXT");
        }
        if (oldVersion < 4) {
            String createCartTable = "CREATE TABLE IF NOT EXISTS " + TABLE_CART + " (" +
                    COL_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CART_USER_EMAIL + " TEXT, " +
                    COL_CART_PRODUCT_ID + " INTEGER, " +
                    COL_CART_QUANTITY + " INTEGER)";
            db.execSQL(createCartTable);
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN phone TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN address TEXT");
        }
    }

    // ======== User Methods ========
    public Cursor getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT name, email, phone, address FROM " + TABLE_USERS + " WHERE email=?", new String[]{email});
    }

    public boolean updateUserProfile(String email, String name, String phone, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put("phone", phone);
        values.put("address", address);
        int rows = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    public boolean insertUser(String name, String email, String password, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, hashPassword(password));
        values.put(COL_TYPE, type);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, hashPassword(password)}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public String getUserType(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_TYPE + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=?", new String[]{email});
        String type = null;
        if (cursor.moveToFirst()) type = cursor.getString(0);
        cursor.close();
        return type;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + "=?", new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
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

    // ======== Product Methods ========
    public boolean insertProduct(String name, String description, double price, String imageUrl, String category, int quantity, String sellerEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME, name);
        values.put(COL_PRODUCT_DESC, description);
        values.put(COL_PRODUCT_PRICE, price);
        values.put(COL_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COL_PRODUCT_CATEGORY, category);
        values.put(COL_PRODUCT_QUANTITY, quantity);
        values.put(COL_PRODUCT_SELLER_EMAIL, sellerEmail);
        return db.insert(TABLE_PRODUCTS, null, values) != -1;
    }

    public boolean updateProduct(int id, String name, String description, double price, String imageUrl, String category, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PRODUCT_NAME, name);
        values.put(COL_PRODUCT_DESC, description);
        values.put(COL_PRODUCT_PRICE, price);
        values.put(COL_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COL_PRODUCT_CATEGORY, category);
        values.put(COL_PRODUCT_QUANTITY, quantity);
        int rowsAffected = db.update(TABLE_PRODUCTS, values, COL_PRODUCT_ID + "=?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }
    // In DBHelper.java
    public List<Product> getMyProducts(String sellerEmail) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PRODUCT_SELLER_EMAIL + "=?",
                new String[]{sellerEmail}
        );

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE_URL)));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY)));
                product.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_QUANTITY)));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }


    public boolean deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PRODUCTS, COL_PRODUCT_ID + "=?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PRODUCT_ID + "=?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            Product product = new Product();
            product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)));
            product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)));
            product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)));
            product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)));
            product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE_URL)));
            product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY)));
            product.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_QUANTITY)));
            cursor.close();
            return product;
        }
        cursor.close();
        return null;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE_URL)));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY)));
                product.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_QUANTITY)));
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
            do { categories.add(cursor.getString(0)); } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PRODUCT_CATEGORY + "=?", new String[]{category});
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID)));
                product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)));
                product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESC)));
                product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE_URL)));
                product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_CATEGORY)));
                product.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_QUANTITY)));
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    // ======== Cart Methods ========
    public void addToCart(String userEmail, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART + " WHERE " + COL_CART_USER_EMAIL + "=? AND " + COL_CART_PRODUCT_ID + "=?", new String[]{userEmail, String.valueOf(productId)});
        if (cursor.moveToFirst()) {
            int currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_QUANTITY));
            ContentValues values = new ContentValues();
            values.put(COL_CART_QUANTITY, currentQuantity + 1);
            db.update(TABLE_CART, values, COL_CART_ID + "=?", new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_ID)))});
        } else {
            ContentValues values = new ContentValues();
            values.put(COL_CART_USER_EMAIL, userEmail);
            values.put(COL_CART_PRODUCT_ID, productId);
            values.put(COL_CART_QUANTITY, 1);
            db.insert(TABLE_CART, null, values);
        }
        cursor.close();
    }

    public List<CartItem> getCartItems(String userEmail) {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.*, p." + COL_PRODUCT_NAME + ", p." + COL_PRODUCT_PRICE + ", p." + COL_PRODUCT_IMAGE_URL +
                " FROM " + TABLE_CART + " c INNER JOIN " + TABLE_PRODUCTS + " p ON c." + COL_CART_PRODUCT_ID + " = p." + COL_PRODUCT_ID +
                " WHERE c." + COL_CART_USER_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{userEmail});
        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem();
                item.setCartId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_ID)));
                item.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_PRODUCT_ID)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_QUANTITY)));
                item.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME)));
                item.setProductPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE)));
                item.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE_URL)));
                cartItems.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cartItems;
    }

    public void updateCart(String userEmail, int productId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CART_QUANTITY, newQuantity);
        db.update(TABLE_CART, values, COL_CART_USER_EMAIL + "=? AND " + COL_CART_PRODUCT_ID + "=?", new String[]{userEmail, String.valueOf(productId)});
    }

    public void removeFromCart(String userEmail, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COL_CART_USER_EMAIL + "=? AND " + COL_CART_PRODUCT_ID + "=?", new String[]{userEmail, String.valueOf(productId)});
    }

    public void clearCart(String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COL_CART_USER_EMAIL + "=?", new String[]{userEmail});
    }

    public int getCartItemCount(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_CART_QUANTITY + ") FROM " + TABLE_CART + " WHERE " + COL_CART_USER_EMAIL + "=?", new String[]{userEmail});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getCartQuantity(String userEmail, int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_CART_QUANTITY + " FROM " + TABLE_CART + " WHERE " + COL_CART_USER_EMAIL + "=? AND " + COL_CART_PRODUCT_ID + "=?", new String[]{userEmail, String.valueOf(productId)});
        int qty = 0;
        if (cursor.moveToFirst()) qty = cursor.getInt(0);
        cursor.close();
        return qty;
    }

    // ======== Cart Aliases for Compatibility ========
    public void updateCartItemQuantity(String userEmail, int productId, int newQuantity) {
        updateCart(userEmail, productId, newQuantity);
    }

    public void removeCartItem(String userEmail, int productId) {
        removeFromCart(userEmail, productId);
    }
}
