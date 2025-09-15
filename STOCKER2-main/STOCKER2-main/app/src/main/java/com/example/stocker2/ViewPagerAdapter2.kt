package com.example.stocker2

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter2(activity: AppCompatActivity, private val idSuper: String) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        HomeFragment.newInstance(idSuper),
        IngresoProductoFragment.newInstance(idSuper),
        PerfilSuperFragment.newInstance(idSuper)
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
