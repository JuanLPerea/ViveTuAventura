package com.aventuras.Utilidades

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.aventuras.Interfaces.AventuraFirebaseCallback
import com.aventuras.Interfaces.FirebaseCallback
import com.aventuras.Interfaces.ImagenFirebaseCallback
import com.aventuras.Interfaces.NumeroAventurasCallback
import com.aventuras.R
import com.aventuras.SalvarPreferencias.DatabaseHelper
import com.aventuras.modelos.Adventure
import com.aventuras.modelos.Capitulo
import java.io.File

class FirebaseUtils (val context: Context) {

    private var listener: FirebaseCallback? = null //instance of your interface
    private var aventuraListener : AventuraFirebaseCallback? = null
    private var imageListener : ImagenFirebaseCallback? = null
    private var numeroAventurasUsuarioListener : NumeroAventurasCallback? = null
    lateinit var storage: FirebaseStorage
    var contador = 0

    fun subirAventuraFirebase (db : SQLiteDatabase, adventure : Adventure) {

        val databaseHelper = DatabaseHelper(context)
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        var listaCapitulos: MutableList<Capitulo> = mutableListOf()
        listaCapitulos = databaseHelper.cargarCapitulos(db, adventure.id)
        adventure.listaCapitulos = listaCapitulos
        adventure.publicado = false
        // Guardar en firebase
        firebaseDatabase.collection("AVENTURAS").document(adventure.id)
            .set(adventure)
            .addOnSuccessListener { documentReference ->
//                Log.d("Miapp", "DocumentSnapshot added with ID: ${adventure.id}")
                Toast.makeText(context, "Historia Subida, pendiente de publicación.", Toast.LENGTH_LONG).show()
                // Actualizamos la base de datos local para saber que esta aventura ya está publicada
                adventure.publicado = true
                databaseHelper.actualizarAventura(db , adventure)
                // Actualizamos el campo notificaciones en Firebase
                val valor = hashMapOf("NOTI_TXT" to adventure.nombreAventura)
                firebaseDatabase.collection("NOTIFICACION").document("NOTI_DOC").set(valor)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error $e", Toast.LENGTH_LONG).show()
                Log.d("Miapp", "Error adding document", e)
            }
    }

    fun subirImagenesFirebase (listaCapitulos: MutableList<Capitulo> , idAventura: String) {
        storage = FirebaseStorage.getInstance()

        if (contador < listaCapitulos.size) {
            val storageRef = storage.getReference()
            var file = Uri.fromFile(File(listaCapitulos.get(contador).imagenCapitulo))
            val idCapitulo = listaCapitulos.get(contador).id
            val imageRef = storageRef.child("images/" + idAventura + "/" + idCapitulo + ".jpg")
            var uploadTask = imageRef.putFile(file)

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {error ->
                // Handle unsuccessful uploads
                Log.d("Miapp" , "Error al subir foto a Firebase")
                Toast.makeText(context, "Error al subir imágenes: $error", Toast.LENGTH_LONG).show()
                contador++
                subirImagenesFirebase(listaCapitulos, idAventura)
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                Log.d("Miapp" , "Subida foto a Firebase")
              //  Toast.makeText(context, "Imágenes subidas", Toast.LENGTH_LONG).show()
                contador++
                subirImagenesFirebase(listaCapitulos, idAventura)
            }
        } else if (contador ==  listaCapitulos.size) {
            contador = 0
        }
    }

    fun cargarImagenFirebase (idAventura: String, idCapitulo : String)  {
        storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()
        val imageRef = storageRef.child("images/" + idAventura + "/" + idCapitulo + ".jpg")
        val ONE_MEGABYTE: Long = 1024 * 1024
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
            imageListener!!.onImageLoaded(bitmap)
        }.addOnFailureListener{
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sinimagen)
            imageListener!!.onImageLoaded(bitmap)
            Log.d("Miapp" , "Error al descargar imagen")
        }
    }

    fun recuperarAventuraFirebase  (idAventura:String)  {
        var  aventuraCargada = Adventure()
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        val docRef = firebaseDatabase.collection("AVENTURAS").document(idAventura)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            aventuraCargada = documentSnapshot.toObject(Adventure::class.java)!!
            aventuraListener!!.onAventuraLoaded(aventuraCargada)
        }
    }

    fun getNumAventurasUsuario (usuario : String) {

        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        var numeroAventuras = 0
        var totalAventuras = 0

        firebaseDatabase.collection("AVENTURAS")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        totalAventuras++
                        val aventuraTMP = document.toObject(Adventure::class.java)
                        if (aventuraTMP.usuario.equals(usuario)) {
                            numeroAventuras++
                        }
                    }
                    numeroAventurasUsuarioListener!!.NumeroAventurasUsuario(numeroAventuras, totalAventuras)
                    }
                .addOnFailureListener {exception ->
                    Toast.makeText(context, "Error al publicar aventura" , Toast.LENGTH_LONG).show()
                    Log.d("Miapp" , "Error: " + exception)
                }
    }



    fun recuperarListaAventurasFirebase (nombreAventura:String, autorAventura:String, soloNoPublicados:Boolean) {

        var listaAventuras : MutableList<Adventure> = mutableListOf()
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()

        firebaseDatabase.collection("AVENTURAS")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val aventuraTMP = document.toObject(Adventure::class.java)

                    // Filtrar ...........................
                    //

                    var filtrarAdd = false

                    // Si queremos ver solo los no publicados (No tiene en cuenta si se ha filtrado por nombre o autor
                    if (soloNoPublicados) {
                        if (!aventuraTMP.publicado) filtrarAdd = true
                    } else {
                        // Si no filtramos por ningún criterio, añadir siempre
                        if (nombreAventura.equals("") && autorAventura.equals("")) {
                            filtrarAdd = true
                        } else {
                            // si filtramos por nombre, añadir si contiene el texto buscado
                            if (!nombreAventura.equals("") && aventuraTMP.nombreAventura.contains(nombreAventura)) {
                                filtrarAdd = true
                            }
                            // si filtramos por autor, añadir si contiene el texto buscado
                            if (!autorAventura.equals("") && aventuraTMP.creador.contains(autorAventura)) {
                                filtrarAdd = true
                            }
                        }
                    }

                    if (filtrarAdd) listaAventuras.add(aventuraTMP)

                }
                // Comunicamos al interface que ha terminado la tarea y devolvemos los datos
                listener!!.onListLoaded(listaAventuras)
            }
                .addOnFailureListener {
                    Log.d("Miapp" , "Error al descargar lista")
                }

    }


    fun setListener(listener : FirebaseCallback) {
        this.listener = listener
    }

    fun setAventuraListener(listenerAventura : AventuraFirebaseCallback) {
        this.aventuraListener = listenerAventura
    }

    fun setImageListener(listenerImagenes : ImagenFirebaseCallback) {
        this.imageListener = listenerImagenes
    }

    fun setNumeroAventurasListener(numeroAventurasCallback: NumeroAventurasCallback) {
        this.numeroAventurasUsuarioListener = numeroAventurasCallback
    }

    fun actualizarNotaAventura( aventura: Adventure) {
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        firebaseDatabase.collection("AVENTURAS").document(aventura.id)
                .update("nota" , aventura.nota)
                .addOnSuccessListener {
                    Log.d("Miapp" , "Nota actualizada")
                }
                .addOnFailureListener { e ->
                    Log.d("Miapp" , "Error: $e")
                }
    }

    fun actualizarVisitasAventura(aventura: Adventure) {
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        firebaseDatabase.collection("AVENTURAS").document(aventura.id)
                .update("visitas" , aventura.visitas)
                .addOnSuccessListener {
                    Log.d("Miapp" , "Nota actualizada")
                }
                .addOnFailureListener { e ->
                    Log.d("Miapp" , "Error: $e")
                }
    }

    fun borrarAventura(aventura : Adventure) {
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        firebaseDatabase.collection("AVENTURAS").document(aventura.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context , "Aventura Borrada" , Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context , "Error al borrar la aventura" , Toast.LENGTH_LONG).show()
                }

        // Borrar imagenes de la aventura de Firebase Storage
        storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()

        // Delete the file


        aventura.listaCapitulos.forEach {capituloTMP ->
            val imageRef = storageRef.child("images/" + aventura.id + "/" + capituloTMP.id + ".jpg")
            imageRef
                .delete()
                .addOnSuccessListener {
                     Log.d("Miapp" , "Imagenes Borradas Correctamente")
            }
                .addOnFailureListener {
                     Log.d("Miapp" , "Error al borrar imagenes")
                }
        }



    }






}