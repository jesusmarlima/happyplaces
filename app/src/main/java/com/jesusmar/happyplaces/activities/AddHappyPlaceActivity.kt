package com.jesusmar.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.jesusmar.happyplaces.R
import com.jesusmar.happyplaces.database.DatabaseHandler
import com.jesusmar.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var saveImageToInternalStorage: Uri? = null
    private var mLongitude: Double = 0.0
    private var mLAtidute: Double = 0.0


    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        setSupportActionBar(tb_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tb_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateView()
        }

        updateDateView()
        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(this@AddHappyPlaceActivity,
                    dateSetListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH) ).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery",
                "Capture Foto from camera")
                pictureDialog.setItems(pictureDialogItems){
                    _, which ->
                    when (which){
                        0 ->  choosePhotoFromGalery()
                        1 ->  takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save -> {
                saveHappyPlace()
            }
        }
    }

    private fun saveHappyPlace() {
        when {
            et_title.text.isNullOrEmpty() -> {
                Toast.makeText(this@AddHappyPlaceActivity, "Please Enter Title", Toast.LENGTH_SHORT)
                    .show()
            }
            et_location.text.isNullOrEmpty() -> {
                Toast.makeText(
                    this@AddHappyPlaceActivity,
                    "Please Enter Location",
                    Toast.LENGTH_SHORT
                ).show()
            }
            et_description.text.isNullOrEmpty() -> {
                Toast.makeText(
                    this@AddHappyPlaceActivity,
                    "Please Enter Description",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val happyPlaceModel = HappyPlaceModel(
                    0, et_title.text.toString(),
                    saveImageToInternalStorage.toString(),
                    et_description.text.toString(),
                    et_date.text.toString(),
                    et_location.text.toString(),
                    mLAtidute,
                    mLongitude)

                val dbHandler = DatabaseHandler(this)
                val addHappyPlaceResult: Long = dbHandler.addHappyPlace(happyPlaceModel)
                if (addHappyPlaceResult > 0) {
                    Toast.makeText(this, "the happy place was saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    setImageFromGallery(data)
                }
                CAMERA_REQUEST_CODE -> {
                    setImageFromCamera(data)
                }
            }
        }
    }

    private fun setImageFromCamera(data: Intent?) {
        if (data != null) {
            try {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                iv_place_image.setImageBitmap(thumbnail)
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@AddHappyPlaceActivity,
                    "FAILS LOADING IMAGE", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setImageFromGallery(data: Intent?) {
        if (data != null) {
            val contentURI = data!!.data
            try {
                val seletedImageBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                iv_place_image.setImageBitmap(seletedImageBitmap)
                saveImageToInternalStorage = saveImageToInternalStorage(seletedImageBitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@AddHappyPlaceActivity,
                    "FAILS LOADING IMAGE", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.CAMERA)
            .withListener( object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()){
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent,
                            CAMERA_REQUEST_CODE
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun choosePhotoFromGalery() {
        Dexter.withActivity(this).withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener( object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report!!.areAllPermissionsGranted()){
                        val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent,
                            GALLERY_REQUEST_CODE
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("it looks you have turned off permission required " +
                    "for this feature. it can be under the applications Settings")
            .setPositiveButton("GO TO SETTINGS"){
            _, _ -> try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
        }.setNegativeButton("Cancel"){
                dialog, _ -> dialog.dismiss()
            }.show()
    }

    private fun updateDateView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitMap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_FOLDER, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg" )

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val IMAGE_FOLDER = "HappuPlacesImages"
    }

}
