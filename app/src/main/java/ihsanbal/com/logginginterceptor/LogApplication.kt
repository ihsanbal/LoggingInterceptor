package ihsanbal.com.logginginterceptor

import android.app.Application
import ihsanbal.com.logginginterceptor.di.DaggerNetComponent
import ihsanbal.com.logginginterceptor.di.NetComponent
import ihsanbal.com.logginginterceptor.di.NetModule

/**
 * @author ihsan on 09/02/2017.
 */
class LogApplication : Application() {

    var appComponent: NetComponent? = null

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerNetComponent.builder()
                .netModule(NetModule("http://postman-echo.com/", assets))
                .build()
    }

}