package com.tcd.ghostlyContact.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.createtemporarycontact.R
import com.tcd.ghostlyContact.models.Contacts
import java.lang.String


class DeletedContactAdapter(contacts: List<Contacts>) :
    RecyclerView.Adapter<DeletedContactAdapter.ViewHolder>() {

    var gymMembers = emptyList<Contacts>()
    var context: Context? = null

    init {
        this.gymMembers = contacts
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_deleted_contacts, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = gymMembers[position]
        holder.nameTextView.text = contact.name
        holder.phoneNumber.text = contact.phoneNumber

        val diffMillis: Long = contact.time - System.currentTimeMillis()
        if (diffMillis <= 0) {
            holder.timeLeft.text = "00:00:00"
            holder.createAgain.visibility = View.VISIBLE
            holder.createAgain.isEnabled = true
        } else {
            val seconds: Long = diffMillis / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val timeString =
                String.format(
                    "%d days %02d:%02d:%02d",
                    days,
                    hours % 24,
                    minutes % 60,
                    seconds % 60
                )
            holder.timeLeft.text = timeString
        }
        holder.createAgain.setOnClickListener {
            buildCustomDialog(context!!, contact.name, contact.phoneNumber)
        }

    }

    fun buildCustomDialog(context: Context, name: kotlin.String, phoneNumber: kotlin.String) {
        val builder = AlertDialog.Builder(context)
            .create()
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.alert_create_again, null)
        val permanent = view.findViewById<Button>(R.id.permanent)
        val temporary = view.findViewById<Button>(R.id.temporary)
        val nameTxt = view.findViewById<TextView>(R.id.name)
        val phone = view.findViewById<TextView>(R.id.phone)
        nameTxt.text = name
        phone.text = phoneNumber
        builder.setView(view)
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    override fun getItemCount(): Int {
        Log.d("Size", gymMembers.size.toString())
        return gymMembers.size
    }

    fun setData(user: List<Contacts>) {
        this.gymMembers = user
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name)
        val phoneNumber: TextView = itemView.findViewById(R.id.phoneNumber)
        val timeLeft: TextView = itemView.findViewById(R.id.timeLeft)
        val createAgain: Button = itemView.findViewById(R.id.createAgain)
    }
}