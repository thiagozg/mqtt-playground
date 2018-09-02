package br.com.thiagozg.mqtt.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.thiagozg.mqtt.R
import kotlinx.android.synthetic.main.activity_receive.*

class ReceiveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)
        bindViews()
    }

    private fun bindViews() {
        tvJson.text = intent.getStringExtra(KEY_JSON) ?: getString(R.string.empty_json)
    }

    companion object {
        const val KEY_JSON = "keyJson"
    }

}