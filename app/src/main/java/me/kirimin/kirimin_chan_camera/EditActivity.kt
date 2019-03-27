package me.kirimin.kirimin_chan_camera

import android.content.ContentValues
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Environment
import android.view.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_edit.*
import android.content.Intent
import androidx.core.content.FileProvider

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
            R.drawable.frame12,
            R.drawable.frame13,
            R.drawable.frame14,
            R.drawable.frame15,
            R.drawable.frame16,
            R.drawable.frame_kirino1,
            R.drawable.frame_sd1
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

    private var defaultFrameWidth = 0
    private var defaultFrameHeight = 0
    private var preDx: Int = 0
    private var preDy: Int = 0
    private var scale = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.edit_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        photoImageView.setImageBitmap(intent.getParcelableExtra<Uri>(EXTRA_URI).rotateFixedBitmap(this))

        val detector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scale *= detector.scaleFactor
                val left = frameImageVIew.left
                val top = frameImageVIew.top
                val width = left + defaultFrameWidth * scale
                val height = top + defaultFrameHeight * scale
                frameImageVIew.layout(left, top, width.toInt(), height.toInt())
                return true
            }
        })
        frameImageVIew.setOnTouchListener { view, event ->
            setScrollAction(view, event)
            detector.onTouchEvent(event)
            true
        }

        frameList.forEach { res ->
            val item = layoutInflater.inflate(R.layout.view_frame, null)
            val frameThumbnail = item.findViewById<ImageView>(R.id.frameImageVIew)
            val bitmap = BitmapFactory.decodeResource(resources, res)
            val scaled = Bitmap.createScaledBitmap(bitmap, bitmap.width / 4, bitmap.height / 4, true)
            frameThumbnail.setImageBitmap(scaled)
            frameThumbnail.setOnClickListener {
                frameImageVIew.setImageResource(res)
                frameImageVIew.post {
                    defaultFrameWidth = frameImageVIew.width
                    defaultFrameHeight = frameImageVIew.height
                }
            }
            frameSelect.addView(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_save -> savePhotoAction()
            R.id.action_share -> shareAction()
            R.id.action_reset -> {
                preDx = 0
                preDy = 0
                scale = 1f
                frameImageVIew.layout(0, 0, defaultFrameWidth, defaultFrameHeight)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePhotoAction() {
        saveImpl()
        Toast.makeText(this, "ギャラリーに保存しました", Toast.LENGTH_SHORT).show()
    }

    private fun shareAction() {
        val file = File(saveImpl())
        val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, contentResolver.getType(uri))
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_TEXT, "#きりみんちゃんカメラ")
        startActivity(intent)
    }

    private fun saveImpl(): String {
        val baseBitmap = Bitmap.createBitmap(photoImageView.width, photoImageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBitmap)

        val photoBitmap = (photoImageView.drawable as BitmapDrawable).bitmap
        val photoSize = computeBitmapSizeFromDynamicImageLayer(photoImageView)
        val scaledPhotoBitmap = Bitmap.createScaledBitmap(photoBitmap, photoSize.x, photoSize.y, true)
        canvas.drawBitmap(
            scaledPhotoBitmap,
            computeCenter(baseBitmap.width, scaledPhotoBitmap.width),
            computeCenter(baseBitmap.height, scaledPhotoBitmap.height),
            null
        )

        if (frameImageVIew.drawable != null) {
            val frameBitmap = (frameImageVIew.drawable as BitmapDrawable).bitmap
            val frameSize = computeBitmapSizeFromDynamicImageLayer(frameImageVIew)
            val scaledFrameBitmap = Bitmap.createScaledBitmap(frameBitmap, frameSize.x, frameSize.y, true)
            scaledFrameBitmap.density = photoBitmap.density
            canvas.drawBitmap(scaledFrameBitmap, frameImageVIew.left.toFloat(), frameImageVIew.top.toFloat(), null)
        }

        val paint = Paint()
        paint.textSize = 120.toDp(this)
        paint.color = ContextCompat.getColor(this, R.color.colorPrimary)
        canvas.drawText(getString(R.string.sukashi), 64.toDp(this), 100.toDp(this), paint)

        val saveDir = SAVE_DIR
        val file = File(Environment.getExternalStorageDirectory().path + saveDir)
        if (!file.exists()) {
            file.mkdir()
        }

        val fileNameDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN)
        val fileName = "a" + ".jpg"
        val attachName = file.absolutePath + "/" + fileName

        val out = FileOutputStream(attachName)
        baseBitmap.compress(CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()

        // save index
        val values = ContentValues()
        val contentResolver = contentResolver
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put("_data", attachName)
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return attachName
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
