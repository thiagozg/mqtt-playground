package br.com.thiagozg.mqtt.model.interactor

import android.content.Context
import android.util.Log
import br.com.thiagozg.mqtt.model.domain.MQTT_BROKER_URL
import br.com.thiagozg.mqtt.model.domain.MQTT_PASSWORD
import br.com.thiagozg.mqtt.model.domain.MQTT_USERNAME
import br.com.thiagozg.mqtt.model.domain.TAG
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException

class MqttRepository(context: Context) {

    private val mqttAndroidClient: MqttAndroidClient =
            MqttAndroidClient(context, MQTT_BROKER_URL, MqttClient.generateClientId())

    private val disconnectedBufferOptions: DisconnectedBufferOptions
        get() {
            return DisconnectedBufferOptions().apply {
                isBufferEnabled = true
                bufferSize = 100
                isPersistBuffer = false
                isDeleteOldestMessages = false
            }
        }

    private val mqttConnectionOption: MqttConnectOptions
        get() {
            return MqttConnectOptions().apply {
                isCleanSession = false
                isAutomaticReconnect = true
                userName = MQTT_USERNAME
                password = MQTT_PASSWORD.toCharArray()
            }
        }

    fun connectMqttClient(): Boolean {
        var isConnectedSuccessed = true
        if (!mqttAndroidClient.isConnected) {
            try {
                val token = mqttAndroidClient.connect(mqttConnectionOption)
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                        isConnectedSuccessed = true
                        Log.d(TAG, "Success")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        isConnectedSuccessed = false
                        Log.d(TAG, "Failure " + exception.toString())
                    }
                }
            } catch (e: MqttException) {
                e.printStackTrace()
                isConnectedSuccessed = false
            }
        }

        return isConnectedSuccessed
    }

    fun getMqttClient(): MqttAndroidClient {
        if (!mqttAndroidClient.isConnected) {
            connectMqttClient()
        }

        return mqttAndroidClient
    }

    @Throws(MqttException::class)
    fun disconnect() {
        val mqttToken = mqttAndroidClient.disconnect()
        mqttToken.actionCallback = object : IMqttActionListener {
            override fun onSuccess(iMqttToken: IMqttToken) {
                Log.d(TAG, "Successfully disconnected")
            }

            override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString())
            }
        }
    }


    @Throws(MqttException::class, UnsupportedEncodingException::class)
    fun publishMessage(msg: String, qos: Int, topic: String) {
        val encodedPayload: ByteArray = msg.toByteArray(charset("UTF-8"))
        val message = MqttMessage(encodedPayload)
        message.id = 320
        message.isRetained = true
        message.qos = qos
        mqttAndroidClient.publish(topic, message)
    }


    @Throws(MqttException::class)
    fun subscribe(topic: String, qos: Int): Boolean {
        var isSubscribeSuccessed = true
        val token = mqttAndroidClient.subscribe(topic, qos)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(iMqttToken: IMqttToken) {
                isSubscribeSuccessed = true
                Log.d(TAG, "Subscribe Successfully $topic")
            }

            override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                isSubscribeSuccessed = false
                Log.e(TAG, "Subscribe Failed $topic")
            }
        }

        return isSubscribeSuccessed
    }

    @Throws(MqttException::class)
    fun unSubscribe(topic: String) {
        val token = mqttAndroidClient.unsubscribe(topic)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(iMqttToken: IMqttToken) {
                Log.d(TAG, "UnSubscribe Successfully $topic")
            }

            override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                Log.e(TAG, "UnSubscribe Failed $topic")
            }
        }
    }

}