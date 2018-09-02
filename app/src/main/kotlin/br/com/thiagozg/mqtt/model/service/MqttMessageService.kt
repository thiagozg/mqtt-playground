package br.com.thiagozg.mqtt.model.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import br.com.thiagozg.mqtt.model.domain.TAG
import br.com.thiagozg.mqtt.model.interactor.MqttRepository
import dagger.android.AndroidInjection
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

class MqttMessageService : Service() {

    @Inject lateinit var mqttRepository: MqttRepository

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        mqttRepository.getMqttClient().setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.d(TAG, "connectComplete")
            }

            override fun connectionLost(throwable: Throwable) {
                Log.d(TAG, "connectionLost")
            }

            @Throws(Exception::class)
            override fun messageArrived(s: String, mqttMessage: MqttMessage) {
                Log.d(TAG, "messageArrived")
                // setMessageNotification(s, new String(mqttMessage.getPayload()));
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.d(TAG, "deliveryComplete")
            }
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    /*
    private void setMessageNotification(@NonNull String topic, @NonNull String msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_message_black_24dp)
                        .setContentTitle(topic)
                        .setContentText(msg);
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100, mBuilder.build());
    }
    */

}
