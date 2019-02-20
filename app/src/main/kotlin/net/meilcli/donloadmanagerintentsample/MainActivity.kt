package net.meilcli.donloadmanagerintentsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val downloadService = DownloadService(applicationContext)

        button.setOnClickListener {
            downloadService.download("https://avatars0.githubusercontent.com/u/2821921?s=460&v=4", "icon.png")
        }
    }
}
