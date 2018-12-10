package me.kirimin.kirimin_chan_camera

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.provider.MediaStore
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.content.ContentValues
import me.kirimin.kirimin_chan_camera.EditActivity.Companion.EXTRA_URI

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_SELECT = 0
    }

    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonCamera = findViewById<Button>(R.id.button_camera)
        buttonCamera.setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                navigateToCamera()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    private fun navigateToCamera() {
        val photoName = System.currentTimeMillis().toString() + ".jpg"
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, photoName)
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intentGallery.type = "image/*"
        val intent = Intent.createChooser(intentCamera, getString(R.string.select_photo))
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intentGallery))
        startActivityForResult(intent, REQUEST_IMAGE_SELECT)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkPermissions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_SELECT -> {
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = data?.data ?: uri
                    val intent = Intent(this, EditActivity::class.java)
                    intent.putExtra(EXTRA_URI, resultUri)
                    startActivity(intent)
                }
            }
        }
    }
}