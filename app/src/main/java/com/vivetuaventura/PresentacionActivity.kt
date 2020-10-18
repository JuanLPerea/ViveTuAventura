package com.vivetuaventura

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.vivetuaventura.Adapters.PresentaciónViewPagerAdapter
import com.vivetuaventura.modelos.OnBoardingDatos

class PresentacionActivity : AppCompatActivity() {

    var presentaciónViewPagerAdapter : PresentaciónViewPagerAdapter? = null
    var tabLayout : TabLayout? = null
    var onBoardingViewPager : ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentacion)

        tabLayout = findViewById(R.id.presentacionTabLayout)

        val onBoardingDatos : MutableList<OnBoardingDatos> = ArrayList()
        onBoardingDatos.add(OnBoardingDatos(R.drawable.pantalla1))
        onBoardingDatos.add(OnBoardingDatos(R.drawable.pantalla2))


        setOnBoardingViewPagerAdapter(onBoardingDatos)


    }

    private fun setOnBoardingViewPagerAdapter(onBoardingDatos : List <OnBoardingDatos>) {

        onBoardingViewPager = findViewById(R.id.onBoardingViewPager)
        presentaciónViewPagerAdapter = PresentaciónViewPagerAdapter(this, onBoardingDatos)
        onBoardingViewPager!!.adapter = presentaciónViewPagerAdapter
        tabLayout?.setupWithViewPager(onBoardingViewPager)



    }
}