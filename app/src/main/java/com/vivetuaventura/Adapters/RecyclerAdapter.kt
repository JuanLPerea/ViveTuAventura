package com.vivetuaventura.Adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.vivetuaventura.JugarActivity
import com.vivetuaventura.R
import com.vivetuaventura.modelos.Aventura

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    var aventuras: MutableList<Aventura> = ArrayList()
    lateinit var context: Context

    fun RecyclerAdapter(aventuras : MutableList<Aventura>, context: Context) {
        this.aventuras = aventuras
        this.context = context
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        val item = aventuras.get(position)
        holder.bind(item , context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_aventura, parent, false))
    }


    override fun getItemCount(): Int {
        return aventuras.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreAventura = view.findViewById(R.id.itemAventura) as TextView
        val creador = view.findViewById(R.id.itemCreador) as TextView
        val visitas = view.findViewById(R.id.itemVisitas) as TextView
        val nota = view.findViewById(R.id.itemNota) as TextView

        fun bind(aventura:Aventura, context: Context) {
            nombreAventura.text = aventura.nombreAventura
            creador.text = aventura.creador
            visitas.text = aventura.visitas.toString()
            nota.text = aventura.nota.toString()
            itemView.setOnClickListener(View.OnClickListener {

              //  Toast.makeText(context, "Has hecho click en: " + nombreAventura.text, Toast.LENGTH_LONG).show()

                val intent = Intent (context, JugarActivity::class.java).apply {
                    putExtra("ID_AVENTURA", aventura.id)
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
