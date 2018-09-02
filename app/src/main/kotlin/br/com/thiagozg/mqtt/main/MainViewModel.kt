package br.com.thiagozg.mqtt.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import br.com.thiagozg.mqtt.model.domain.*
import br.com.thiagozg.mqtt.model.interactor.MqttRepository
import br.com.thiagozg.mqtt.model.interactor.NetworkRepository
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended


class MainViewModel(val mqttRepository: MqttRepository,
                    val networkRepository: NetworkRepository) : ViewModel() {

    private val wifiConnectionLiveData = MutableLiveData<WifiConnectionStatus>()

    fun getWifiConnectionStatus() = wifiConnectionLiveData

    fun getMqttConnectionStatus() = mqttRepository.mqttConnectionLiveData

    fun connectToWifi(networkSSID: String, networkPassword: String) {
        wifiConnectionLiveData.value = networkRepository.connectWifi(networkSSID, networkPassword)
    }

    fun isConnectedToWifi() = networkRepository.hasWifiConnection()

    fun connectToMqttClient() {
        mqttRepository.connectMqttClient()
        mqttRepository.getMqttClient().setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.w(TAG, "Connected")
            }

            override fun connectionLost(throwable: Throwable) {
                Log.w(TAG, "connectionLost: ${throwable.message}")
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w(TAG, "topic: $topic \nmessage: ${mqttMessage}")
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w(TAG, "deliveryComplete: ${iMqttDeliveryToken.message}")
            }
        })
    }

    fun publishToMqttClient() {
        val mqttVo = MqttVO("OK", ConfigVO("casenio"))
        mqttRepository.publishMessage(mqttVo, 0, MQTT_TOPIC_PUBLISH)
    }

    fun subscribeToMqttClient() {
        mqttRepository.subscribe(MQTT_TOPIC, 0)
    }

}
