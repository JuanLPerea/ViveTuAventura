package com.vivetuaventura.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vivetuaventura.Adapters.RecyclerAdapter
import com.vivetuaventura.Interfaces.FirebaseCallback
import com.vivetuaventura.Interfaces.OnItemListClicked
import com.vivetuaventura.Interfaces.OnWebListItemSelected
import com.vivetuaventura.JugarActivity
import com.vivetuaventura.R
import com.vivetuaventura.Utilidades.FirebaseUtils
import com.vivetuaventura.modelos.Adventure

lateinit var mRecyclerView: RecyclerView
val mAdapter: RecyclerAdapter = RecyclerAdapter()
var listaAventuras: MutableList<Adventure> = mutableListOf()
lateinit var firebaseUtils : FirebaseUtils
lateinit var listenerWebListItemSelected : OnWebListItemSelected

class FragmentAventurasWeb (context : Context): Fragment() , FirebaseCallback , OnItemListClicked {
    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aventuras_web, container, false)

        // utlidades de Firebase
        firebaseUtils = FirebaseUtils(context!!)
        firebaseUtils.setListener(this)

        // Recuperar lista aventuras en Firebase
        firebaseUtils.recuperarListaAventurasFirebase("","", false)

        mRecyclerView = view.findViewById(R.id.recyclerAventuraWeb) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(view.context)
        mAdapter.RecyclerAdapter(listaAventuras, view.context , this)
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

    override fun itemListClicked(idAventura: String , itemView : View , publicado : Boolean) {

        if (publicado) {
            listenerWebListItemSelected.OnWebListItemSelected(idAventura)
            val popupMenu = PopupMenu(context, itemView)
            popupMenu.inflate(R.menu.jugar_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId ==  R.id.jugar_aventura_menu_item) {
                    val intent = Intent (context, JugarActivity::class.java).apply {
                        putExtra("ID_AVENTURA", idAventura)
                    }
                    startActivity(intent)
                }
                true
            }
            popupMenu.show()
        } else {
            Toast.makeText(context, "Esta historia aún no está publicada" , Toast.LENGTH_LONG).show()
        }
    }

    fun setListenerWebListItemSelected (mListenerWebListItemSelected: OnWebListItemSelected) {
        listenerWebListItemSelected = mListenerWebListItemSelected
    }


}