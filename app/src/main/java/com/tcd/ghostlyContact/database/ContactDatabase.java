package com.tcd.ghostlyContact.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tcd.ghostlyContact.dao.ContactsDAO;
import com.tcd.ghostlyContact.models.Contacts;

@Database(entities = {Contacts.class}, version = 1, exportSchema = false)
public abstract class ContactDatabase extends RoomDatabase {
    private static ContactDatabase instance;

    public static synchronized ContactDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            ContactDatabase.class, "contacts")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract ContactsDAO getContactsDAO();


}
