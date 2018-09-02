package br.com.thiagozg.mqtt.di.modules

import br.com.thiagozg.mqtt.di.scopes.PerActivity
import br.com.thiagozg.mqtt.main.MainActivity
import br.com.thiagozg.mqtt.main.MainModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {

    @PerActivity
    @ContributesAndroidInjector(modules = [MainModule::class])
    internal abstract fun bindMainFeature(): MainActivity

}
