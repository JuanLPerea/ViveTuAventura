package com.aventuras.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.aventuras.Adapters.RecyclerAdapter
import com.aventuras.Interfaces.FirebaseCallback
import com.aventuras.Interfaces.MainProgressBarCallback
import com.aventuras.Interfaces.OnItemListClicked
import com.aventuras.Interfaces.OnWebListItemSelected
import com.aventuras.JugarActivity
import com.aventuras.R
import com.aventuras.Utilidades.FirebaseUtils
import com.aventuras.modelos.Adventure

private lateinit var mRecyclerView: RecyclerView
private val mAdapter: RecyclerAdapter = RecyclerAdapter()
private var listaAventuras: MutableList<Adventure> = mutableListOf()
lateinit var firebaseUtils : FirebaseUtils
private lateinit var listenerWebListItemSelected : OnWebListItemSelected
private lateinit var auth : FirebaseAuth
private lateinit var contexto : Context
private var usuarioUUID = ""
lateinit var listenerProgressBar : MainProgressBarCallback

class FragmentAventurasWeb (): Fragment() , FirebaseCallback , OnItemListClicked {

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

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                TODO("Not yet implemented")
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition


                if (direction == ItemTouchHelper.LEFT) {
                     // Swipe hacia la izquierda editar
                    Toast.makeText(context , getString(R.string.no_edit_web_adv) , Toast.LENGTH_LONG).show()
                }

                if ( direction == ItemTouchHelper.RIGHT) {
                    if (usuarioUUID.equals(listaAventuras.get(position).usuario)) {
                        // Swipe hacia la derecha borrar
                        ShowDialogConfirmarBorrar(position)
                    } else {
                        Toast.makeText(context , getString(R.string.no_delete_adv_own) , Toast.LENGTH_LONG).show()
                    }
                }
                recargarReciclerView()
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

        return view
    }

    fun ShowDialogConfirmarBorrar (position : Int) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.confirmar_dialog)

        val textoConfirmar = dialog.findViewById(R.id.texto_dialog_confirmarTV) as TextView
        textoConfirmar.text = resources.getText(R.string.borrar_confirmar)

        val yesBtn = dialog.findViewById(R.id.aceptar_confirmar_dialog_BTN) as Button
        yesBtn.setOnClickListener {
            // Borrar de Firebase
            firebaseUtils.borrarAventura(listaAventuras.get(position))
            recargarReciclerView()
            dialog.dismiss()
        }

        val noBtn = dialog.findViewById(R.id.cancelar_confirmar_dialog_BTN) as Button
        noBtn.setOnClickListener {
            recargarReciclerView()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun recargarReciclerView() {
        // Recargar la lista de las aventuras
        listaAventuras.removeAll(listaAventuras)
        if (CheckConnection()) {
            firebaseUtils.recuperarListaAventurasFirebase("","", false)
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        recargarReciclerView()
    }

    override fun onListLoaded(listaAventurasRecuperadas: MutableList<Adventure>) {

        listaAventurasRecuperadas.sortBy{ adventure -> adventure.visitas }
        listaAventurasRecuperadas.reverse()

        listaAventuras.removeAll(listaAventuras)
        if (CheckConnection()) {
            listaAventuras.addAll(listaAventurasRecuperadas)
        }
        listenerProgressBar!!.RecyclerListUpdated()
        mAdapter.notifyDataSetChanged()
    }

    fun filtrarLista(nombreAventura : String, autorAventura: String, soloNoPublicados : Boolean) {
        // Recargar la lista de las aventuras
        listaAventuras.removeAll(listaAventuras)
        if(CheckConnection()) {
            firebaseUtils.recuperarListaAventurasFirebase(nombreAventura, autorAventura, soloNoPublicados)
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun itemListClicked(idAventura: String , itemView : View , publicado : Boolean) {
        if (CheckConnection()) {
            if (publicado) {
                listenerWebListItemSelected.OnWebListItemSelected(idAventura)
                val popupMenu = PopupMenu(context, itemView)
                popupMenu.menu.add(R.string.jugar)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    val intent = Intent (context, JugarActivity::class.java).apply {
                        putExtra("ID_AVENTURA", idAventura)
                    }
                    startActivity(intent)

                    true
                }
                popupMenu.show()
            } else {
                Toast.makeText(context, getString(R.string.pendiente_apro) , Toast.LENGTH_LONG).show()
            }
        }

    }

    fun setListenerWebListItemSelected (mListenerWebListItemSelected: OnWebListItemSelected) {
        listenerWebListItemSelected = mListenerWebListItemSelected
    }

    fun setListenerMainProgressBar (mListenerProgressBarCallback: MainProgressBarCallback) {
        listenerProgressBar = mListenerProgressBarCallback

    }

    fun setContexto (contextoApp : Context) {
        contexto = contextoApp
    }

    private fun CheckConnection () : Boolean {
        val cm = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    fun setUsuarioUUID (usuarioUUIDMain : String) {
        usuarioUUID = usuarioUUIDMain
    }

}