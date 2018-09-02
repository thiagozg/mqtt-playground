package br.com.thiagozg.mqtt.main

import br.com.thiagozg.mqtt.di.scopes.PerActivity
import br.com.thiagozg.mqtt.model.interactor.MqttRepository
import br.com.thiagozg.mqtt.model.interactor.NetworkRepository
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    @PerActivity
    fun providesMainViewModel(mqttRepository: MqttRepository,
                              networkRepository: NetworkRepository) =
            MainViewModel(mqttRepository, networkRepository)

}