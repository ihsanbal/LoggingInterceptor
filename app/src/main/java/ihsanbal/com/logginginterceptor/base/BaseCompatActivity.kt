package ihsanbal.com.logginginterceptor.base

import androidx.appcompat.app.AppCompatActivity
import ihsanbal.com.logginginterceptor.LogApplication
import ihsanbal.com.logginginterceptor.di.NetComponent

/**
 * @author ihsan on 09/02/2017.
 */
abstract class BaseCompatActivity : AppCompatActivity() {
    val injector: NetComponent?
        get() = (application as LogApplication).appComponent
}