package com.tcd.ghostlyContact

import androidx.lifecycle.LiveData
import com.tcd.ghostlyContact.dao.ContactsDAO
import com.tcd.ghostlyContact.models.Contacts


class ContactsRepository(private val gymMemberDao: ContactsDAO) {

    val readAllData: LiveData<List<Contacts>> = gymMemberDao.allGymMembers

    fun addUser(user: Contacts) {
        gymMemberDao.insert(user)
    }
}