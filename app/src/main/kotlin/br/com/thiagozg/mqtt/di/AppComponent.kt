package br.com.thiagozg.mqtt.di

import br.com.thiagozg.mqtt.di.modules.AndroidModule
import br.com.thiagozg.mqtt.di.modules.BuildersModule
import br.com.thiagozg.mqtt.MqttApplication
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AndroidModule::class,
    BuildersModule::class]
)
interface AppComponent : AndroidInjector<MqttApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<MqttApplication>()

}
