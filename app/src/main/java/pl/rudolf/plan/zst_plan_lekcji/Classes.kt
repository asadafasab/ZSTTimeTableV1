package pl.rudolf.plan.zst_plan_lekcji

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.widget.*
import org.jetbrains.anko.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*


class Classes : AppCompatActivity() {
    lateinit var customAdapter: ArrayAdapter<String>
    private var noConnection = true
    private var offlineTeachersBoolean = true
    private var date = ""
    private var listOfLinks = mutableListOf<String>()
    private val offlineTeachers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.action_classes)
        downloadClasses()
        longToast("Ladowanie...")
    }

    private fun downloadClasses(){
        doAsync {
            try {
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.US)
                val systemDate = dateFormat.format(Date())
                checkDate()
                val doc = Jsoup.connect("http://www.zst.net.pl/pliki/plany/$date/lista.html").get()
                val body =doc.getElementById("oddzialy").select("a")
                body.forEach{item->
                    listOfLinks.add("${item.attr("href").replace("plany/","")
                            .replace("o","")
                            .replace(".html","")} " +
                            "${item.text().replace("."," ")
                            .replace("(","")
                            .replace(")","")
                    }")
                }
                save("classes","$listOfLinks")
                save("metaT","$date/$systemDate")
            } catch (Ex: Exception) {
                listOfLinks=loadOffline()
            }
            uiThread {
                if (!offlineTeachersBoolean && noConnection) {
                    verticalLayout {
                        textView {
                            gravity= Gravity.CENTER
                            text="ლ(ಠ_ಠლ)"
                            textSize=48f
                        }
                        textView {
                            text="Sprawdź połączenie z internetem"
                            gravity= Gravity.CENTER
                            textSize=20f
                        }
                    }

                }
                else
                    render()
            }
        }
    }

    private fun save(address: String, data: String) {
        val sharedPref = getSharedPreferences("plan", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(address, data)
        editor.apply()
    }

    private fun getList(address: String): String {
        val sharedPref = getSharedPreferences("plan", Context.MODE_PRIVATE)
        return sharedPref.getString(address, "")
    }
    private fun checkDate(){
        val reg = Regex("(?<=[/])")
        var datee = false
        Jsoup.connect("http://zst.net.pl/index.php/plan-lekcji")
                .get().select("h2").forEachIndexed {i,d ->
                    if(d.text().contains("Plan lekcji od")&& !datee){
                        date=d.select("a").attr("href").split(reg)[5]
                        datee=true
                    }
                }
        noConnection=false
    }
    private fun loadOffline():MutableList<String>{
        try {
            offlineTeachers.addAll(getList("classes")
                    .replace("[","")
                    .replace("[","")
                    .split(","))
            if (offlineTeachers[0]=="")
                offlineTeachersBoolean=false
        }catch(Ex:Exception){
            offlineTeachersBoolean=false
        }
        return offlineTeachers
    }
    private fun render(){
        val lv = listView {  }
        val arrayClasses = ArrayList<String>()
        arrayClasses.addAll(listOfLinks)
        customAdapter = ArrayAdapter(this@Classes,
                android.R.layout.simple_list_item_1, arrayClasses)
        lv.adapter = customAdapter

        lv.setOnItemClickListener{parent,_,position,_->
            val tmp= parent.getItemAtPosition(position).toString()
            startActivity(Intent(this, ShowPlan::class.java)
                    .putExtra("address", "o"+tmp.split(" ")[0]+".html")
                    .putExtra("name", tmp.split(" ")[1])
                    .putExtra("date",date))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)
        val item = menu.findItem(R.id.item_search)
        val searchView = item.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                try{
                    customAdapter.filter.filter(s)
                }catch (Ex:Exception){}
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
}
