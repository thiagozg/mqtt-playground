package br.com.thiagozg.mqtt.main

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import br.com.thiagozg.mqtt.R
import br.com.thiagozg.mqtt.main.ReceiveActivity.Companion.KEY_JSON
import br.com.thiagozg.mqtt.model.domain.MqttMessageResponse
import br.com.thiagozg.mqtt.model.domain.MqttVO
import br.com.thiagozg.mqtt.model.domain.WifiConnectionStatus
import com.google.gson.Gson
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.activeWifi()
        observeLoadingStatus()
        observeWifiConnection()
        observeMqttConnection()
        initViews()
    }

    private fun initViews() {
        viewModel.isConnectedToWifi().handleMqttComponents()
        btConnectWifi.setOnClickListener {
            viewModel.connectToWifi(applicationContext, etSsid.text.toString(), etPassword.text.toString())
        }
        btConnectMqtt.setOnClickListener {
            viewModel.connectToMqttClient()
        }
        btSubscribeMqtt.setOnClickListener {
            viewModel.subscribeToMqttClient(etTopic.text.toString())
            btPublishMqtt.isEnabled = true
            handleSubscribeButtons()
        }
        btUnsubiscribeMqtt.setOnClickListener {
            viewModel.unsubscribeOfMqttClient()
            btPublishMqtt.isEnabled = false
            handleSubscribeButtons()
        }
        btPublishMqtt.setOnClickListener {
            viewModel.publishToMqttClient(etMessage.text.toString())
        }
    }

    private fun handleSubscribeButtons() {
        btUnsubiscribeMqtt.isEnabled = !btUnsubiscribeMqtt.isEnabled
        btSubscribeMqtt.isEnabled = !btSubscribeMqtt.isEnabled
    }

    private fun observeLoadingStatus() {
        viewModel.getLoadingStatus().observe(this, Observer<Boolean> { isLoading ->
            isLoading?.let {
                if (it) {
                    pbGoogle.visibility = View.VISIBLE
                    pbGoogle.bringToFront()
                } else {
                    pbGoogle.visibility = View.GONE
                }
            }
        })
    }

    private fun observeWifiConnection() {
        viewModel.getWifiConnectionStatus().observe(this, Observer<WifiConnectionStatus> { wifiConnection ->
            wifiConnection?.let {
                it.isConnected.run {
                    handleMqttComponents()
                    if (this) {
                        Toast.makeText(this@MainActivity,
                            getString(R.string.device_connected_to_wifi).format(it.networkName),
                            Toast.LENGTH_LONG)
                            .show()
                        viewModel.connectToMqttClient()
                    } else {
                        viewModel.disconnectOfMqttClient()
                        showRetryDialog(WIFI_CONNECTION, it.isChanged)
                        viewModel.clearDisposables()
                    }
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
                    btSubscribeMqtt.isEnabled = true
                    btConnectMqtt.isEnabled = false
                    observeMqttMessage()
                } else {
                    btSubscribeMqtt.isEnabled = false
                    btConnectMqtt.isEnabled = true
                    showRetryDialog(MQTT_CONNECTION)
                }
            }
        })
    }

    private fun observeMqttMessage() {
        viewModel.getMqttMessage().observe(this, Observer<MqttMessageResponse> { messageResponse ->
            if (messageResponse != null && messageResponse.newMessage) {
                messageResponse.message?.let { handleMqttMessage(it) }
            }
        })
    }

    private fun handleMqttMessage(message: String) {
        val mqttVO = Gson().fromJson(message, MqttVO::class.java)
        if (mqttVO.status.equals(MESSAGE_RESULT_OK, true)) {
            val intent = Intent(this, ReceiveActivity::class.java).apply {
                putExtra(KEY_JSON, message)
            }
            viewModel.unsubscribeOfMqttClient()
            startActivity(intent)
        } else {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.atention))
                .setMessage(getString(R.string.message_publish_retry_with_ok))
                .show()
        }
    }

    private fun showRetryDialog(connectionType: Int, isChanged: Boolean = false) {
        val message = getString(
            if (!isChanged) R.string.device_not_connected
            else R.string.turn_back_ssid
        )

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .setNeutralButton(getString(R.string.retry)) { dialog, _ ->
                    dialog.dismiss()
                    when(connectionType) {
                        WIFI_CONNECTION ->
                            viewModel.connectToWifi(applicationContext,
                                etSsid.text.toString(), etPassword.text.toString())
                        MQTT_CONNECTION -> viewModel.connectToMqttClient()
                    }
                }.show()
    }

    private fun Boolean.handleMqttComponents() = run {
        btConnectMqtt.isEnabled = this
        etTopic.isEnabled = this
        etMessage.isEnabled = this
    }

    companion object {
        const val WIFI_CONNECTION = 0
        const val MQTT_CONNECTION = 1
        const val MESSAGE_RESULT_OK = "OK"
    }
}
