package br.com.thiagozg.mqtt.main

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import br.com.thiagozg.mqtt.R
import br.com.thiagozg.mqtt.main.ReceiveActivity.Companion.KEY_JSON
import br.com.thiagozg.mqtt.model.domain.WifiConnectionStatus
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observeWifiConnection()
        observeMqttConnection()
        initViews()
    }

    private fun initViews() {
        viewModel.isConnectedToWifi().handleButtons()

        btConnectWifi.setOnClickListener {
            // TODO : corrigir isso trabalhando com uma thread secundaria
            pbGoogle.visibility = View.VISIBLE
            pbGoogle.bringToFront()

            // FIXME : remover isso depois
            viewModel.connectToWifi("RB-5", "residencialrb")
//            viewModel.connectToWifi(etSsid.text.toString(), etPassword.text.toString())
        }

        btConnectMqtt.setOnClickListener {
            viewModel.connectToMqttClient()
        }

        btPublishMqtt.setOnClickListener {
            viewModel.publishToMqttClient()
        }
    }

    private fun observeWifiConnection() {
        viewModel.getWifiConnectionStatus().observe(this, Observer<WifiConnectionStatus> { wifiConnection ->
            pbGoogle.visibility = View.GONE // FIXME: not working
            wifiConnection?.let {
                it.isConnected.handleButtons()

                if (it.isConnected) {
                    Toast.makeText(this,
                            getString(R.string.device_connected_to_wifi).format(it.networkName),
                            Toast.LENGTH_LONG)
                            .show()
                    viewModel.connectToMqttClient()
                } else {
                    showRetryDialog(getString(R.string.retry), WIFI_CONNECTION)
                }
            }
        })
    }

    private fun observeMqttConnection() {
        viewModel.getMqttConnectionStatus().observe(this, Observer<Boolean> { mqttConnection ->
            mqttConnection?.let {
                if (it) {
                    Toast.makeText(this,
                        getString(R.string.device_connected_to_mqtt),
                        Toast.LENGTH_LONG)
                        .show()
                } else {
                    showRetryDialog(getString(R.string.retry), MQTT_CONNECTION)
                }
            }
        })
    }

    // TODO : chamar da viewModel a partir de um liveData
    private fun handleMqttMessage(message: String) {
//        if (message.contains())
        // TODO: parsear para Json e verificar (add dependency)
        val intent = Intent(this, ReceiveActivity::class.java).apply {
            putExtra(KEY_JSON, message)
        }
        startActivity(intent)
    }

    private fun showRetryDialog(@StringRes stringId: String, connectionType: Int) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(getString(R.string.device_not_connected))
                .setNeutralButton(stringId) { dialog, _ ->
                    dialog.dismiss()
                    when(connectionType) {
                        WIFI_CONNECTION -> viewModel.connectToWifi(etSsid.text.toString(), etPassword.text.toString())
                        MQTT_CONNECTION -> viewModel.connectToMqttClient()
                    }
                }.show()
    }

    private fun Boolean.handleButtons() = run {
        btConnectMqtt.isEnabled = this
        btPublishMqtt.isEnabled = this
    }

    companion object {
        const val WIFI_CONNECTION = 0
        const val MQTT_CONNECTION = 1
    }
}
