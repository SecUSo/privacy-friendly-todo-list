/*
Privacy Friendly To-Do List
Copyright (C) 2017-2024  Sebastian Lutz

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlytodolist.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr

/**
 * Created by Sebastian Lutz on 06.12.2017.
 *
 * This Activity sets up the tutorial that shall appear for the first start of the app.
 */
class TutorialActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button
    private lateinit var layouts: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make notification bar transparent
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.activity_tutorial)
        viewPager = findViewById(R.id.view_pager)
        dotsLayout = findViewById(R.id.layoutDots)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)

        //add slides to layouts array
        layouts = intArrayOf(
            R.layout.tutorial_slide1,
            R.layout.tutorial_slide2,
            R.layout.tutorial_slide3,
            R.layout.tutorial_slide4,
            R.layout.tutorial_slide5
        )

        //add bottom dots
        addBottomDots(0)

        //change status bar to transparent
        changeStatusBarColor()
        viewPager.setAdapter(MyViewPageAdapter())
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        btnSkip.setOnClickListener { launchHomeScreen() }
        btnNext.setOnClickListener {
            // checking for last page
            // if last page home screen will be launched
            val current = viewPager.currentItem + 1
            if (current < layouts.size) {
                // move to next screen
                viewPager.setCurrentItem(current)
            } else {
                launchHomeScreen()
            }
        }
    }

    private fun launchHomeScreen() {
        PreferenceMgr.setFirstTimeLaunch(this, false)
        val intent = Intent(this@TutorialActivity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            // change button text 'NEXT' on last slide to 'GOT IT'
            if (position == layouts.size - 1) {
                // last slide
                btnNext.setText(R.string.okay)
                btnSkip.visibility = View.GONE
            } else {
                // not last slide reached yet
                btnNext.setText(R.string.next)
                btnSkip.visibility = View.VISIBLE
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    private fun addBottomDots(currentPage: Int) {
        val colorActive = ContextCompat.getColor(this, R.color.dotActive)
        val colorInactive = ContextCompat.getColor(this, R.color.dotInactive)
        dotsLayout.removeAllViews()
        for (i in layouts.indices) {
            val dot = TextView(this)
            // Use Unicode character "Bullet" (decimal code 8226) as text.
            dot.text = 8226.toChar().toString()
            dot.textSize = 35f
            dot.setTextColor(if (i == currentPage) colorActive else colorInactive)
            dotsLayout.addView(dot)
        }
    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    private inner class MyViewPageAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, anObject: Any): Boolean {
            return view === anObject
        }

        override fun destroyItem(container: ViewGroup, position: Int, anObject: Any) {
            val view = anObject as View
            container.removeView(view)
        }
    }
}
