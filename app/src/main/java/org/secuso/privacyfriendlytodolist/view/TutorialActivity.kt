/*
 This file is part of Privacy Friendly To-Do List.

 Privacy Friendly To-Do List is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly To-Do List is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly To-Do List. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlytodolist.view

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    private var viewPager: ViewPager? = null
    private var dotsLayout: LinearLayout? = null
    private var btnSkip: Button? = null
    private var btnNext: Button? = null
    private var layouts: IntArray? = null
    private var myViewPageAdapter: MyViewPageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
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
        myViewPageAdapter = MyViewPageAdapter()
        viewPager!!.setAdapter(myViewPageAdapter)
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)
        btnSkip!!.setOnClickListener { launchHomeScreen() }
        btnNext!!.setOnClickListener {
            // checking for last page
            // if last page home screen will be launched
            val current = viewPager!!.currentItem + 1
            if (current < layouts!!.size) {
                // move to next screen
                viewPager!!.setCurrentItem(current)
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
            if (position == layouts!!.size - 1) {
                // last slide
                btnNext!!.setText(R.string.okay)
                btnSkip!!.visibility = View.GONE
            } else {
                // not last slide reached yet
                btnNext!!.setText(R.string.next)
                btnSkip!!.visibility = View.VISIBLE
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    private fun addBottomDots(currentPage: Int) {
        val dots: Array<TextView?> = arrayOfNulls(layouts!!.size)
        val colorsActive = getResources().getIntArray(R.array.array_dot_active)
        val colorsInactive = getResources().getIntArray(R.array.array_dot_inactive)
        dotsLayout!!.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = Html.fromHtml("&#8226;")
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(colorsInactive[currentPage])
            dotsLayout!!.addView(dots[i])
        }
        if (dots.isNotEmpty()) {
            dots[currentPage]!!.setTextColor(colorsActive[currentPage])
        }
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private inner class MyViewPageAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater!!.inflate(layouts!![position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts!!.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

    companion object {
        val TAG: String = TutorialActivity::class.java.getSimpleName()
    }
}
