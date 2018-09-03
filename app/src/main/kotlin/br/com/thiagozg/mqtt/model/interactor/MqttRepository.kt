package br.com.thiagozg.mqtt.model.interactor

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import br.com.thiagozg.mqtt.model.domain.*
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException

class MqttRepository(context: Context) {

    val mqttConnectionLiveData = MutableLiveData<Boolean>()
    val mqttMessageLiveData = MutableLiveData<MqttMessageResponse>()
    private val gson = Gson()

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
//                userName = MQTT_USERNAME
//                password = MQTT_PASSWORD.toCharArray()
                keepAliveInterval = 600
            }
        }

    fun connectMqttClient() {
        if (!mqttAndroidClient.isConnected) {
            try {
                mqttAndroidClient.connect(mqttConnectionOption, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                        mqttConnectionLiveData.value = true
                        unSubscribe(MQTT_DEFAULT_TOPIC)
                        Log.d(TAG, "Success")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        mqttConnectionLiveData.value = false
                        Log.d(TAG, "Failure $exception")
                    }
                })
                setCallbackExtended()
            } catch (e: MqttException) {
                e.printStackTrace()
                mqttConnectionLiveData.value = false
            }
        }
    }

    private fun setCallbackExtended() {
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.w(TAG, "Connected Complete")
            }

            override fun connectionLost(throwable: Throwable) {
                Log.w(TAG, "Connection Lost: ${throwable.message}")
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w(TAG, "MessageArrived - Topic: $topic \nMessage: ${mqttMessage}")
                mqttMessageLiveData.value = MqttMessageResponse(mqttMessage.toString(), true)
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w(TAG, "Delivery Complete: ${iMqttDeliveryToken.message}")
            }
        })
    }

    fun getMqttClient(): MqttAndroidClient {
        if (!mqttAndroidClient.isConnected) {
            connectMqttClient()
        }

        return mqttAndroidClient
    }

    @Throws(MqttException::class)
    fun disconnect() {
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(iMqttToken: IMqttToken) {
                    Log.d(TAG, "Successfully disconnected")
                }

                override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                    Log.d(TAG, "Failed to disconnected: " + throwable.toString())
                }
            })
        }
    }

    @Throws(MqttException::class, UnsupportedEncodingException::class)
    fun publishMessage(msg: Any, qos: Int, topic: String) {
        val encodedPayload: ByteArray = gson.toJson(msg).toByteArray(charset("UTF-8"))
        val message = MqttMessage(encodedPayload)
        message.id = 320
        message.isRetained = true
        message.qos = qos
        mqttAndroidClient.publish(topic, message)
    }


    @Throws(MqttException::class)
    fun subscribe(topic: String, qos: Int) {
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(iMqttToken: IMqttToken) {
                    Log.d(TAG, "Subscribe Successfully on $topic")
                }

                override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                    Log.e(TAG, "Subscribe Failed on $topic", throwable)
                }
            })
        }
    }

    @Throws(MqttException::class)
    fun unSubscribe(topic: String) {
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(iMqttToken: IMqttToken) {
                    mqttMessageLiveData.value = MqttMessageResponse(newMessage = false)
                    Log.d(TAG, "UnSubscribe Successfully on $topic")
                }

                override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                    Log.e(TAG, "UnSubscribe Failed on $topic", throwable)
                }
            })
        }
    }

}