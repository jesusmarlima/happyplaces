package com.jesusmar.happyplaces

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jesusmar.happyplaces.database.AppDatabase
import com.jesusmar.happyplaces.models.HappyPlaceModel
import com.jesusmar.happyplaces.models.HappyPlaceModelDao
import org.hamcrest.CoreMatchers
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DataBaseLayerTest {

    private lateinit var happyPlaceModelDao: HappyPlaceModelDao
    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        happyPlaceModelDao = db.happyPlacesModelDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.clearAllTables()
        db.close()
    }

    @Test
    fun writeHappyPlaceReadInList() {
        val happyPlace = HappyPlaceModel(1,"23123","3231","wqe23","342342","dfsdfsdf","123123","e234342")
        happyPlaceModelDao.insertAll(happyPlace)
        val byName = happyPlaceModelDao.findByName ("23123","wqe23")
        assertThat(byName, CoreMatchers.equalTo(happyPlace))
    }


    @Test
    fun deleteHappyPlaceTest() {
        val happyPlace = HappyPlaceModel(1,"23123","3231","wqe23","342342","dfsdfsdf","123123","e234342")
        happyPlaceModelDao.insertAll(happyPlace)
        val byName = happyPlaceModelDao.findByName ("23123","wqe23")
        happyPlaceModelDao.delete(byName)
        assertThat(happyPlaceModelDao.getAll().size,CoreMatchers.equalTo(0))
    }

    @Test
    fun updateHappyPlaceTest() {
        val happyPlace = HappyPlaceModel(1,"23123","3231","wqe23","342342","dfsdfsdf","123123","e234342")
        happyPlaceModelDao.insertAll(happyPlace)

        val byName = happyPlaceModelDao.findByName ("23123","wqe23").copy(title = "hahaha")

        happyPlaceModelDao.updateHappyPlace(byName)
        val updated = happyPlaceModelDao.findByName ("hahaha","wqe23")
        val old = happyPlaceModelDao.findByName ("23123","wqe23")

        assertThat(updated,CoreMatchers.equalTo(byName))
        assertThat(old, CoreMatchers.nullValue())
    }

    @Test
    fun getAllHappyPlacesTest() {
        val happyPlace1 = HappyPlaceModel(1,"23123","3231","wqe23","342342","dfsdfsdf","123123","e234342")
        val happyPlace2 = HappyPlaceModel(2,"23123","3231","wqe23","342342","dfsdfsdf","123123","e234342")
        val happyPlace3 = HappyPlaceModel(3,"23123","3231","wqe23","342342","dfsdfsdf","123123","e234342")
        val happyPlace4 = HappyPlaceModel(4,"23123","3231","wqe23","342342","dfsdfsdf","123123","e234342")

        happyPlaceModelDao.insertAll(happyPlace1,happyPlace2,happyPlace3,happyPlace4)

        val all = happyPlaceModelDao.getAll()

        assertThat(all,CoreMatchers.hasItems(happyPlace1,happyPlace2,happyPlace3,happyPlace4))

    }
}
