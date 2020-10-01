package com.vivetuaventura.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.vivetuaventura.JugarActivity
import com.vivetuaventura.R
import com.vivetuaventura.modelos.Adventure

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    var adventures: MutableList<Adventure> = ArrayList()
    lateinit var context: Context

    fun RecyclerAdapter(adventures : MutableList<Adventure>, context: Context) {
        this.adventures = adventures
        this.context = context
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        val item = adventures.get(position)
        holder.bind(item , context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_aventura, parent, false))
    }


    override fun getItemCount(): Int {
        return adventures.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

              //  Toast.makeText(context, "Has hecho click en: " + nombreAventura.text, Toast.LENGTH_LONG).show()

                val intent = Intent (context, JugarActivity::class.java).apply {
                    putExtra("ID_AVENTURA", adventure.id)
                    putExtra("ALMACENADO" , "LOCAL")
                }
                context.startActivity(intent)

            })


            itemView.setOnLongClickListener{
                Toast.makeText(context, "Has hecho long click en: " + nombreAventura.text, Toast.LENGTH_LONG).show()

            /*    TODO("Si hacemos long Click mostramos un dialog para decidir si editamos esta aventura o la queremos eliminar" +
                        "Primero hay que comprobar si la aventura que hemos seleccionado es nuestra y si es así" +
                        "lanzamos el intent de crear aventura cargando los datos que tengamos ya creados" +
                        "Si no es nuestra mostramos un mensaje diciendo que no podemos editar aventuras que no son nuestras")

            */
                true
            }
        }

    }


}
