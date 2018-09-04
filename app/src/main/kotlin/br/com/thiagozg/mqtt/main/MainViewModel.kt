package br.com.thiagozg.mqtt.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.NetworkInfo
import android.util.Log
import br.com.thiagozg.mqtt.R.string.topic
import br.com.thiagozg.mqtt.model.domain.*
import br.com.thiagozg.mqtt.model.interactor.MqttRepository
import br.com.thiagozg.mqtt.model.interactor.NetworkRepository
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings.timeout
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.util.HalfSerializer.onComplete
import io.reactivex.schedulers.Schedulers.io
import java.util.concurrent.TimeUnit


class MainViewModel(val mqttRepository: MqttRepository,
                    val networkRepository: NetworkRepository) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val wifiLiveData = MutableLiveData<WifiConnectionStatus>()
    private val loadingLiveData = MutableLiveData<Boolean>()
    private var topic: String = MQTT_DEFAULT_TOPIC

    fun getWifiConnectionStatus() = wifiLiveData

    fun getLoadingStatus() = loadingLiveData

    fun getMqttConnectionStatus() = mqttRepository.connectionLiveData

    fun getMqttMessage() = mqttRepository.messageLiveData

    fun activeWifi()  = networkRepository.activeWifi()

    fun isConnectedToWifi() = networkRepository.hasWifiConnection()

    fun connectToWifi(context: Context, networkSSID: String, networkPassword: String) {
        disposables.add(
            networkRepository.connectWifi(networkSSID, networkPassword)
                .subscribeOn(io())
                .observeOn(mainThread())
                .doOnSubscribe { loadingLiveData.postValue(true) }
                .doAfterTerminate { loadingLiveData.postValue(false) }
                .timeout(3L, TimeUnit.MINUTES)
                .flatMapSingle<Connectivity> { observableNetwork(context, networkSSID) }
                .subscribe( { connectivity ->
//                    if (connectivity.extraInfo().cleanNetworkInfo() == networkSSID) {
                        onResponse(true, networkSSID)
//                    } else {
//                        onResponse(false, isChanged = true)
//                    }
                }, {
                    onResponse(false)
                    Log.e(TAG, it.message, it)
                })
        )
    }

    private fun observableNetwork(context: Context, networkSSID: String): Single<Connectivity>? {
        return ReactiveNetwork.observeNetworkConnectivity(context)
            .filter { it.detailedState().name == NetworkInfo.State.CONNECTED.name }
            .filter { it.extraInfo().cleanNetworkInfo() == networkSSID }
            .firstOrError()
    }

    private fun String.cleanNetworkInfo() = replace("\"", "")

    fun connectToMqttClient() = mqttRepository.connectMqttClient()

    fun disconnectOfMqttClient() = mqttRepository.disconnect()

    fun subscribeToMqttClient(topic: String) {
        this.topic = topic
        mqttRepository.subscribe(topic, 0)
    }

    fun unsubscribeOfMqttClient() = mqttRepository.unSubscribe(this.topic)

    fun publishToMqttClient(status: String = "OK") {
        val msg = status.replace("\n", "").trim()
        val mqttVo = MqttVO(msg, ConfigVO("casenio"))
        mqttRepository.publishMessage(mqttVo, 0, topic)
    }

    override fun onCleared() {
        super.onCleared()
        clearDisposables()
    }

    fun clearDisposables() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
    }

    private fun onResponse(isConnected: Boolean, networkSSID: String = "", isChanged: Boolean = false) =
        wifiLiveData.postValue(WifiConnectionStatus(isConnected, networkSSID, isChanged))

}
