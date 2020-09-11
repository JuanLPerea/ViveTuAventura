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
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vivetuaventura.Adapters.RecyclerAdapter
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Aventura
import kotlinx.android.synthetic.main.activity_crear_aventura.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.crear_dialogo_layout.*
import kotlinx.android.synthetic.main.crear_dialogo_layout.view.*
import kotlinx.android.synthetic.main.crear_dialogo_layout.view.nombreAventuraDLG

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

        val aventura1 = Aventura("1", "Primera Aventura", "Juan Luis", 10, 10)
        val aventura2 = Aventura("2", "Segunda Aventura", "Juan Luis", 3, 6)
        listaAventuras.add(aventura1)
        listaAventuras.add(aventura2)
        listaAventuras.add(aventura2)
        listaAventuras.add(aventura2)
        listaAventuras.add(aventura2)
        listaAventuras.add(aventura2)

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
            databaseHelper.crearAventuraBD(db , nomavTMP, autorTMP)

            val intent = Intent (this, CrearAventuraActivity::class.java).apply {
                putExtra("NOMBRE_AVENTURA", nomavTMP)
                putExtra("AUTOR_AVENTURA", autorTMP)
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

}



