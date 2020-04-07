package com.jesusmar.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jesusmar.happyplaces.R
import com.jesusmar.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_happy_place_details.*
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_maps.map

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    var happyPlace : HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        happyPlace = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel


        setSupportActionBar(tb_map)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = happyPlace!!.title
        tb_map.setNavigationOnClickListener { onBackPressed()  }


        val supportMapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        supportMapFragment.getMapAsync(this)



    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val pos = LatLng(happyPlace!!.latitude,happyPlace!!.longitude)
        googleMap!!.addMarker(
            MarkerOptions().position(pos).title(happyPlace!!.location))
        val newLateLongZoon = CameraUpdateFactory.newLatLngZoom(pos, 10f)
        googleMap.animateCamera(newLateLongZoon)

    }

}
