package com.tcd.ghostlyContact.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mycompany.createtemporarycontact.R;
import com.tcd.ghostlyContact.ContactsViewModel;
import com.tcd.ghostlyContact.adapter.DeletedContactAdapter;
import com.tcd.ghostlyContact.models.Contacts;

import java.util.ArrayList;
import java.util.List;

public class ViewDeletedContactActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Contacts> deletedContacts;
    DeletedContactAdapter adapter;
    ContactsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deleted_contact);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deletedContacts = new ArrayList<>();
        adapter = new DeletedContactAdapter(deletedContacts);
        recyclerView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);

        viewModel.getGetAllContacts().observe(this, contacts -> {
            if (contacts != null) {
                adapter.setData(contacts);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deleted_contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.refresh) {
            recreate();
        }
        return super.onOptionsItemSelected(item);
    }
}