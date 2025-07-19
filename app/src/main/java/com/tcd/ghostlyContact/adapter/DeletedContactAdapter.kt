package com.tcd.ghostlyContact.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.createtemporarycontact.R
import com.tcd.ghostlyContact.models.Contacts


class DeletedContactAdapter(contacts: List<Contacts>, val clickListener: ClickListener) :
    RecyclerView.Adapter<DeletedContactAdapter.ViewHolder>() {

    companion object {
        var mClickListener: ClickListener? = null
    }

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
        mClickListener = clickListener
        val contact = gymMembers[position]
        holder.nameTextView.text = contact.name
        holder.phoneNumber.text = contact.phoneNumber
        holder.createAgain.visibility = View.INVISIBLE
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
            buildCustomDialog(
                context!!,
                position,
                contact.name,
                contact.phoneNumber,
                mClickListener!!
            )
        }

    }

    private fun fillSpinner(context: Context, time: Spinner) {
        val timeSpinner = ArrayList<String>()
        timeSpinner.add("1 min")
        timeSpinner.add("5 min")
        timeSpinner.add("10 min")
        timeSpinner.add("30 min")
        timeSpinner.add("60 min")
        timeSpinner.add("1 day")
        timeSpinner.add("1 Week")
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_item, timeSpinner
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        time.adapter = arrayAdapter
    }

    fun buildCustomDialog(
        context: Context,
        position: Int,
        name: String,
        phoneNumber: String,
        mClickListener: ClickListener
    ) {
        val builder = AlertDialog.Builder(context).create()
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.alert_create_again, null)
        val permanent = view.findViewById<Button>(R.id.permanent)
        val temporary = view.findViewById<Button>(R.id.temporary)
        val time = view.findViewById<Spinner>(R.id.time)
        val close = view.findViewById<ImageView>(R.id.close)
        fillSpinner(context, time)
        val nameTxt = view.findViewById<TextView>(R.id.name)
        val phone = view.findViewById<TextView>(R.id.phone)
        nameTxt.text = name
        phone.text = phoneNumber

        close.setOnClickListener {
            builder.dismiss()
        }

        permanent.setOnClickListener {
            mClickListener.createPermanent(position, name, phoneNumber)
            builder.dismiss()
        }
        temporary.setOnClickListener {
            mClickListener.createTemporary(
                position,
                name,
                phoneNumber,
                time.selectedItem.toString()
            )
            builder.dismiss()
        }
        builder.setView(view)
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    interface ClickListener {
        fun createTemporary(position: Int, name: String, phoneNumber: String, time: String)
        fun createPermanent(position: Int, name: String, phoneNumber: String)
    }

    override

    fun getItemCount(): Int {
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