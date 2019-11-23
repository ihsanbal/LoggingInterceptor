package ihsanbal.com.logginginterceptor.di

import dagger.Component
import ihsanbal.com.logginginterceptor.ui.MainActivity
import javax.inject.Singleton

/**
 * @author ihsan on 09/02/2017.
 */
@Singleton
@Component(modules = [NetModule::class])
interface NetComponent {
    fun inject(activity: MainActivity?)
}