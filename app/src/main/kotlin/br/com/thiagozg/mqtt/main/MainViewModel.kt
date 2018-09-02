package br.com.thiagozg.mqtt.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.provider.SyncStateContract
import br.com.thiagozg.mqtt.model.interactor.MqttRepository
import br.com.thiagozg.mqtt.model.interactor.NetworkRepository
import br.com.thiagozg.mqtt.model.domain.WifiConnectionStatus
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MainViewModel(val mqttRepository: MqttRepository,
                    val networkRepository: NetworkRepository) : ViewModel() {

//    private val networkRepository by lazy {
//        NetworkRepository()
//    }

    private lateinit var mqttAndroidClient: MqttAndroidClient

    private val wifiConnectionLiveData = MutableLiveData<WifiConnectionStatus>()
    private val mqttConnectionLiveData = MutableLiveData<Boolean>()

    fun getWifiConnectionStatus() = wifiConnectionLiveData

    fun getMqttConnectionStatus() = mqttConnectionLiveData

    fun connectToWifi(networkSSID: String, networkPassword: String) {
        wifiConnectionLiveData.value = networkRepository.connectWifi(networkSSID, networkPassword)
    }

    fun isConnectedToWifi() = networkRepository.hasWifiConnection()

    fun connectToMqttClient(context: Context) {
        mqttRepository.connectMqttClient()
//        val clientId = MqttClient.generateClientId()
//        mqttAndroidClient = MqttAndroidClient(context.applicationContext,
//                "tcp://m14.cloudmqtt.com",
//                clientId)
//
//        try {
//            val token = mqttAndroidClient.connect(getMqttConnectionOption())
//            token.actionCallback = object : IMqttActionListener {
//                override fun onSuccess(asyncActionToken: IMqttToken) {
//                    mqttConnectionLiveData.value = true
//                }
//
//                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
//                    mqttConnectionLiveData.value = false
//                }
//            }
//        } catch (e: MqttException) {
//            e.printStackTrace()
//            mqttConnectionLiveData.value = false
//        }
    }

    private fun getMqttConnectionOption() =
            MqttConnectOptions().apply {
                isCleanSession = false
                isAutomaticReconnect = true
                setWill(SyncStateContract.Constants.DATA, "I am going offline".toByteArray(), 1, true)
                userName = "fwifmtlv"
                password = "41EuBEDShpPN ".toCharArray()
            }

}
