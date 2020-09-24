package com.vivetuaventura.Fragments

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vivetuaventura.Adapters.RecyclerAdapter
import com.vivetuaventura.R
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Aventura

class FragmentAventurasLocal(context : Context) : Fragment() {

    lateinit var mRecyclerView: RecyclerView
    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    val mAdapter: RecyclerAdapter = RecyclerAdapter()
    var listaAventuras: MutableList<Aventura> = mutableListOf()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aventuras_local, container, false)

        Log.d("MIAPP", "on Create")
        databaseHelper = DatabaseHelper(context!!)
        db = databaseHelper.writableDatabase

        // Recuperar lista aventuras en la BD
        listaAventuras = databaseHelper.cargarListaAventurasBD(db)

        mRecyclerView = view.findViewById(R.id.recyclerAventura) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(view.context)
        mAdapter.RecyclerAdapter(listaAventuras, view.context)
        mRecyclerView.adapter = mAdapter



        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val position = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe hacia la izquierda editar

                    /*
                    removeView()
                    edit_position = position
                    alertDialog!!.setTitle("Edit Name")
                    et_name!!.setText(names[position])
                    alertDialog!!.show()


                     */
                    recargarReciclerView()
                } else {

                    // Swipe hacia la derecha borrar
                    databaseHelper.eliminarAventuraBD(db , listaAventuras.get(position).id)
                    recargarReciclerView()

                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

        return view
    }

    fun recargarReciclerView() {
        // Recargar la lista de las aventuras
        listaAventuras.removeAll(listaAventuras)
        listaAventuras.addAll( databaseHelper.cargarListaAventurasBD(db))
        mAdapter.notifyDataSetChanged()
        Log.d("Miapp", "On restart")
    }

    override fun onResume() {
        super.onResume()
        recargarReciclerView()
    }

}