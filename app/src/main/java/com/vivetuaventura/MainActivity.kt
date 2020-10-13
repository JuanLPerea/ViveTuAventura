package com.vivetuaventura

import android.app.Dialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vivetuaventura.Adapters.TabLayoutAdapter
import com.vivetuaventura.Fragments.FragmentAventurasLocal
import com.vivetuaventura.Fragments.FragmentAventurasWeb
import com.vivetuaventura.Fragments.firebaseUtils
import com.vivetuaventura.Interfaces.NumeroAventurasCallback
import com.vivetuaventura.Interfaces.OnLocalListItemSelected
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.Utilidades.FirebaseUtils
import com.vivetuaventura.Utilidades.ImagesHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() , OnLocalListItemSelected {
    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    lateinit var imagesHelper : ImagesHelper
    private lateinit var auth: FirebaseAuth
    lateinit var fragmentAventurasLocal : FragmentAventurasLocal
    lateinit var fragmentAventurasWeb : FragmentAventurasWeb
    var usuario = ""
    lateinit var imagenPortada : ImageView
    lateinit var textoPortada : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a las views de la portada
        imagenPortada = findViewById(R.id.imageViewPortada)
        textoPortada = findViewById(R.id.textViewPortada)

        // Referencias a los fragments
        fragmentAventurasLocal = FragmentAventurasLocal(this)
        fragmentAventurasWeb = FragmentAventurasWeb(this)

        // Listener para cuando haces click en un elemento de la lista
        fragmentAventurasLocal.setListClickListener(this)

        // creamos una instancia de la clase para manipular la imágenes
        imagesHelper = ImagesHelper(applicationContext)

        // Desactivamos modo estricto
        imagesHelper.desactivarModoEstricto()

        // Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser == null) signInAnonymously()

        // Instanciar Base de Datos SQLite
        databaseHelper = DatabaseHelper(applicationContext)
        db = databaseHelper.writableDatabase

        // Tab Layout
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        val tab1 = tabLayout.newTab().setText("Tus Aventuras")
        val tab2 = tabLayout.newTab().setText("Aventuras Web")
        tabLayout.addTab(tab1)
        tabLayout.addTab(tab2)
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = TabLayoutAdapter(this, supportFragmentManager, tabLayout.tabCount, fragmentAventurasLocal, fragmentAventurasWeb)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val crearAventuraBTN = findViewById(R.id.crearAventuraBTN) as FloatingActionButton
        crearAventuraBTN.setOnClickListener {
            Log.d("Miapp", "Ha pulsado crear aventura")
            CrearAventura()
        }

        val filtrarAventuras = findViewById<FloatingActionButton>(R.id.filtrarAventurasAB)
        filtrarAventuras.setOnClickListener{
            Log.d("Miapp", "pulsado filtar")
            dialogoFiltrar()
        }
    }

    private fun CrearAventura() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.crear_dialogo_layout)
        val yesBtn = dialog.findViewById(R.id.empezarCrearBTN) as Button
        val textoDialogo = dialog.findViewById(R.id.tituloDialog) as TextView
        textoDialogo.setText("Crear Aventura")

        yesBtn.setOnClickListener {
            val nombreAventuraET = dialog.findViewById(R.id.nombreAventuraDLG) as EditText
            val autorET = dialog.findViewById(R.id.AutorDLG) as EditText

            var nomavTMP =  nombreAventuraET.text.toString()
            if (nomavTMP.equals("")) nomavTMP = "Sin Nombre"

            var autorTMP =  autorET.text.toString()
            if (autorTMP.equals("")) autorTMP = "Sin Autor"

            // CREAMOS LA AVENTURA EN LA BASE DE DATOS
            val idAventura = databaseHelper.crearAventuraBD(db , nomavTMP, autorTMP, auth.currentUser!!.uid)

            val intent = Intent (this, CrearAventuraActivity::class.java).apply {
                putExtra("ID_AVENTURA", idAventura)
                putExtra("ESNUEVO" , true)
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
                0 -> fragmentAventurasLocal.filtrarLista(nombreFiltrar, autorFiltrar, soloNoPublicados)
                1 -> fragmentAventurasWeb.filtrarLista(nombreFiltrar, autorFiltrar, soloNoPublicados)
            }

            dialog.dismiss()
        }

        dialog.show()

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Log.d("Miapp" , "Usuario: " + currentUser.toString())
        if (currentUser == null) signInAnonymously()

    }

    private fun signInAnonymously() {
        // [START signin_anonymously]
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Miapp", "signInAnonymously:success")
                    usuario = auth.currentUser.toString()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Miapp", "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }

            }
    }

    fun setImagenPortada (bitmap: Bitmap) {
        imagenPortada.setImageBitmap(bitmap)
    }


    private fun signOut() {
        auth.signOut()
    }

    override fun onStop() {
        super.onStop()
        signOut()
    }

    override fun LocalListItemSelected(bitmap: Bitmap) {
        imageViewPortada.setImageBitmap(bitmap)
    }


}
