package com.aventuras

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.aventuras.Adapters.PresentaciónViewPagerAdapter
import com.aventuras.modelos.OnBoardingDatos

class PresentacionActivity : AppCompatActivity() {

    lateinit var presentaciónViewPagerAdapter : PresentaciónViewPagerAdapter
    lateinit var tabLayout : TabLayout
    lateinit var onBoardingViewPager : ViewPager
    lateinit var botonAtras : TextView
    lateinit var botonSiguiente : TextView
    lateinit var botonEmpezar : Button
    val onBoardingDatos : MutableList<OnBoardingDatos> = ArrayList()

    var position = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentacion)

        supportActionBar?.hide()

         tabLayout = findViewById(R.id.presentacionTabLayout)


        onBoardingDatos.add(OnBoardingDatos(R.drawable.pantalla1))
        onBoardingDatos.add(OnBoardingDatos(R.drawable.pantalla2))
        onBoardingDatos.add(OnBoardingDatos(R.drawable.pantalla1))

        botonAtras = findViewById (R.id.textViewAtrasPresentacion)
        botonSiguiente = findViewById (R.id.textViewSiguientePresentacion)
        botonEmpezar = findViewById(R.id.buttonPresentacionEmpezar)


        onBoardingViewPager = findViewById(R.id.onBoardingViewPager)
        presentaciónViewPagerAdapter = PresentaciónViewPagerAdapter(applicationContext, onBoardingDatos)
        onBoardingViewPager.adapter = presentaciónViewPagerAdapter
        tabLayout?.setupWithViewPager(onBoardingViewPager)

        botonEmpezar.setOnClickListener {
            finish()
        }


        botonSiguiente.setOnClickListener {
            eventoPosicion(onBoardingViewPager.currentItem + 1)
        }

        botonAtras.setOnClickListener {
            eventoPosicion(onBoardingViewPager.currentItem - 1)
        }


        onBoardingViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object :
        TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d("Miapp" , "Tab selected")

                eventoPosicion(onBoardingViewPager.currentItem)

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.d("Miapp" , "Tab unselected")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.d("Miapp" , "Tab Reselected")
            }
        })


    }

    private fun eventoPosicion(posicionNueva : Int) {
        when (posicionNueva){
            0 -> {
                onBoardingViewPager.currentItem = 0
                loadFirstScreen()
            }
            in 1 .. onBoardingDatos.size-2 -> {
                onBoardingViewPager.currentItem = posicionNueva
                loadScreen()
            }
            onBoardingDatos.size-1 -> {
                onBoardingViewPager.currentItem = onBoardingDatos.size - 1
                loadLastScreen()
            }
        }
    }

    private fun loadLastScreen() {
        tabLayout.visibility = View.INVISIBLE
        botonAtras.visibility = View.INVISIBLE
        botonSiguiente.visibility = View.INVISIBLE
        botonEmpezar.visibility = View.VISIBLE
    }

    private fun loadFirstScreen() {
        tabLayout.visibility = View.VISIBLE
        botonAtras.visibility = View.INVISIBLE
        botonSiguiente.visibility = View.VISIBLE
        botonEmpezar.visibility = View.INVISIBLE
    }

    private fun loadScreen() {
        tabLayout.visibility = View.VISIBLE
        botonAtras.visibility = View.VISIBLE
        botonSiguiente.visibility = View.VISIBLE
        botonEmpezar.visibility = View.INVISIBLE
    }


}