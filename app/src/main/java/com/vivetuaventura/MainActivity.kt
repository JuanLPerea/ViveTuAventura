package com.vivetuaventura

import android.app.Dialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vivetuaventura.Adapters.RecyclerAdapter
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Aventura

class MainActivity : AppCompatActivity() {

    lateinit var mRecyclerView : RecyclerView
    lateinit var databaseHelper: DatabaseHelper
    lateinit var db : SQLiteDatabase
    val mAdapter : RecyclerAdapter = RecyclerAdapter()
    var listaAventuras:MutableList<Aventura> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MIAPP", "on Create")
        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.writableDatabase

        // Desactivar modo estricto


        // Recuperar lista aventuras en la BD
        listaAventuras = databaseHelper.cargarListaAventurasBD(db)
        setUpRecyclerView()

        val crearAventuraBTN = findViewById(R.id.crearAventuraBTN) as FloatingActionButton
        crearAventuraBTN.setOnClickListener {
            Log.d("Miapp", "Ha pulsado crear aventura")
            showDialog()
        }

        val filtrarAventuras = findViewById(R.id.filtrarAventurasAB) as FloatingActionButton
        filtrarAventuras.setOnClickListener{
            Log.d("Miapp", "pulsado filtar")
            dialogoFiltrar()
        }

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

                    /*
                    removeView()
                    edit_position = position
                    alertDialog!!.setTitle("Edit Name")
                    et_name!!.setText(names[position])
                    alertDialog!!.show()


                     */
                    recargarReciclerView()
                } else {

                    // Swipe hacia la derecha borrar
                    databaseHelper.eliminarAventuraBD(db , listaAventuras.get(position).id)
                    recargarReciclerView()

                }
            }

        }


        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

    }

    fun setUpRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerAventura) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.RecyclerAdapter(listaAventuras, this)
        mRecyclerView.adapter = mAdapter
    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.crear_dialogo_layout)
        val yesBtn = dialog.findViewById(R.id.empezarCrearBTN) as Button

        yesBtn.setOnClickListener {

            val nombreAventuraET = dialog.findViewById(R.id.nombreAventuraDLG) as EditText
            val autorET = dialog.findViewById(R.id.AutorDLG) as EditText

            var nomavTMP =  nombreAventuraET.text.toString()
            if (nomavTMP.equals("")) nomavTMP = "Sin Nombre"

            var autorTMP =  autorET.text.toString()
            if (autorTMP.equals("")) autorTMP = "Sin Autor"

            // CREAMOS LA AVENTURA EN LA BASE DE DATOS
            val idAventura = databaseHelper.crearAventuraBD(db , nomavTMP, autorTMP)

            val intent = Intent (this, CrearAventuraActivity::class.java).apply {
                putExtra("ID_AVENTURA", idAventura)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun dialogoFiltrar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.filtrar_dialogo_layout)
        val okBTN = dialog.findViewById(R.id.aceptarBTN_filtrar) as Button

        okBTN.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    override fun onRestart() {
        super.onRestart()
        recargarReciclerView()



    }

    fun recargarReciclerView() {
        // Recargar la lista de las aventuras
        listaAventuras.removeAll(listaAventuras)
        listaAventuras.addAll( databaseHelper.cargarListaAventurasBD(db))
        mAdapter.notifyDataSetChanged()
        Log.d("Miapp", "On restart")
    }
}



