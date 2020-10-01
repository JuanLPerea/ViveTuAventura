package com.vivetuaventura.Utilidades

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.vivetuaventura.Interfaces.FirebaseCallback
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo

class FirebaseUtils (val context: Context) {

    private var listener: FirebaseCallback? = null //instance of your interface


    fun subirAventuraFirebase (db : SQLiteDatabase, adventure : Adventure, usuario : String) {

        val databaseHelper = DatabaseHelper(context)
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        var listaCapitulos: MutableList<Capitulo> = mutableListOf()
        listaCapitulos = databaseHelper.cargarCapitulos(db, adventure.id)
        adventure.listaCapitulos = listaCapitulos

        // Guardar en firebase
        firebaseDatabase.collection(usuario).document(adventure.id)
            .set(adventure)
            .addOnSuccessListener { documentReference ->
                Log.d("Miapp", "DocumentSnapshot added with ID: ${adventure.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Miapp", "Error adding document", e)
            }

    }

    fun recuperarAventuraFirebase (db: SQLiteDatabase, usuario: String , idAventura:String) : Adventure {

        var  aventuraCargada = Adventure()

        val databaseHelper = DatabaseHelper(context)
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()

        val docRef = firebaseDatabase.collection(usuario).document(idAventura)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            aventuraCargada = documentSnapshot.toObject(Adventure::class.java)!!
        }

        return  aventuraCargada

    }
    
    
    fun recuperarListaAventurasFirebase (usuario: String) : MutableList<Adventure> {
        
        var listaAventuras : MutableList<Adventure> = mutableListOf()
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()

        firebaseDatabase.collection(usuario)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val aventuraTMP = document.toObject(Adventure::class.java)
                    Log.d("Miapp" , "Aventura ${document.id} del usuario ${usuario}")
                    listaAventuras.add(aventuraTMP)
                }

                // Comunicamos al interface que ha terminado la tarea y devolvemos los datos
                listener!!.onListLoaded(listaAventuras)
            }

        return listaAventuras
        
    }


    fun setListener(listener : FirebaseCallback) {
        this.listener = listener
    }

    /*
    fun getJobsOnADate(Date date) {
        ...
    }
     */


}