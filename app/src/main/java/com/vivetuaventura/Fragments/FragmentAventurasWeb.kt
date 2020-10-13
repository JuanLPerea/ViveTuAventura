package com.vivetuaventura.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vivetuaventura.Adapters.RecyclerAdapter
import com.vivetuaventura.Interfaces.FirebaseCallback
import com.vivetuaventura.Interfaces.OnItemListClicked
import com.vivetuaventura.R
import com.vivetuaventura.Utilidades.FirebaseUtils
import com.vivetuaventura.modelos.Adventure

lateinit var mRecyclerView: RecyclerView
val mAdapter: RecyclerAdapter = RecyclerAdapter()
var listaAventuras: MutableList<Adventure> = mutableListOf()
lateinit var firebaseUtils : FirebaseUtils

class FragmentAventurasWeb (context : Context): Fragment() , FirebaseCallback {
    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aventuras_web, container, false)

        // utlidades de Firebase
        firebaseUtils = FirebaseUtils(context!!)
        firebaseUtils.setListener(this)

        // Recuperar lista aventuras en Firebase
        firebaseUtils.recuperarListaAventurasFirebase("","", false)
        var aventura = Adventure()
        aventura.nombreAventura = "Prueba"

        listaAventuras.add(aventura)

        mRecyclerView = view.findViewById(R.id.recyclerAventuraWeb) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(view.context)
        mAdapter.RecyclerAdapter(listaAventuras, view.context , this as OnItemListClicked)
        mRecyclerView.adapter = mAdapter

        return view
    }

    fun recargarReciclerView() {
        // Recargar la lista de las aventuras
        firebaseUtils.recuperarListaAventurasFirebase("","", false)
        mAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        recargarReciclerView()
    }

    override fun onListLoaded(listaAventurasRecuperadas: MutableList<Adventure>) {
        listaAventuras.removeAll(listaAventuras)
        listaAventuras.addAll(listaAventurasRecuperadas)
        mAdapter.notifyDataSetChanged()
    }

    fun filtrarLista(nombreAventura : String, autorAventura: String, soloNoPublicados : Boolean) {
        // Recargar la lista de las aventuras
        firebaseUtils.recuperarListaAventurasFirebase(nombreAventura, autorAventura, soloNoPublicados)
        mAdapter.notifyDataSetChanged()
    }

}