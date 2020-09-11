package com.vivetuaventura.SalvarPreferencias

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.vivetuaventura.modelos.Aventura
import com.vivetuaventura.modelos.AventuraContract

class  DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "DB_AVENTURAS",null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {

        Log.d("MIAPP" , "Crear BD")

        val CREATE_TABLE_AVENTURA = "CREATE TABLE AVENTURA (ID TEXT, NOMBRE TEXT, CREADOR TEXT, NOTA INTEGER, PUBLICADO BOOLEAN, VISITAS INTEGER)"
        val CREATE_TABLE_CAPITULOS = "CREATE TABLE CAPITULOS (ID TEXT, IDAVENTURA INTEGER, CAPITULOPADRE INTEGER, CAPITULO1 INTEGER, CAPITULO2 INTEGER, TEXTOCAPITULO TEXT, IMAGENCAPITULO TEXT, FINHISTORIA BOOLEAN)"
        db!!.execSQL(CREATE_TABLE_AVENTURA)
        db!!.execSQL(CREATE_TABLE_CAPITULOS)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val DROP_TABLE = "DROP TABLE IF EXISTS " + AventuraContract.AventuraEntry.NOMBRE_AVENTURA
        db!!.execSQL(DROP_TABLE)
        onCreate(db)
    }

    fun crearAventuraBD(db: SQLiteDatabase?, nombreAventura : String , autorAventura : String) {

        val rnds = (0..100000).random()
        val aventuraUUID =  nombreAventura + "_" + System.currentTimeMillis().toString() + "_" + rnds.toString()

        val ADD_NODO = "INSERT INTO AVENTURA VALUES ('" + aventuraUUID + "' , '" + nombreAventura + "' , '" + autorAventura  + "' , 0 , 0, 0)"

        db!!.execSQL(ADD_NODO)
        Log.d("MIAPP" , "Creada Aventura en la BD")
    }

    fun cargarAventura(db: SQLiteDatabase, aventuraUIID:String) {

        val aventura:Aventura
        aventura = Aventura("jk","Aventura falsa", "cualquiera", 0 , 0)

       // return aventura
    }


}