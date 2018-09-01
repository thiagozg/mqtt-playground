package br.com.thiagozg.mqqt.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager

class MainViewModel(context: Context) : ViewModel() {

//    private val networkRepository by lazy {
//        NetworkRepository()
//    }

    private val wifiServiceManager: WifiManager by lazy {
        context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val wifiConnectionManager: ConnectivityManager by lazy {
        context.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val connectionStatus = MutableLiveData<Boolean>()
    private val progressStatus = MutableLiveData<Boolean>()

    fun getConnectionStatus() = connectionStatus
    fun getProgressStatus() = progressStatus

    private fun activeWifi() {
        if (!wifiServiceManager.isWifiEnabled) {
            Thread.sleep(1000L)
            wifiServiceManager.isWifiEnabled = true
        }
    }

    fun connectToWifi(networkSSID: String, networkPassword: String) {
        progressStatus.value = true
        activeWifi()

        val conf = WifiConfiguration()
        conf.SSID = "\"$networkSSID\""
        conf.preSharedKey = "\"$networkPassword\""
        conf.status = WifiConfiguration.Status.ENABLED
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)

        wifiServiceManager.isWifiEnabled = true
        var netId = wifiServiceManager.addNetwork(conf)
        if (netId == -1) {
            netId = getExistingNetworkId(conf.SSID)
        }
        wifiServiceManager.disconnect()
        wifiServiceManager.enableNetwork(netId, true)
        wifiServiceManager.reconnect()

        Thread.sleep(4500)
        connectionStatus.value = isConnectedToWifi()
        progressStatus.value = false
    }

    private fun getExistingNetworkId(SSID: String): Int {
        val configuredNetworks = wifiServiceManager.configuredNetworks
        if (configuredNetworks != null) {
            for (existingConfig in configuredNetworks) {
                if (existingConfig.SSID == SSID) {
                    return existingConfig.networkId
                }
            }
        }
        return -1
    }

    private fun isConnectedToWifi(): Boolean {
        val networks = wifiConnectionManager.allNetworks
        if (networks != null && !networks.isEmpty()) {
            for (i in 0 until networks.size) {
                val ntkInfo = wifiConnectionManager.getNetworkInfo(networks[i])
                if (ntkInfo.type == ConnectivityManager.TYPE_WIFI && ntkInfo.isConnected) {
                    val connectionInfo = wifiServiceManager.connectionInfo
                    return connectionInfo != null
                }
            }
        }

        return false
    }

}
