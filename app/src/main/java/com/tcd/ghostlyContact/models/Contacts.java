package com.tcd.ghostlyContact.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "GhostlyContacts")
public class Contacts {
    @PrimaryKey(autoGenerate = true)
    int id;

    long contactId;

    String name, phoneNumber;

    long time;

    boolean isDeleted;


    public Contacts(long contactId, String name, String phoneNumber, long time, boolean isDeleted) {
        this.contactId = contactId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.time = time;
        this.isDeleted = isDeleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
