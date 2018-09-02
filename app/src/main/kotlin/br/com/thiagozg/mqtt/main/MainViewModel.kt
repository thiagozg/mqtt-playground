package br.com.thiagozg.mqtt.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import br.com.thiagozg.mqtt.model.domain.MQTT_TOPIC
import br.com.thiagozg.mqtt.model.domain.WifiConnectionStatus
import br.com.thiagozg.mqtt.model.interactor.MqttRepository
import br.com.thiagozg.mqtt.model.interactor.NetworkRepository


class MainViewModel(val mqttRepository: MqttRepository,
                    val networkRepository: NetworkRepository) : ViewModel() {

    private val wifiConnectionLiveData = MutableLiveData<WifiConnectionStatus>()
    private val mqttConnectionLiveData = MutableLiveData<Boolean>()

    fun getWifiConnectionStatus() = wifiConnectionLiveData

    fun getMqttConnectionStatus() = mqttConnectionLiveData

    fun connectToWifi(networkSSID: String, networkPassword: String) {
        wifiConnectionLiveData.value = networkRepository.connectWifi(networkSSID, networkPassword)
    }

    fun isConnectedToWifi() = networkRepository.hasWifiConnection()

    fun connectToMqttClient() {
        mqttConnectionLiveData.value = mqttRepository.connectMqttClient()
    }

    fun publishToMqttClient() {
        mqttRepository.publishMessage("Connected!", 1, MQTT_TOPIC)
    }

    fun subscribeToMqttClient() {
        mqttRepository.subscribe(MQTT_TOPIC, 1)
    }

}
