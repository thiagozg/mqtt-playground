package br.com.thiagozg.mqtt.di.modules

import android.content.Context
import br.com.thiagozg.mqtt.MqttApplication
import br.com.thiagozg.mqtt.di.scopes.PerActivity
import br.com.thiagozg.mqtt.di.scopes.PerApplication
import br.com.thiagozg.mqtt.model.interactor.MqttRepository
import br.com.thiagozg.mqtt.model.interactor.NetworkRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AndroidModule {

    @Provides
    @Singleton
    fun provideContext(application: MqttApplication) = application.applicationContext

    @Provides
    @Singleton
    fun providesNetworkRepository(context: Context) = MqttRepository(context)

    @Provides
    @Singleton
    fun providesMqttRepository(context: Context) = NetworkRepository(context)

}
