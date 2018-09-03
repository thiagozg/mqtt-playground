package br.com.thiagozg.mqtt.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import br.com.thiagozg.mqtt.model.domain.*
import br.com.thiagozg.mqtt.model.interactor.MqttRepository
import br.com.thiagozg.mqtt.model.interactor.NetworkRepository


class MainViewModel(val mqttRepository: MqttRepository,
                    val networkRepository: NetworkRepository) : ViewModel() {

    private val wifiConnectionLiveData = MutableLiveData<WifiConnectionStatus>()
    private var topic: String = MQTT_DEFAULT_TOPIC

    fun getWifiConnectionStatus() = wifiConnectionLiveData

    fun getMqttConnectionStatus() = mqttRepository.mqttConnectionLiveData

    fun getMqttMessage() = mqttRepository.mqttMessageLiveData

    fun connectToWifi(networkSSID: String, networkPassword: String) {
        wifiConnectionLiveData.value = networkRepository.connectWifi(networkSSID, networkPassword)
    }

    fun isConnectedToWifi() = networkRepository.hasWifiConnection()

    fun connectToMqttClient() {
        mqttRepository.connectMqttClient()
    }

    fun subscribeToMqttClient(topic: String) {
        this.topic = topic
        mqttRepository.subscribe(topic, 0)
    }

    fun unsubscribeOfMqttClient() {
        mqttRepository.unSubscribe(this.topic)
    }

    fun publishToMqttClient(status: String = "OK") {
        val msg = status.replace(" \n\t".toRegex(), "")
        val mqttVo = MqttVO(msg, ConfigVO("casenio"))
        mqttRepository.publishMessage(mqttVo, 0, topic)
    }

}
