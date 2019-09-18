package pl.rudolf.plan.zst_plan_lekcji

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jsoup.Jsoup

class NewsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.news)
        toast("Ladowanie...")
        downloadNews()
    }
    private fun downloadNews(){
        val titles = mutableListOf<String>()
        val descriptions = mutableListOf<String>()
        var connection = true
        doAsync {
            try {
                Jsoup.connect("https://zst.net.pl/index.php/aktualnosci").get().body().select("div.art-postcontent").forEach{
                    if(it.select("img").attr("src")=="/images/stories/wykrzyknik.jpg")
                        descriptions.add(it.text())
                }

            } catch (Ex: Exception) {
                connection = false
            }

            uiThread {
                when (connection) {
                    true -> {
                        scrollView {
                            verticalLayout {
                                setBackgroundColor(Color.parseColor("#dddddd"))

                                onClick {
                                    startActivity(Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://www.zst.net.pl/index.php/aktualnosci")))
                                }
                                if (descriptions.isEmpty()){
                                    textView{
                                        text="Brak\n sensownych\n aktualnosci..."
                                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                                        textSize = 38f
                                        setBackgroundColor(Color.WHITE)
                                        padding = dip(10)
                                    }
                                }else{
                                    descriptions.forEach{
                                        verticalLayout {
                                            padding=dip(8)
                                            textView {
                                                padding=dip(12)
                                                textSize = 16f
                                                setBackgroundColor(Color.WHITE)
                                                text = it
                                            }
                                        }
                                    }
                                    val btn=button {
                                        setBackgroundColor(Color.WHITE)
                                        gravity = Gravity.BOTTOM
                                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                                        padding = dip(8)
                                        text=". . ."
                                    }
                                    var x=0
                                    btn.onClick {
                                        x++
                                        if (x==2){
                                            imageView{
                                                imageResource = R.drawable.cheeki
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    false -> {
                        verticalLayout {
                            textView{
                                gravity= Gravity.CENTER
                                text=getString(R.string.loading)
                                textSize=48f
                            }
                        }
                        longToast("Sprawdz połaczenie z internetem...")
                    }
                }
            }
        }
    }
}
