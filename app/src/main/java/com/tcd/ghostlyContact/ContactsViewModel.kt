package com.tcd.ghostlyContact

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.tcd.ghostlyContact.database.ContactDatabase
import com.tcd.ghostlyContact.models.Contacts
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ContactsRepository
    val getAllContacts: LiveData<List<Contacts>>

    init {
        val gymMemberDao = ContactDatabase.getInstance(application).contactsDAO
        repository = ContactsRepository(gymMemberDao)
        getAllContacts = repository.readAllData
    }


    fun insert(gymMember: Contacts) = viewModelScope.launch {
        repository.addUser(gymMember)
    }
}