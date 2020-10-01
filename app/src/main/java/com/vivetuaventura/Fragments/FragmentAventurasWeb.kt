package com.vivetuaventura.Fragments

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vivetuaventura.Adapters.RecyclerAdapter
import com.vivetuaventura.Interfaces.FirebaseCallback
import com.vivetuaventura.R
import com.vivetuaventura.Utilidades.FirebaseUtils
import com.vivetuaventura.modelos.Adventure

lateinit var mRecyclerView: RecyclerView
val mAdapter: RecyclerAdapter = RecyclerAdapter()
var listaAventuras: MutableList<Adventure> = mutableListOf()
lateinit var firebaseUtils : FirebaseUtils
private lateinit var auth: FirebaseAuth
private var user = ""

class FragmentAventurasWeb (context : Context): Fragment() , FirebaseCallback {
    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aventuras_web, container, false)

        // Usuario
        // Initialize Firebase Auth
        auth = Firebase.auth
        user = auth.currentUser!!.uid
        Log.d("Miapp" , "Usuario: " + user)

        // utlidades de Firebase
        firebaseUtils = FirebaseUtils(context!!)
        firebaseUtils.setListener(this)

        // Recuperar lista aventuras en Firebase
        listaAventuras = firebaseUtils.recuperarListaAventurasFirebase(user)
        var aventura = Adventure()
        aventura.nombreAventura = "Prueba"

        listaAventuras.add(aventura)

        Log.d("Miapp" , "Tamaño de la lista on Create:" + listaAventuras.size.toString())


        mRecyclerView = view.findViewById(R.id.recyclerAventuraWeb) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(view.context)
        mAdapter.RecyclerAdapter(listaAventuras, view.context)
        mRecyclerView.adapter = mAdapter

        return view
    }

    fun recargarReciclerView() {
        // Recargar la lista de las aventuras
        listaAventuras.removeAll(listaAventuras)
        listaAventuras.addAll( firebaseUtils.recuperarListaAventurasFirebase(user))
        mAdapter.notifyDataSetChanged()
        Log.d("Miapp", "On restart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Miapp" , "Tamaño de la lista On Resume:" + listaAventuras.size.toString())
        recargarReciclerView()
    }

    override fun onListLoaded(listaAventurasRecuperadas: MutableList<Adventure>) {
        Log.d("Miapp" , "Tamaño de la lista Interface:" + listaAventuras.size.toString())
        listaAventuras.removeAll(listaAventuras)
        listaAventuras.addAll(listaAventurasRecuperadas)
        mAdapter.notifyDataSetChanged()
    }


}