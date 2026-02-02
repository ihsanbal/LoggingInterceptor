package ihsanbal.com.logginginterceptor.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ihsanbal.com.logginginterceptor.api.Api
import ihsanbal.com.logginginterceptor.base.BaseCompatActivity
import ihsanbal.com.logginginterceptor.databinding.ActivityMainBinding
import ihsanbal.com.logginginterceptor.model.Body
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.*
import javax.inject.Inject

class MainActivity : BaseCompatActivity() {

    private var outputUri: Uri? = null
    private var outputDisplayName: String? = null
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var api: Api
    private val uploadPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flags)
            uploadFromUri(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector?.inject(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        bindListeners()
    }

    private fun bindListeners() {
        binding.buttonPost.setOnClickListener { callPost() }
        binding.buttonZip.setOnClickListener { callZip() }
        binding.buttonGet.setOnClickListener { callGet() }
        binding.buttonDelete.setOnClickListener { callDelete() }
        binding.buttonPatch.setOnClickListener { callPatch() }
        binding.buttonPut.setOnClickListener { callPut() }
        binding.buttonPdf.setOnClickListener { callPdf() }
        binding.buttonPdfUpload.setOnClickListener { launchUploadPicker() }
    }

    private fun callPost() {
        api.post(Body())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ log(it) }, { logError(it) })
    }

    private fun callZip() {
        val observablePost = api.post(Body())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
        val observableGet = api.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
        Observable.zip(observablePost, observableGet) { o: ResponseBody, _: ResponseBody -> o }
                .subscribe({ response ->
                    try {
                        log(response)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { logError(it) })
    }

    private fun log(it: ResponseBody) {
        Log.w("onNext", it.string())
    }

    private fun logError(error: Throwable) {
        Log.e("onError", error.message ?: "Request failed", error)
    }

    private fun callGet() {
        api.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ log(it) }, { logError(it) })
    }

    private fun callDelete() {
        api.delete()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ log(it) }, { logError(it) })
    }

    private fun callPatch() {
        api.patch("q2")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ log(it) }, { logError(it) })
    }

    private fun callPut() {
        api.put()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ log(it) }, { logError(it) })
    }

    private fun callPdf() {
        if (checkPermission()) {
            api.pdf()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ downloadFile(it) }, { logError(it) })
        } else {
            requestPermission()
        }
    }

    private fun launchUploadPicker() {
        uploadPicker.launch(arrayOf("application/pdf"))
    }

    private fun uploadFromUri(uri: Uri) {
        val requestBody = contentResolver.openInputStream(uri)?.use { stream ->
            val bytes = stream.readBytes()
            bytes.toRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull())
        }
        if (requestBody == null) {
            Toast.makeText(this, "Unable to read selected file", Toast.LENGTH_SHORT).show()
            return
        }
        val filename = queryDisplayName(uri) ?: "upload.pdf"
        val body: MultipartBody.Part = createFormData("picture", filename, requestBody)
        val descriptionString = "hello, this is description speaking"
        api.post(descriptionString.toRequestBody(MultipartBody.FORM), body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Observer<ResponseBody> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onError(e: Throwable) {
                        logError(e)
                    }
                    override fun onComplete() {}
                    override fun onNext(responseBody: ResponseBody) {}
                })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPdf()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        }
        val result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }
    }

    @Suppress("DEPRECATION")
    @Throws(IOException::class)
    private fun downloadFile(body: ResponseBody) {
        val displayName = "file.pdf"
        outputDisplayName = displayName
        outputUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        } else {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), displayName)
            Uri.fromFile(file)
        }
        val targetUri = outputUri ?: throw IOException("Failed to create output Uri")
        contentResolver.openOutputStream(targetUri)?.use { output ->
            body.byteStream().use { input ->
                input.copyTo(output)
            }
        } ?: throw IOException("Failed to open output stream")
        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(targetUri, "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent.createChooser(target, "Open File")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return outputDisplayName
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1000
    }
}
