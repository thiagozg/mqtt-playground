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

class MqttRepository(private val context: Context) {

    private lateinit var mqttAndroidClient: MqttAndroidClient

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

    fun connectMqttClient(clientId: String = MqttClient.generateClientId()): MqttAndroidClient {
        mqttAndroidClient = MqttAndroidClient(context, MQTT_BROKER_URL, clientId)
        try {
            val token = mqttAndroidClient.connect(mqttConnectionOption)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                    Log.d(TAG, "Success")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(TAG, "Failure " + exception.toString())
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        return mqttAndroidClient
    }


    @Throws(MqttException::class)
    fun disconnect(client: MqttAndroidClient) {
        val mqttToken = client.disconnect()
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
    fun publishMessage(client: MqttAndroidClient, msg: String, qos: Int, topic: String) {
        val encodedPayload: ByteArray = msg.toByteArray(charset("UTF-8"))
        val message = MqttMessage(encodedPayload)
        message.id = 320
        message.isRetained = true
        message.qos = qos
        client.publish(topic, message)
    }


    @Throws(MqttException::class)
    fun subscribe(client: MqttAndroidClient, topic: String, qos: Int) {
        val token = client.subscribe(topic, qos)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(iMqttToken: IMqttToken) {
                Log.d(TAG, "Subscribe Successfully $topic")
            }

            override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                Log.e(TAG, "Subscribe Failed $topic")
            }
        }
    }


    @Throws(MqttException::class)
    fun unSubscribe(client: MqttAndroidClient, topic: String) {
        val token = client.unsubscribe(topic)
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