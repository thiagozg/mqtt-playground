package br.com.thiagozg.mqtt.model.domain

class MqttMessageResponse(
    var message: String? = null,
    var newMessage: Boolean = false
)