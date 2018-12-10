package me.kirimin.kirimin_chan_camera

import android.content.ContentValues
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import android.view.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_edit.*

class EditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URI = "uri"
        const val SAVE_DIR = "/kirimin-chan-camera/"

        val frameList = listOf(
                R.drawable.frame1,
                R.drawable.frame2,
                R.drawable.frame3,
                R.drawable.frame4,
                R.drawable.frame5,
                R.drawable.frame6,
                R.drawable.frame7,
                R.drawable.frame8,
                R.drawable.frame9,
                R.drawable.frame10,
                R.drawable.frame11,
                R.drawable.frame12
        )

        private fun computeBitmapSizeFromDynamicImageLayer(imageLayer: ImageView): Point {
            val actualHeight: Int
            val actualWidth: Int
            val imageLayerHeight = imageLayer.height
            val imageLayerWidth = imageLayer.width
            val bitmapHeight = imageLayer.drawable.intrinsicHeight
            val bitmapWidth = imageLayer.drawable.intrinsicWidth
            if (imageLayerHeight * bitmapWidth <= imageLayerWidth * bitmapHeight) {
                actualWidth = bitmapWidth * imageLayerHeight / bitmapHeight
                actualHeight = imageLayerHeight
            } else {
                actualHeight = bitmapHeight * imageLayerWidth / bitmapWidth
                actualWidth = imageLayerWidth
            }
            return Point(actualWidth, actualHeight)
        }

        private fun computeCenter(frameSize: Int, objectSize: Int) = (frameSize.toFloat() - objectSize) / 2

    }

    private var preDx: Int = 0
    private var preDy: Int = 0

    private var scale = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        photoImageView.setImageURI(intent.getParcelableExtra(EXTRA_URI))

        val detector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scale *= detector.scaleFactor
                frameImageVIew.scaleX = scale
                frameImageVIew.scaleY = scale
                return true
            }
        })
        frameImageVIew.setOnTouchListener { view, event ->
            detector.onTouchEvent(event)
            setScrollAction(view, event)
            true
        }

        frameList.forEach {
            val item = layoutInflater.inflate(R.layout.view_frame, null)
            val frameThumbnail = item.findViewById<ImageView>(R.id.frameImageVIew)
            frameThumbnail.setImageResource(it)
            frameThumbnail.setOnClickListener {
                frameImageVIew.setImageDrawable(frameThumbnail.drawable)
            }
            frameSelect.addView(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_save -> savePhoto()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePhoto() {
        val newBitmap = Bitmap.createBitmap(photoImageView.width, photoImageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)

        val imageBitmap = (photoImageView.drawable as BitmapDrawable).bitmap
        val imageSize = computeBitmapSizeFromDynamicImageLayer(photoImageView)
        val scaledImageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize.x, imageSize.y, true)
        canvas.drawBitmap(
                scaledImageBitmap,
                computeCenter(newBitmap.width, imageBitmap.width),
                computeCenter(newBitmap.height, imageBitmap.height),
                null
        )

        val frameBitmap = (frameImageVIew.drawable as BitmapDrawable).bitmap
        val frameSize = computeBitmapSizeFromDynamicImageLayer(frameImageVIew)
        val scaledFrameBitmap = Bitmap.createScaledBitmap(frameBitmap, frameSize.x, frameSize.y, true)
        canvas.drawBitmap(scaledFrameBitmap, frameImageVIew.left.toFloat(), frameImageVIew.top.toFloat(), null)

        val saveDir = SAVE_DIR
        val file = File(Environment.getExternalStorageDirectory().path + saveDir)
        if (!file.exists()) {
            file.mkdir()
        }

        val fileNameDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN)
        val fileName = fileNameDate.format(Date()) + ".jpg"
        val attachName = file.absolutePath + "/" + fileName

        val out = FileOutputStream(attachName)
        newBitmap.compress(CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()

        // save index
        val values = ContentValues()
        val contentResolver = contentResolver
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put("_data", attachName)
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show()
    }

    private fun setScrollAction(view: View, event: MotionEvent) {
        val newDx = event.rawX.toInt()
        val newDy = event.rawY.toInt()

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                view.performClick()
                val dx = view.left + (newDx - preDx)
                val dy = view.top + (newDy - preDy)
                val imgW = dx + view.width
                val imgH = dy + view.height

                view.layout(dx, dy, imgW, imgH)
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
            }
            else -> {
            }
        }

        preDx = newDx
        preDy = newDy
    }
}
