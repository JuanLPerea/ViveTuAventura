package com.vivetuaventura

import android.app.Dialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.vivetuaventura.Adapters.RecyclerAdapter
import com.vivetuaventura.Adapters.TabLayoutAdapter
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Aventura

class MainActivity : AppCompatActivity() {
    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        databaseHelper = DatabaseHelper(applicationContext)
        db = databaseHelper.writableDatabase


        // Tab Layout
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        tabLayout.addTab(tabLayout.newTab().setText("Tus Aventuras"))
        tabLayout.addTab(tabLayout.newTab().setText("Aventuras Web"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = TabLayoutAdapter(
            this, supportFragmentManager,
            tabLayout.tabCount
        )
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })



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





}
