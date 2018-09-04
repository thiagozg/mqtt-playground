package br.com.thiagozg.mqtt.model.domain

class WifiConnectionStatus(
    var isConnected: Boolean = false,
    var networkName: String = "",
    val isChanged: Boolean = false
)
