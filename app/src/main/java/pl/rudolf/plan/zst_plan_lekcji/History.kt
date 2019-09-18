package pl.rudolf.plan.zst_plan_lekcji

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*

import kotlinx.android.synthetic.main.activity_history.*
import org.json.JSONArray

class History : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.action_classes -> {
                startActivity(Intent(this, Classes::class.java))
            }
            R.id.action_teachers -> {
                startActivity(Intent(this, Teachers::class.java))
            }
            R.id.action_classrooms -> {
                startActivity(Intent(this, Classrooms::class.java))
            }
        }
        false
    }
    private fun firstRun(){
        val sharedPref = getSharedPreferences("plan", Context.MODE_PRIVATE)
        val data = sharedPref.getString("firstRun", "")
        if (data=="true"){
        }else{
            sharedPref.edit().clear().apply()
            sharedPref.edit().putString("firstRun", "true").apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(toolbar)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        firstRun()

        load_news.setOnClickListener {
            startActivity(Intent(this,NewsActivity::class.java))
        }

        val linearLayout: LinearLayout = findViewById(R.id.ll_history)
        lateinit var jsonHist:JSONArray
        try {
            jsonHist = JSONArray(getHistory())
        }catch (Ex:Exception){
            jsonHist = JSONArray()
        }
        var x=0
        while (x<jsonHist.length()){
            if (jsonHist.getString(x) == "") {
            } else {
                val btnShow = Button(this)
                btnShow.text = jsonHist.getString(x).split("/")[1]
                btnShow.setBackgroundResource(R.drawable.ripple)
                btnShow.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                val param = btnShow.layoutParams as LinearLayout.LayoutParams
                param.setMargins(10, 10, 10, 10)
                btnShow.layoutParams = param
                val tmp = x
                btnShow.setOnClickListener {
                    startActivity(Intent(this, ShowPlan::class.java)
                            .putExtra("address",
                                    jsonHist.getString(tmp).split("/")[0])
                            .putExtra("name",
                                    jsonHist.getString(tmp).split("/")[1]))
                }
                btnShow.setOnLongClickListener{
                    modifyHistory(jsonHist.getString(tmp),btnShow)
                }
                linearLayout.addView(btnShow)
            }
            x++
        }
    }

    private fun modifyHistory(item:String,btnShow:Button):Boolean{
        val removeAlert = AlertDialog.Builder(this@History)
        with(removeAlert){
            setTitle("Usunąć element?")
            setNegativeButton("nie"){ dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("tak"){ dialog, _ ->
                val sharedPref = getSharedPreferences("plan", Context.MODE_PRIVATE)
                val historyList = JSONArray(sharedPref.getString("history", ""))
                val editor = sharedPref.edit()
                var x = 0

                while (x<historyList.length()){
                    if (historyList.getString(x)==item)
                        historyList.remove(x)
                    x++
                }
                editor.putString("history", historyList.toString())
                editor.apply()
                dialog.dismiss()
                btnShow.visibility = View.GONE
            }
        }
        removeAlert.create().show()
        return true
    }

    private fun getHistory():String {
        val sharedPref = getSharedPreferences("plan", Context.MODE_PRIVATE)
        return sharedPref.getString("history", "")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_other, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_www) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zst.net.pl")))
        } else if (id == R.id.action_about) {
            val alertDialog = AlertDialog.Builder(this@History)
            alertDialog.setTitle("4b0ut")
            alertDialog.setMessage("4pplic4ti0n cre4ted by:\nDominik Przeor and Kiwav(Kropidło)\n\nth1s applic4ti0n is 0p3n s0urce\n4nd 1s wr1tt3n in k0tlin\nst1ll_in_d3vel0pment")
            alertDialog.create().show()
        }
        return super.onOptionsItemSelected(item)
    }
}