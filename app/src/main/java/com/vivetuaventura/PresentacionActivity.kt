package com.vivetuaventura

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.vivetuaventura.Adapters.PresentaciónViewPagerAdapter
import com.vivetuaventura.modelos.OnBoardingDatos

class PresentacionActivity : AppCompatActivity() {

    lateinit var presentaciónViewPagerAdapter : PresentaciónViewPagerAdapter
    lateinit var tabLayout : TabLayout
    lateinit var onBoardingViewPager : ViewPager
    var position = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentacion)

        supportActionBar?.hide()

         tabLayout = findViewById(R.id.presentacionTabLayout)

        val onBoardingDatos : MutableList<OnBoardingDatos> = ArrayList()
        onBoardingDatos.add(OnBoardingDatos(R.drawable.pantalla1))
        onBoardingDatos.add(OnBoardingDatos(R.drawable.pantalla2))

        val botonAtras = findViewById<TextView> (R.id.textViewAtrasPresentacion)
        val botonSiguiente = findViewById<TextView> (R.id.textViewSiguientePresentacion)
        val botonEmpezar = findViewById<Button>(R.id.buttonPresentacionEmpezar)


        onBoardingViewPager = findViewById(R.id.onBoardingViewPager)
        presentaciónViewPagerAdapter = PresentaciónViewPagerAdapter(this, onBoardingDatos)
        onBoardingViewPager!!.adapter = presentaciónViewPagerAdapter
        tabLayout?.setupWithViewPager(onBoardingViewPager)


        botonSiguiente.setOnClickListener {
            position = onBoardingViewPager!!.currentItem
            if (position < onBoardingDatos.size) {
                botonAtras!!.visibility = View.VISIBLE
                position++
                onBoardingViewPager!!.currentItem = position
            }

            if (position == onBoardingDatos.size - 1) {
                    loadLastScreen()

            }
        }

        botonAtras.setOnClickListener {
            position = onBoardingViewPager!!.currentItem
            if (position == 0) {
                botonAtras.visibility = View.INVISIBLE
            }

            if (position > 0) {
                botonAtras!!.visibility = View.VISIBLE
                position--
                onBoardingViewPager!!.currentItem = position
            }
        }

        tabLayout!!.addOnTabSelectedListener ( object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                TODO("Not yet implemented")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                TODO("Not yet implemented")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                TODO("Not yet implemented")
            }
        })


    }

    private fun loadLastScreen() {
        tabLayout!!.visibility = View.INVISIBLE
        botonAtras.visibility = View.INVISIBLE
        botonSiguiente.visibility = View.INVISIBLE
        botonEmpezar!!.visibility = View.VISIBLE
    }


}