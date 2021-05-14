package com.mycompany.createtemporarycontact.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mycompany.createtemporarycontact.R;
import com.mycompany.createtemporarycontact.adapter.ContactsAdapter;
import com.mycompany.createtemporarycontact.model.Contacts;

import java.util.ArrayList;
import java.util.List;

public class DisplayContactListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Contacts> contactsList;
    ContactsAdapter adapter;
    FloatingActionButton button;

    TextView noPermission, textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact_list);

        setTitle("ConTemp - Manage Contacts");

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        button = findViewById(R.id.floating_action_button);
        noPermission = findViewById(R.id.noPermission);
        textView = findViewById(R.id.text);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        contactsList = new ArrayList<>();
        button.setOnClickListener(v -> onBackPressed());
        getContactList();
    }

    public void getContactList() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    1);
        } else {
            noPermission.setVisibility(View.GONE);
            String[] PROJECTION = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, // Honeycomb+ should use this
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    PROJECTION, null, null,
                    "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");
            contactsList.clear();

            if (!cursor.moveToNext())
                textView.setVisibility(View.VISIBLE);

            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Contacts contacts = new Contacts(name, number);
                contactsList.add(contacts);

                Log.d("Size", String.valueOf(contactsList.size()));
                adapter = new ContactsAdapter(contactsList, getApplicationContext());
                recyclerView.setAdapter(adapter);


                adapter.notifyDataSetChanged();

            }
            cursor.close();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(DisplayContactListActivity.this, CreateContactActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            View view = findViewById(R.id.refresh);
            view.animate().rotation(1080).start();
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                        1);
            } else {
                contactsList.clear();
                getContactList();
                Toast.makeText(this, "Contact List Updated", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

}