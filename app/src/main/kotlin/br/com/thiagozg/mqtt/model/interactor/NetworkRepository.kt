package br.com.thiagozg.mqtt.model.interactor

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import br.com.thiagozg.mqtt.model.domain.WifiConnectionStatus
import io.reactivex.Observable
import io.reactivex.Single

class NetworkRepository(context: Context) {

    private val wifiServiceManager: WifiManager by lazy {
        context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val wifiConnectionManager: ConnectivityManager by lazy {
        context.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun activeWifi() {
        if (!wifiServiceManager.isWifiEnabled) {
            wifiServiceManager.isWifiEnabled = true
        }
    }

    fun connectWifi(networkSSID: String, networkPassword: String): Observable<WifiConnectionStatus> {
        return Observable.fromCallable {
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

            Thread.sleep(4000)
            WifiConnectionStatus(hasWifiConnection(), networkSSID)
        }
    }

    fun hasWifiConnection(): Boolean {
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

}