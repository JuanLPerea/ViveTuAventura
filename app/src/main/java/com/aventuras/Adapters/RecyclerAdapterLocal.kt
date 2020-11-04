package com.aventuras.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aventuras.Interfaces.OnItemListClicked
import com.aventuras.R
import com.aventuras.modelos.Adventure

class RecyclerAdapterLocal : RecyclerView.Adapter<RecyclerAdapterLocal.ViewHolderLocal>() {

    var adventures: MutableList<Adventure> = ArrayList()
    lateinit var context: Context
    lateinit var listener : OnItemListClicked

    fun RecyclerAdapterLocal(adventures : MutableList<Adventure>, context: Context, onItemListClicked: OnItemListClicked) {
        this.adventures = adventures
        this.context = context
        this.listener = onItemListClicked
    }

    override fun onBindViewHolder(holder: ViewHolderLocal, position: Int) {
        val item = adventures.get(position)
        holder.bind(item , context)
        holder.itemView.setOnClickListener {
            listener.itemListClicked(item.id , holder.itemView , item.publicado)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapterLocal.ViewHolderLocal {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewHolder = ViewHolderLocal(layoutInflater.inflate(R.layout.item_aventura_local, parent, false))
        return viewHolder
    }

    override fun getItemCount(): Int {
        return adventures.size
    }

    class ViewHolderLocal(view: View) : RecyclerView.ViewHolder(view)   {
        val nombreAventura = view.findViewById(R.id.itemAventura) as TextView
        val creador = view.findViewById(R.id.itemCreador) as TextView
        val fondoFila = view.findViewById(R.id.fondo_item_list) as LinearLayout


        fun bind(adventure: Adventure, context: Context) {
            nombreAventura.text = adventure.nombreAventura
            creador.text = adventure.creador
            if (adventure.publicado) {
                fondoFila.setBackgroundColor( Color.TRANSPARENT )
            } else {
                fondoFila.setBackgroundColor(ContextCompat.getColor(context, R.color.amarillo))
            }
        }

    }



}