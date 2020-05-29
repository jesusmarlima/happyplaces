package com.jesusmar.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.happyplaces.adapters.HappyPlacesAdapter
import com.happyplaces.utils.SwipeToDeleteCallback
import com.jesusmar.happyplaces.R
import com.jesusmar.happyplaces.database.AppDatabase
import com.jesusmar.happyplaces.models.HappyPlaceModel
import com.jesusmar.happyplaces.util.DataTask
import kotlinx.android.synthetic.main.activity_main.*
import pl.kitek.rvswipetodelete.SwipeToEditCallback
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    companion object{
        val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        val EXTRA_PLACE_DETAILS = "extra_place_details"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAddHappyPlace.setOnClickListener{
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getHappyPlaceFormLocalDatabase()

    }

    private fun getHappyPlaceFormLocalDatabase() {

        val dataTask = DataTask()

        dataTask.setDataListener(object: DataTask.DataTaskListener {
            override fun execute(handler: AppDatabase): Any? {
                return handler.happyPlacesModelDao().getAll()
            }

            override fun onSuccess(result: Any?) {
                rv_happy_place_list.visibility = View.VISIBLE
                tv_no_records_available.visibility = View.GONE
                if (result is List<*>) {
                    setupHappyPlaceRecycleView(result.filterIsInstance<HappyPlaceModel>())
                }
            }

            override fun onFail() {
                rv_happy_place_list.visibility = View.GONE
                tv_no_records_available.visibility = View.VISIBLE
            }

        })

        dataTask.execute(this)
    }


    private fun setupHappyPlaceRecycleView(happyPlaceList: List<HappyPlaceModel>){
        rv_happy_place_list.layoutManager = LinearLayoutManager(this)
        rv_happy_place_list.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList as ArrayList<HappyPlaceModel> )
        rv_happy_place_list.adapter = placesAdapter

        placesAdapter.setOnClickListener(object :
            HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailsActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent
                )
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_place_list.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happy_place_list)


        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // TODO (Step 6: Call the adapter function when it is swiped for delete)
                // START
                val adapter = rv_happy_place_list.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getHappyPlaceFormLocalDatabase()
                // END
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happy_place_list)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
                getHappyPlaceFormLocalDatabase()
            }
        }
    }
}
