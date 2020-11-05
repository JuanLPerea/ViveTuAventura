package com.aventuras

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.aventuras.Adapters.TabLayoutAdapter
import com.aventuras.Fragments.FragmentAventurasLocal
import com.aventuras.Fragments.FragmentAventurasWeb
import com.aventuras.Fragments.firebaseUtils
import com.aventuras.Interfaces.*
import com.aventuras.SalvarPreferencias.DatabaseHelper
import com.aventuras.Utilidades.ImagesHelper
import com.aventuras.Utilidades.Prefs
import com.aventuras.modelos.Adventure
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnLocalListItemSelected, OnWebListItemSelected, ImagenFirebaseCallback, AventuraFirebaseCallback {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var imagesHelper: ImagesHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var imagenPortada: ImageView
    private lateinit var textoPortada: TextView
    private var usuarioUUID : String = ""
    lateinit var fragmentAventurasLocal: FragmentAventurasLocal
    lateinit var fragmentAventurasWeb: FragmentAventurasWeb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        comprobarPrimeraEjecucion()

        // Referencias a las views de la portada
        imagenPortada = findViewById(R.id.imageViewPortada)
        textoPortada = findViewById(R.id.textViewPortada)

        // creamos una instancia de la clase para manipular la imágenes
        imagesHelper = ImagesHelper(applicationContext)

        // Desactivamos modo estricto
        imagesHelper.desactivarModoEstricto()

        // Instanciar Base de Datos SQLite
        databaseHelper = DatabaseHelper(applicationContext)
        db = databaseHelper.writableDatabase

        // Referencias a los fragments
        fragmentAventurasLocal = FragmentAventurasLocal()
        fragmentAventurasLocal.setContexto(applicationContext)
        fragmentAventurasLocal.setListClickListener(this)

        fragmentAventurasWeb = FragmentAventurasWeb()
        fragmentAventurasWeb.setContexto(applicationContext)
        fragmentAventurasWeb.setListenerWebListItemSelected(this)

        // Botón crear aventura oculto al cargar activity
        crearAventuraBTN.visibility = View.INVISIBLE

        // Tab Layout
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        val tab2 = tabLayout.newTab().setText(resources.getText(R.string.tusaventuras))
        val tab1 = tabLayout.newTab().setText(resources.getText(R.string.aventurasweb))
        tabLayout.addTab(tab1)
        tabLayout.addTab(tab2)
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout.tabMode = TabLayout.MODE_FIXED
        val adapter = TabLayoutAdapter(this, supportFragmentManager, tabLayout.tabCount, fragmentAventurasLocal, fragmentAventurasWeb)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    crearAventuraBTN.visibility = View.INVISIBLE
                } else {
                    crearAventuraBTN.visibility = View.VISIBLE
                }


                viewPager.currentItem = tab.position
                imagenPortada.setImageResource(R.drawable.libreta_cortada)
                textoPortada.setText(getString(R.string.aventuras_name))
                fragmentAventurasWeb.filtrarLista("", "", false)

                if (checkConnection()) {
                    viewPager.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.blanco))

                } else {
                    viewPager.setBackgroundResource(R.drawable.sinconexion)
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val crearAventuraBTN = findViewById(R.id.crearAventuraBTN) as FloatingActionButton
        crearAventuraBTN.setOnClickListener {
            CrearAventura()
        }

        val filtrarAventuras = findViewById<FloatingActionButton>(R.id.filtrarAventurasAB)
        filtrarAventuras.setOnClickListener {
            dialogoFiltrar()
        }

        val infoButton = findViewById<FloatingActionButton>(R.id.infoFloatingActionButton)
        infoButton.setOnClickListener {
            val intent = Intent(applicationContext, PresentacionActivity::class.java)
            startActivity(intent)
        }

        // Comprobar conexion
        if (checkConnection()) {
            viewPager.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.blanco))
        } else {
            viewPager.setBackgroundResource(R.drawable.sinconexion)
        }
    }

    private fun CrearAventura() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.crear_dialogo_layout)
        val yesBtn = dialog.findViewById(R.id.empezarCrearBTN) as Button
        val textoDialogo = dialog.findViewById(R.id.tituloDialog) as TextView
        textoDialogo.setText(getString(R.string.add_adv))

        yesBtn.setOnClickListener {
            val nombreAventuraET = dialog.findViewById(R.id.nombreAventuraDLG) as EditText
            val autorET = dialog.findViewById(R.id.AutorDLG) as EditText

            var nomavTMP = nombreAventuraET.text.toString()
            if (nomavTMP.equals("")) nomavTMP = getString(R.string.sinnombre)

            var autorTMP = autorET.text.toString()
            if (autorTMP.equals("")) autorTMP = getString(R.string.sinautor)

            // CREAMOS LA AVENTURA EN LA BASE DE DATOS
            val idAventura = databaseHelper.crearAventuraBD(db, nomavTMP, autorTMP, auth.currentUser!!.uid)

            val intent = Intent(this, CrearAventuraActivity::class.java).apply {
                putExtra("ID_AVENTURA", idAventura)
                putExtra("ESNUEVO", true)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        val cancelBtn = dialog.findViewById(R.id.cancelarCrearBTN) as Button
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun dialogoFiltrar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.filtrar_dialogo_layout)

        val nombreFiltrarET = dialog.findViewById(R.id.nombreAventuraDLG_filtrar) as EditText
        val autorFiltrarET = dialog.findViewById(R.id.AutorDLG_filtrar) as EditText
        val soloNoPublicadosCB = dialog.findViewById(R.id.publicadosCHKBX) as CheckBox


        val okBTN = dialog.findViewById(R.id.aceptarBTN_filtrar) as Button
        okBTN.setOnClickListener {

            var nombreFiltrar = ""
            var autorFiltrar = ""
            var soloNoPublicados = false

            if (nombreFiltrarET.text != null) {
                nombreFiltrar = nombreFiltrarET.text.toString()
            }
            if (autorFiltrarET.text != null) {
                autorFiltrar = autorFiltrarET.text.toString()
            }
            if (soloNoPublicadosCB.isChecked) {
                soloNoPublicados = true
            }

            // llamar a un método público dentro de Fragment Aventuras Local o Web según sea
            when (viewPager.currentItem) {
                1 -> fragmentAventurasLocal.filtrarLista(nombreFiltrar, autorFiltrar, soloNoPublicados)
                0 -> fragmentAventurasWeb.filtrarLista(nombreFiltrar, autorFiltrar, soloNoPublicados)
            }

            dialog.dismiss()
        }

        dialog.show()

    }

    public override fun onStart() {
        super.onStart()
        // Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser == null) {
            signInAnonymously()
        } else {
            usuarioUUID = currentUser.uid
            fragmentAventurasWeb.setUsuarioUUID(usuarioUUID)
        }

    }

    private fun signInAnonymously() {
        // [START signin_anonymously]
        auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        usuarioUUID = auth.uid.toString()
                        fragmentAventurasWeb.setUsuarioUUID(usuarioUUID)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, getString(R.string.eror_usuario),
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }


    override fun LocalListItemSelected(idAventura: String) {
        val aventuraSeleccionada: Adventure
        aventuraSeleccionada = databaseHelper.recuperarAventura(db, idAventura)
        aventuraSeleccionada.listaCapitulos = databaseHelper.cargarCapitulos(db, idAventura)
        var bitmap = imagesHelper.recuperarImagenMemoriaInterna(aventuraSeleccionada.listaCapitulos.get(0).imagenCapitulo)
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.sinimagen)
        }

        imagenPortada.setImageBitmap(bitmap)
        textoPortada.setText(aventuraSeleccionada.nombreAventura)
    }

    override fun OnWebListItemSelected(idAventura: String) {
        firebaseUtils.setAventuraListener(this)
        firebaseUtils.recuperarAventuraFirebase(idAventura)
    }

    override fun onImageLoaded(bitmap: Bitmap) {
        imagenPortada.setImageBitmap(bitmap)
    }

    override fun onAventuraLoaded(aventura: Adventure) {
        firebaseUtils.setImageListener(this)
        textoPortada.setText(aventura.nombreAventura)
        firebaseUtils.cargarImagenFirebase(aventura.id, aventura.listaCapitulos.get(0).id)
    }

    fun comprobarPrimeraEjecucion() {
        val prefs = Prefs(applicationContext)
        if (prefs.primeraEjecucion!!) {
            prefs.primeraEjecucion = false
            val intent = Intent(applicationContext, PresentacionActivity::class.java)
            startActivity(intent)
        }
    }

    fun checkConnection(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }


}
