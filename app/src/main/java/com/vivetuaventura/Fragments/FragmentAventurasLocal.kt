package com.vivetuaventura.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vivetuaventura.Adapters.RecyclerAdapter
import com.vivetuaventura.CrearAventuraActivity
import com.vivetuaventura.R
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Adventure

class FragmentAventurasLocal(context : Context) : Fragment() {

    lateinit var mRecyclerView: RecyclerView
    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    val mAdapter: RecyclerAdapter = RecyclerAdapter()
    var listaAdventures: MutableList<Adventure> = mutableListOf()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aventuras_local, container, false)

        Log.d("MIAPP", "on Create")
        databaseHelper = DatabaseHelper(context!!)
        db = databaseHelper.writableDatabase

        // Recuperar lista aventuras en la BD
        listaAdventures = databaseHelper.cargarListaAventurasBD(db)

        mRecyclerView = view.findViewById(R.id.recyclerAventuraLocal) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(view.context)
        mAdapter.RecyclerAdapter(listaAdventures, view.context)
        mRecyclerView.adapter = mAdapter

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe hacia la izquierda editar
                    showDialogConfirmarEditar(position)
                } else {
                    // Swipe hacia la derecha borrar
                    showDialogConfirmarBorrar(position)
                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

        return view
    }

    fun recargarReciclerView() {
        // Recargar la lista de las aventuras
        listaAdventures.removeAll(listaAdventures)
        listaAdventures.addAll( databaseHelper.cargarListaAventurasBD(db))
        mAdapter.notifyDataSetChanged()
        Log.d("Miapp", "On restart")
    }

    override fun onResume() {
        super.onResume()
        recargarReciclerView()
    }


    private fun showDialogConfirmarBorrar(position : Int) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.confirmar_dialog)

        val textoConfirmar = dialog.findViewById(R.id.texto_dialog_confirmarTV) as TextView
        textoConfirmar.text = "¿Seguro que quieres borrar?"

        val yesBtn = dialog.findViewById(R.id.aceptar_confirmar_dialog_BTN) as Button
        yesBtn.setOnClickListener {
            databaseHelper.eliminarAventuraBD(db , listaAdventures.get(position).id)
            recargarReciclerView()
            dialog.dismiss()
        }

        val noBtn = dialog.findViewById(R.id.cancelar_confirmar_dialog_BTN) as Button
        noBtn.setOnClickListener {
            recargarReciclerView()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDialogConfirmarEditar(position : Int) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.crear_dialogo_layout)

        val nombreAventura = dialog.findViewById(R.id.nombreAventuraDLG) as EditText
        nombreAventura.setText(listaAdventures.get(position).nombreAventura)
        val autorAventura = dialog.findViewById(R.id.AutorDLG) as EditText
        autorAventura.setText(listaAdventures.get(position).creador)

        val yesBtn = dialog.findViewById(R.id.empezarCrearBTN) as Button
        yesBtn.setOnClickListener {

            // Actualizamos el nombre y autor en la BD
            listaAdventures.get(position).nombreAventura = nombreAventura.text.toString()
            listaAdventures.get(position).creador = autorAventura.text.toString()
            databaseHelper.actualizarAventura(db, listaAdventures.get(position))

            // Lanzamos el activity
            val intent = Intent (activity, CrearAventuraActivity::class.java).apply {
                putExtra("ID_AVENTURA", listaAdventures.get(position).id)
                putExtra("ESNUEVO" , false)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        val noBtn = dialog.findViewById(R.id.cancelarCrearBTN) as Button
        noBtn.setOnClickListener {
            recargarReciclerView()
            dialog.dismiss()
        }

        dialog.show()
    }
}


