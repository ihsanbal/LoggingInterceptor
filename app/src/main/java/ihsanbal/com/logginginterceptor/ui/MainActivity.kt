package ihsanbal.com.logginginterceptor.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ihsanbal.com.logginginterceptor.R
import ihsanbal.com.logginginterceptor.api.Api
import ihsanbal.com.logginginterceptor.base.BaseCompatActivity
import ihsanbal.com.logginginterceptor.model.Body
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.*
import javax.inject.Inject

class MainActivity : BaseCompatActivity() {

    private var outputFile: File? = null

    @Inject
    lateinit var api: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector?.inject(this)
        setContentView(R.layout.activity_main)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        button_post.setOnClickListener { callPost() }
        button_zip.setOnClickListener { callZip() }
        button_get.setOnClickListener { callGet() }
        button_delete.setOnClickListener { callDelete() }
        button_patch.setOnClickListener { callPatch() }
        button_put.setOnClickListener { callPut() }
        button_pdf.setOnClickListener { callPdf() }
        button_pdf_upload.setOnClickListener { callUpload() }
    }

    fun callPost() {
        api.post(Body())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(subscriber)
    }

    fun callZip() {
        val observablePost = api.post(Body())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
        val observableGet = api.get()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
        Observable.zip(observablePost, observableGet) { o: ResponseBody?, o1: ResponseBody? -> o }.subscribe(subscriber)
    }

    fun callGet() {
        api.get()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(subscriber)
    }

    fun callDelete() {
        api.delete()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(subscriber)
    }

    fun callPatch() {
        api.patch("q2")
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(subscriber)
    }

    fun callPut() {
        api.put()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(subscriber)
    }

    fun callPdf() {
        if (checkPermission()) {
            api.pdf()
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribeOn(Schedulers.io())
                    ?.subscribe {
                        try {
                            downloadFile(it!!)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
        } else {
            requestPermission()
        }
    }

    fun callUpload() {
        if (outputFile == null) {
            Toast.makeText(this, "Click 'File' for create file", Toast.LENGTH_SHORT).show()
            return
        }
        val requestFile = RequestBody.create(
                "application/pdf".toMediaTypeOrNull(),
                outputFile!!
        )
        val body: MultipartBody.Part = MultipartBody.Part.createFormData("picture", outputFile!!.name, requestFile)
        val descriptionString = "hello, this is description speaking"
        val description = RequestBody.create(
                MultipartBody.FORM, descriptionString)
        api.post(description, body)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(object : Observer<ResponseBody?> {
                    override fun onCompleted() {}
                    override fun onError(e: Throwable) {}
                    override fun onNext(responseBody: ResponseBody?) {}
                })
    }

    private val subscriber: Observer<in ResponseBody?>
        get() = object : Observer<ResponseBody?> {
            override fun onCompleted() {}
            override fun onError(e: Throwable) {}
            override fun onNext(responseBody: ResponseBody?) {
                try {
                    Log.w("onNext", responseBody?.string())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Companion.PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPdf()
            } else {
                Toast.makeText(applicationContext, "Permission Dained", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Companion.PERMISSION_REQUEST_CODE)
    }

    @Throws(IOException::class)
    private fun downloadFile(body: ResponseBody) {
        var count: Int
        val data = ByteArray(1024 * 4)
        val bis: InputStream = BufferedInputStream(body.byteStream(), 1024 * 8)
        outputFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file.zip")
        val output: OutputStream = FileOutputStream(outputFile)
        while (bis.read(data).also { count = it } != -1) {
            output.write(data, 0, count)
        }
        output.flush()
        output.close()
        bis.close()
        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(Uri.fromFile(outputFile), "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        val intent = Intent.createChooser(target, "Open File")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1000
    }
}