package pl.rudolf.plan.zst_plan_lekcji

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_show_plan.*
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup

import kotlinx.android.synthetic.main.fragment_main.view.*
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*

class ShowPlan : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_plan)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("name")
        downloadData()
    }

    private fun save(address: String, data: String) {
        val sharedPref = getSharedPreferences("plan", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(address, data)
        editor.apply()
    }

    private fun saveHistory(item: String) {
        val sharedPref = getSharedPreferences("plan", Context.MODE_PRIVATE)
        val historyList = sharedPref.getString("history", "")

        val editor = sharedPref.edit()
        lateinit var jsonHist:JSONArray
        var save =true

        if(historyList=="[]"||historyList=="") {
            jsonHist = JSONArray()
            jsonHist.put(item)
        }
        else{
            jsonHist = JSONArray(historyList)
            var x=0
            while (x<jsonHist.length()){
                if (item==jsonHist.getString(x)){ save=false }
                x++
            }
            when(save){
                true ->{
                    if (jsonHist.length()==7|| jsonHist.length()>7)
                        jsonHist.remove(0)
                    jsonHist.put(item)
                }
            }
        }
        editor.putString("history", jsonHist.toString())
        editor.apply()
    }

    private fun getPlan(address: String): String {
        val sharedPref = getSharedPreferences("plan", Context.MODE_PRIVATE)
        return sharedPref.getString(address, "")
    }


    private fun downloadData() {
        var date = ""
        var lessonsOffline = true
        var connection = false
        var fileLessons = JSONObject()

        doAsync {
            val address = intent.getStringExtra("address")
            try {
                fileLessons = JSONObject(getPlan(address))
                fileLessons.getString("date")
            }catch (Ex:Exception){
                lessonsOffline = false
            }

            try {
                //get current data
                date=intent.getStringExtra("date")

                when(lessonsOffline){
                    false->{
                        val doc = Jsoup.connect("http://zst.net.pl/pliki/plany/${date}plany/$address").get()
                        val body = doc.select("td.l")
                        body.select("br").append("newline")
                        fileLessons.put("date",date)
                        body.forEachIndexed{index,item ->
                            fileLessons.put("$index",item.text())
                        }
                        save(address,fileLessons.toString())
                    }
                }
                connection=true

            } catch (Ex: Exception) {

            }
            saveHistory(address + "/" + intent.getStringExtra("name"))
            uiThread {
                progressBar.visibility = View.GONE
                if (!lessonsOffline&&!connection){
                    offlineImage.visibility = View.VISIBLE
                }else{
                    mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

                    container.adapter = mSectionsPagerAdapter
                    var dayOfTheWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    when(dayOfTheWeek){
                        7->{
                           dayOfTheWeek=0
                        }
                    }
                    container.currentItem=dayOfTheWeek-2

                    container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
                    tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
                }
            }
        }
    }



    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            return 5
        }
    }
    class PlaceholderFragment : Fragment() {

        private fun loadData():String{
                return activity!!.getSharedPreferences("plan",Context.MODE_PRIVATE)
                        .getString(activity!!.intent.getStringExtra("address"), "")
        }
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            val lessonsTexts = listOf(rootView.l1,rootView.l2,rootView.l3,rootView.l4,
                    rootView.l5,rootView.l6,rootView.l7,rootView.l8,rootView.l9)
            /*val numbers = listOf(rootView.nr1,rootView.nr2,rootView.nr3,rootView.nr4,
                    rootView.nr5,rootView.nr6,rootView.nr7,rootView.nr8,rootView.nr9)*/

            try {
                var x:Int
                val dataJSON = JSONObject(loadData().replace("newline", "\n"))
                when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                    1 -> {
                        x=0
                        lessonsTexts.forEach{
                            it.text=dataJSON.getString("$x")
                            x+=5
                        }
                    }
                    2 -> {
                        x=1
                        lessonsTexts.forEach{
                            it.text=dataJSON.getString("$x")
                            x+=5
                        }
                    }
                    3 -> {
                        x=2
                        lessonsTexts.forEach{
                            it.text=dataJSON.getString("$x")
                            x+=5
                        }
                    }
                    4 -> {
                        x=3
                        lessonsTexts.forEach{
                            it.text=dataJSON.getString("$x")
                            x+=5
                        }
                    }
                    5 -> {
                        x=4
                        lessonsTexts.forEach{
                            it.text=dataJSON.getString("$x")
                            x+=5
                        }
                    }
                }
            }catch (Ex:Exception){

            }
            return rootView
        }

        companion object {
            private const val ARG_SECTION_NUMBER = "section_number"

            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
