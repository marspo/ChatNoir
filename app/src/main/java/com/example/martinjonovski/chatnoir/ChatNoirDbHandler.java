package com.example.martinjonovski.chatnoir;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Jonovski on 12/6/2017.
 */

public class ChatNoirDbHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "userkeys.db";

    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts";

    // Contacts Table Columns names
    private static final String KEY_NAME = "_id";
    private static final String KEY_PH_NO = "key";

    public ChatNoirDbHandler(Context context, String name,
                             SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_NAME + " TEXT PRIMARY KEY," + KEY_PH_NO + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName()); // myUid + friendUid
        values.put(KEY_PH_NO, contact.getKey()); // random shared string
        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    public Contact getContact(String id) {
        String query = "Select * FROM " + TABLE_CONTACTS + " WHERE " + KEY_NAME + " =  \"" + id + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        Contact product = new Contact();

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            product.setName(cursor.getString(0));
            product.setKey(cursor.getString(1));

            cursor.close();
        } else {
            product = null;
        }
        db.close();
        return product;
    }

    // Getting All Contacts
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setName(cursor.getString(0));
                contact.setKey(cursor.getString(1));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    // Updating single contact
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PH_NO, contact.getKey());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_NAME + " = ?",
                new String[]{String.valueOf(contact.getName())});
    }

    // Deleting single contact
    public boolean deleteContact(String productname) {

        boolean result = false;

        String query = "Select * FROM " + TABLE_CONTACTS + " WHERE " + KEY_NAME + " =  \"" + productname + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        Contact product = new Contact();
        if (cursor.moveToFirst()) {
            product.setName(cursor.getString(0));
            db.delete(TABLE_CONTACTS, KEY_NAME + " = ?",
                    new String[]{String.valueOf(product.getName())});
            cursor.close();
            result = true;
        }
        db.close();
        return result;
    }
}