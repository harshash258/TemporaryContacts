package com.mycompany.createtemporarycontact.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mycompany.createtemporarycontact.R;
import com.mycompany.createtemporarycontact.activity.DisplayContactListActivity;
import com.mycompany.createtemporarycontact.activity.EditContactActivity;
import com.mycompany.createtemporarycontact.model.Contacts;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements Filterable {

    public static final String TAG = "";
    List<Contacts> mList;
    List<Contacts> copyList;
    Context context;
    Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contacts> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(copyList);
            } else {
                String filterText = constraint.toString().toLowerCase().trim();

                for (Contacts contacts : copyList) {
                    if (contacts.getName().toLowerCase().contains(filterText)) {
                        filteredList.add(contacts);

                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList.clear();
            mList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public ContactsAdapter(List<Contacts> mList, Context context) {
        this.mList = mList;
        this.context = context;
        copyList = new ArrayList<>(mList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.display_contacts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Contacts contacts = mList.get(position);
        holder.name.setText(contacts.getName());
        holder.phoneNumber.setText(contacts.getNumber());
        holder.delete.setOnClickListener(v -> {
            String names = contacts.getName();
            String phone = contacts.getNumber();
            Log.d(TAG, names + " ------ " + phone);
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
            builder.setIcon(R.drawable.ic_baseline_delete_24);
            builder.setTitle("Delete Contact!!!");
            builder.setMessage("Are you sure you want to delete this Contact?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                deleteContact(context, contacts.getNumber(), contacts.getName());
                dialog.dismiss();
                Intent intent = new Intent(context, DisplayContactListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        holder.edit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditContactActivity.class);
            intent.putExtra("name", contacts.getName());
            intent.putExtra("phone", contacts.getNumber());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    private void deleteContact(Context ctx, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        try (Cursor cur = ctx.getContentResolver().query(contactUri,
                null, null, null, null)) {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                        Toast.makeText(ctx, "Contact Deleted", Toast.LENGTH_SHORT).show();
                    }

                } while (cur.moveToNext());

            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    static public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phoneNumber;
        ImageButton delete, edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            phoneNumber = itemView.findViewById(R.id.contactNumber);
            delete = itemView.findViewById(R.id.deleteContact);
            edit = itemView.findViewById(R.id.editContact);
        }
    }

}
