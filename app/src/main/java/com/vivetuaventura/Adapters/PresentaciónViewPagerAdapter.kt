package com.vivetuaventura.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.vivetuaventura.R
import com.vivetuaventura.modelos.OnBoardingDatos

class PresentaciónViewPagerAdapter(private var context : Context, private var listaDatos : List<OnBoardingDatos>) : PagerAdapter() {
    override fun getCount(): Int {
       return listaDatos.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.onboarding_screen_layout, null)
        val imageView:ImageView

        imageView = view.findViewById(R.id.imageViewOnBoarding)

        imageView.setImageResource(listaDatos[position].imageUrl)

        container.addView(view)
        return view
    }

}