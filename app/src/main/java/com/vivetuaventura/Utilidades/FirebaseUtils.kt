package com.vivetuaventura.Utilidades

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo

class FirebaseUtils (val context: Context) {

    fun subirAventuraFirebase (db : SQLiteDatabase, adventure : Adventure, usuario : String) {

        val databaseHelper = DatabaseHelper(context)
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        var listaCapitulos: MutableList<Capitulo> = mutableListOf()
        listaCapitulos = databaseHelper.cargarCapitulos(db, adventure.id)


        // Crear un Map con los datos de la aventura
        val aventuraMap = hashMapOf(
            "ID" to adventure.id,
            "NOMBRE" to adventure.nombreAventura,
            "CREADOR" to adventure.creador,
            "NOTA" to adventure.nota,
            "PUBLICADO" to adventure.publicado,
            "VISITAS" to adventure.visitas,
            "LISTACAPITULOS" to listaCapitulos
        )

        // Add a new document with a generated ID
        firebaseDatabase.collection(usuario).document(adventure.id)
            .set(aventuraMap)
            .addOnSuccessListener { documentReference ->
                Log.d("Miapp", "DocumentSnapshot added with ID: ${adventure.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Miapp", "Error adding document", e)
            }


    }



}