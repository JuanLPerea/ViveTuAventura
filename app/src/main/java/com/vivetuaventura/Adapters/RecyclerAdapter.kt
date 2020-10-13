package com.vivetuaventura.Adapters

import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vivetuaventura.Interfaces.OnItemListClicked
import com.vivetuaventura.JugarActivity
import com.vivetuaventura.MainActivity
import com.vivetuaventura.R
import com.vivetuaventura.modelos.Adventure

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
            listener.itemListClicked(item.id)
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


        fun bind(adventure:Adventure, context: Context) {
            nombreAventura.text = adventure.nombreAventura
            creador.text = adventure.creador
            visitas.text = adventure.visitas.toString()
            nota.text = adventure.nota.toString()
            itemView.setOnClickListener(View.OnClickListener {
                val popupMenu = PopupMenu(context, itemView)
                popupMenu.inflate(R.menu.jugar_menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId ==  R.id.jugar_aventura_menu_item) {
                        val intent = Intent (context, JugarActivity::class.java).apply {
                            putExtra("ID_AVENTURA", adventure.id)
                        }
                        context.startActivity(intent)
                    }
                    true
                }
                    popupMenu.show()
            })
        }




    }


}
