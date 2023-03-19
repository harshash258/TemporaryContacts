package com.tcd.ghostlyContact.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tcd.ghostlyContact.models.Contacts;

@Dao
public interface ContactsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Contacts contact);

    @Query("SELECT * FROM GhostlyContacts WHERE id = :id")
    Contacts getContactById(int id);

    @Update
    void update(Contacts contact);
}
