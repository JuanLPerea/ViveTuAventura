package com.aventuras.Adapters

import android.content.Context
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aventuras.Interfaces.OnItemListClicked
import com.aventuras.R
import com.aventuras.modelos.Adventure

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    var adventures: MutableList<Adventure> = ArrayList()
    lateinit var context: Context
    lateinit var listener : OnItemListClicked

    fun RecyclerAdapter(adventures : MutableList<Adventure>, context: Context, onItemListClicked: OnItemListClicked) {
        this.adventures = adventures
        this.context = context
        this.listener = onItemListClicked
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        val item = adventures.get(position)
        holder.bind(item , context)
        holder.itemView.setOnClickListener {
            listener.itemListClicked(item.id , holder.itemView , item.publicado)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewHolder = ViewHolder(layoutInflater.inflate(R.layout.item_aventura, parent, false))
        return viewHolder
    }

    override fun getItemCount(): Int {
        return adventures.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)   {
        val nombreAventura = view.findViewById(R.id.itemAventura) as TextView
        val creador = view.findViewById(R.id.itemCreador) as TextView
        val visitas = view.findViewById(R.id.itemVisitas) as TextView
        val nota = view.findViewById(R.id.itemNota) as TextView
        val fondoFila = view.findViewById(R.id.fondo_item_list) as LinearLayout


        fun bind(adventure:Adventure, context: Context) {
            nombreAventura.text = adventure.nombreAventura
            creador.text = adventure.creador
            visitas.text = adventure.visitas.toString()
            nota.text = adventure.nota.toString()
            if (adventure.publicado) {
                fondoFila.setBackgroundColor(ContextCompat.getColor(context, R.color.blanco ))
            } else {
                fondoFila.setBackgroundColor(ContextCompat.getColor(context, R.color.amarillo))
            }
        }

    }


}
