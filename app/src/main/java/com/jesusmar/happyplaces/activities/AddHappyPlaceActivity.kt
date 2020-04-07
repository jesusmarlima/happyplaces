package com.jesusmar.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.jesusmar.happyplaces.R
import com.jesusmar.happyplaces.database.DatabaseHandler
import com.jesusmar.happyplaces.models.HappyPlaceModel
import com.jesusmar.happyplaces.util.GetAddresFromLatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import kotlinx.android.synthetic.main.activity_add_happy_place.iv_place_image
import kotlinx.android.synthetic.main.activity_happy_place_details.*
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
    private var mLatidute: Double = 0.0
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private var mHappyplace: HappyPlaceModel? = null


    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyplace =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        setSupportActionBar(tb_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tb_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddHappyPlaceActivity,
                resources.getString(R.string.google_maps_key)
            )
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateView()
        }

        updateDateView()
        et_date.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        tv_select_current_location.setOnClickListener(this)
    }

    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            mLatidute = locationResult!!.lastLocation.latitude
            mLongitude = locationResult!!.lastLocation.longitude
            var addressTask = GetAddresFromLatLng(this@AddHappyPlaceActivity, mLatidute, mLongitude)
            addressTask.setAddressListener(object : GetAddresFromLatLng.AddressListener {
                override fun onAddressFound(address: String) {
                    et_location.setText(address)
                }

                override fun onError() {
                    Toast.makeText(
                        this@AddHappyPlaceActivity,
                        "Error retriveing location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            addressTask.getAdrress()

        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select photo from Gallery",
                    "Capture Foto from camera"
                )
                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGalery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save -> {
                saveHappyPlace()
            }
            R.id.et_location -> {
                try {
                    val fields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )

                    val intent = Autocomplete
                        .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(this, "Plaase setup location parmissions", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {
                                    requestNewLocationData()
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

            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

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
                val dbHandler = DatabaseHandler(this)

                if (mHappyplace != null) {

                    val happyPlaceModel = HappyPlaceModel(
                        mHappyplace!!.id,
                        et_title.text.toString(),
                        saveImageToInternalStorage.toString(),
                        et_description.text.toString(),
                        et_date.text.toString(),
                        et_location.text.toString(),
                        mLatidute,
                        mLongitude
                    )

                    val addHappyPlaceResult: Int = dbHandler.updateHappyPlace(happyPlaceModel)
                    if (addHappyPlaceResult > 0) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                } else {
                    val happyPlaceModel = HappyPlaceModel(
                        0,
                        et_title.text.toString(),
                        saveImageToInternalStorage.toString(),
                        et_description.text.toString(),
                        et_date.text.toString(),
                        et_location.text.toString(),
                        mLatidute,
                        mLongitude
                    )

                    val addHappyPlaceResult: Long = dbHandler.addHappyPlace(happyPlaceModel)
                    if (addHappyPlaceResult > 0) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    setImageFromGallery(data)
                }
                CAMERA_REQUEST_CODE -> {
                    setImageFromCamera(data)
                }
                PLACE_AUTOCOMPLETE_REQUEST_CODE -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    et_location.setText(place.address)
                    mLatidute = place.latLng!!.latitude
                    mLongitude = place.latLng!!.longitude
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
                Toast.makeText(
                    this@AddHappyPlaceActivity,
                    "FAILS LOADING IMAGE", Toast.LENGTH_SHORT
                )
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
                Toast.makeText(
                    this@AddHappyPlaceActivity,
                    "FAILS LOADING IMAGE", Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.CAMERA
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(
                            galleryIntent,
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
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(
                            galleryIntent,
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

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage(
                "it looks you have turned off permission required " +
                        "for this feature. it can be under the applications Settings"
            )
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateView() {
        val myFormat = "dd.MM.yyyy"

        if (mHappyplace != null) {
            et_title.setText(mHappyplace!!.title)
            et_description.setText(mHappyplace!!.description)
            et_date.setText(mHappyplace!!.date)
            et_location.setText(mHappyplace!!.location)

            saveImageToInternalStorage = Uri.parse(mHappyplace!!.image)

            iv_place_image.setImageURI(saveImageToInternalStorage)

            mLatidute = mHappyplace!!.latitude
            mLongitude = mHappyplace!!.longitude
        }

        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitMap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_FOLDER, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
        private const val IMAGE_FOLDER = "HappuPlacesImages"
    }

}
