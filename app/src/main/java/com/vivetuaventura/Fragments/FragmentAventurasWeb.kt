package com.vivetuaventura.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
lateinit var auth : FirebaseAuth

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


        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                TODO("Not yet implemented")
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // Initialize Firebase Auth
                auth = Firebase.auth
                val currentUser = auth.currentUser
                if (currentUser == null) signInAnonymously()
                val usuarioUUID = auth.uid

                if (direction == ItemTouchHelper.LEFT) {
                     // Swipe hacia la izquierda editar
                    Toast.makeText(context , "No es posible editar una Aventura de la Web, puedes borrarla y volver a subirla desde tus Aventuras" , Toast.LENGTH_LONG).show()
                }

                if ( direction == ItemTouchHelper.RIGHT) {
                    if (usuarioUUID.equals(listaAventuras.get(position).usuario)) {
                        // Swipe hacia la derecha borrar
                        ShowDialogConfirmarBorrar(position)
                    } else {
                        Toast.makeText(context , "No puedes borrar una aventura que no has creado tu." , Toast.LENGTH_LONG).show()
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
        textoConfirmar.text = "¿Seguro que quieres borrar?"

        val yesBtn = dialog.findViewById(R.id.aceptar_confirmar_dialog_BTN) as Button
        yesBtn.setOnClickListener {
            // Borrar de Firebase
            firebaseUtils.borrarAventura(listaAventuras.get(position).id)
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
            Toast.makeText(context, "Aventura pendiente de aprobación, próximamente podrás jugar!" , Toast.LENGTH_LONG).show()
        }
    }

    fun setListenerWebListItemSelected (mListenerWebListItemSelected: OnWebListItemSelected) {
        listenerWebListItemSelected = mListenerWebListItemSelected
    }

    private fun signInAnonymously() {
        // [START signin_anonymously]
        auth.signInAnonymously()

    }

}