package br.com.thiagozg.mqqt.main

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import br.com.thiagozg.mqqt.R
import com.jpardogo.android.googleprogressbar.library.FoldingCirclesDrawable
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = MainViewModel(this)
        observeProgressStatus()
        observeWifiConnection()
        initViews()
    }

    private fun initViews() {
        btConnect.setOnClickListener {
            // FIXME : remover isso depois
            viewModel.connectToWifi("RB-5", "residencialrb")
//            viewModel.connectToWifi(etSsid.text.toString(), etPassword.text.toString())
        }
    }

    private fun observeProgressStatus() {
        pbGoogle.setIndeterminateDrawableTiled(
                FoldingCirclesDrawable.Builder(this)
                        .colors(resources.getIntArray(R.array.color_progress))
                        .build())
        viewModel.getProgressStatus().observe(this, Observer<Boolean> { progressStatus ->
            progressStatus?.let {
                runOnUiThread {
                    if (it) {
                        pbGoogle.visibility = View.VISIBLE
                    } else {
                        pbGoogle.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun observeWifiConnection() {
        viewModel.getConnectionStatus().observe(this, Observer<Boolean> { connectionStatus ->
            connectionStatus?.let {
                if (it) {
                    Toast.makeText(this, "CONECTOU porra", Toast.LENGTH_LONG).show()
                    //  After the connection to the network specified by the user is established, the app initializes an ​MQTT
                    //​ client, connects to the broker and publishes some arbitrary JSON to some arbitrary test topic
                } else {
                    Toast.makeText(this, "CONECTOU merda nenhuma", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

}
