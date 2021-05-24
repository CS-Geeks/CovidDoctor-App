package az.omar.coviddoctor.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import az.omar.coviddoctor.R
import az.omar.coviddoctor.utils.Utils.mRequestCodeForTakingImage
import az.omar.coviddoctor.utils.Utils.mTempFileExtra
import az.omar.coviddoctor.utils.Utils.mRequestCodeForSelectImageFormGallery
import az.omar.coviddoctor.utils.Utils.mRequestCodeForPermission1
import az.omar.coviddoctor.utils.Utils.mRequestCodeForPermission2
import az.omar.coviddoctor.utils.Utils.mTempFile
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        btn_take_photo.setOnClickListener { takePhoto() }
        btn_select_photo.setOnClickListener {
            selectPhoto()
        }
    }

    private fun takePhoto() {
        checkPermissionForCamera()
    }

    private fun selectPhoto() {
        checkPermissionForExternalStorage()
    }


    private fun checkPermissionForExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionCoarse = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(
                    permission,
                    mRequestCodeForPermission1
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(
                    permissionCoarse,
                    mRequestCodeForPermission2
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun checkPermissionForCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)) {
                val permission = arrayOf(Manifest.permission.CAMERA)
                requestPermissions(
                    permission,
                    mRequestCodeForPermission1
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
            } else {
                takePhotoFormCamera()
            }
        }
    }

    private fun takePhotoFormCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, mRequestCodeForTakingImage)
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            mRequestCodeForSelectImageFormGallery
        ) // GIVE AN INTEGER VALUE FOR IMAGE_PICK_CODE LIKE 1000
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var photo: Bitmap? = null
        if (resultCode == Activity.RESULT_OK && requestCode == mRequestCodeForSelectImageFormGallery && data != null) {
            photo = MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                Uri.parse(data.dataString)
            )
        } else if (resultCode == Activity.RESULT_OK && requestCode == mRequestCodeForTakingImage && data != null) {
            photo = data.extras?.get("data") as Bitmap
        }

        if (photo != null) {
            val tempFileName = createImageFromBitmap(photo)
            val resultIntent = Intent(this@MainActivity, ResultActivity::class.java)
            resultIntent.putExtra(mTempFileExtra, tempFileName)
            startActivity(resultIntent)
        }
    }

    private fun createImageFromBitmap(bitmap: Bitmap): String? {
        var fileName: String? = mTempFile //no .png or .jpg needed
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val fo: FileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            fo.write(bytes.toByteArray())
            // remember close file output
            fo.close()
        } catch (e: Exception) {
            e.printStackTrace()
            fileName = null
        }
        return fileName
    }

}