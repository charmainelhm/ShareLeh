package com.example.shareleh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "Shareleh";
    public static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqStatement = "CREATE TABLE users (username TEXT PRIMARY KEY, full_name TEXT, email TEXT, password TEXT);";
        db.execSQL(sqStatement);

        ContentValues testUser1 = new ContentValues();
        testUser1.put("username", "adamteo123");
        testUser1.put("full_name", "Adam Teo");
        testUser1.put("email", "adamteo123@gmail.com");
        testUser1.put("password", "adamteo123");

        ContentValues testUser2 = new ContentValues();
        testUser2.put("username", "carrie_lim");
        testUser2.put("full_name", "Carrie Lim");
        testUser2.put("email", "carrie_lim@gmail.com");
        testUser2.put("password", "carrielim123");

        long testUser1Id = db.insert("users", null, testUser1);
        long testUser2Id = db.insert("users", null, testUser2);

//        String sqStatement2 = "CREATE TABLE listings (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, owner TEXT, image_url TEXT, collect_before TEXT, location TEXT, qtn INTEGER, reserved_by TEXT, reserved INTEGER NOT NULL DEFAULT 0);";
//        db.execSQL(sqStatement2);
//
//        initListings(db);
    }

    private void initListings(SQLiteDatabase db) {
        ContentValues cabbage = new ContentValues();
        cabbage.put("title", "Cabbage");
        cabbage.put("description", "Cabbage to give away");
        cabbage.put("owner", "carrie_lim");
        cabbage.put("image_url", "https://cdn-prod.medicalnewstoday.com/content/images/articles/284/284823/one-big-cabbage.jpg");
        cabbage.put("collect_before", "27-12-2022");
        cabbage.put("location", "Blk 123 Cabbage Street");
        cabbage.put("qtn", 3);
        cabbage.put("reserved_by", "");

        ContentValues carrot = new ContentValues();
        carrot.put("title", "Carrots");
        carrot.put("description", "Herbaceous biennial plant of the Apiaceae family that produces an edible taproot. Among common varieties root shapes range from globular to long, with lower ends blunt to pointed.");
        carrot.put("owner", "benjamintan123");
        carrot.put("image_url", "https://images.immediate.co.uk/production/volatile/sites/30/2021/11/carrots-953655d.jpg");
        carrot.put("collect_before", "02-11-2022");
        carrot.put("location", "Blk 456 Carrot Road");
        carrot.put("qtn", 6);
        carrot.put("reserved_by", "");

        db.insert("listings", null, cabbage);
        db.insert("listings", null, carrot);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        switch(i) {
            case 1:
                switch(i1) {
                    case 2:
                        String sqStatement2 = "CREATE TABLE listings (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, owner TEXT, image_url TEXT, collect_before TEXT, location TEXT, qtn INTEGER, reserved_by TEXT, reserved INTEGER NOT NULL DEFAULT 0);";
                        db.execSQL(sqStatement2);
                        initListings(db);
                        break;
                }
                break;
            default:
                break;
        }

    }
}
