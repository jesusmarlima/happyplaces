package  com.jesusmar.happyplaces.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseHandler {

    companion object {

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE HappyPlaceTable ("
                        + "_ID INTEGER PRIMARY KEY NOT NULL,"
                        + "TITLE TEXT,"
                        + "IMAGE TEXT,"
                        + "DESCRIPTION TEXT,"
                        + "DATE TEXT,"
                        + "LOCATION TEXT,"
                        + "LATITUDE TEXT,"
                        + "LONGITUDE TEXT);")

                database.execSQL("DROP TABLE HappyPlaceTable;")
                database.execSQL(CREATE_HAPPY_PLACE_TABLE)

            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "HappyPlacesDatabase")
                .addMigrations(MIGRATION_2_3)
                .build()
    }
}

//    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
//
//    companion object {
//        private const val DATABASE_VERSION = 1 // Database version
//        private const val DATABASE_NAME = "HappyPlacesDatabase" // Database name
//        private const val TABLE_HAPPY_PLACE = "HappyPlacesTable" // Table Name
//
//        //All the Columns names
//        private const val KEY_ID = "_id"
//        private const val KEY_TITLE = "title"
//        private const val KEY_IMAGE = "image"
//        private const val KEY_DESCRIPTION = "description"
//        private const val KEY_DATE = "date"
//        private const val KEY_LOCATION = "location"
//        private const val KEY_LATITUDE = "latitude"
//        private const val KEY_LONGITUDE = "longitude"
//    }
//
//    override fun onCreate(db: SQLiteDatabase?) {
//        val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE " + TABLE_HAPPY_PLACE + "("
//                + KEY_ID + " INTEGER PRIMARY KEY,"
//                + KEY_TITLE + " TEXT,"
//                + KEY_IMAGE + " TEXT,"
//                + KEY_DESCRIPTION + " TEXT,"
//                + KEY_DATE + " TEXT,"
//                + KEY_LOCATION + " TEXT,"
//                + KEY_LATITUDE + " TEXT,"
//                + KEY_LONGITUDE + " TEXT)")
//        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE")
//        onCreate(db)
//    }
//
//    fun addHappyPlace(happyPlace: HappyPlaceModel): Long {
//        val db = this.writableDatabase
//
//        val contentValues = ContentValues()
//        contentValues.put(KEY_TITLE, happyPlace.title)
//        contentValues.put(KEY_IMAGE, happyPlace.image)
//        contentValues.put(KEY_DESCRIPTION, happyPlace.description)
//        contentValues.put(KEY_DATE, happyPlace.date)
//        contentValues.put(KEY_LOCATION, happyPlace.location)
//        contentValues.put(KEY_LATITUDE, happyPlace.latitude)
//        contentValues.put(KEY_LONGITUDE, happyPlace.longitude)
//
//        val result = db.insert(TABLE_HAPPY_PLACE, null, contentValues)
//
//        db.close()
//        return result
//    }
//
//    fun getHappyPlaceList(): ArrayList<HappyPlaceModel>{
//        val happyPlaceList = ArrayList<HappyPlaceModel>()
//        val selectQuery = " SELECT * FROM ${TABLE_HAPPY_PLACE}"
//        val db = this.readableDatabase
//
//        try {
//            val cursor: Cursor = db.rawQuery(selectQuery, null)
//            if (cursor.moveToFirst()){
//                do{
//                    var place = HappyPlaceModel(
//                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
//                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
//                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
//                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
//                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
//                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
//                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
//                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)))
//                    happyPlaceList.add(place)
//                } while(cursor.moveToNext())
//            }
//            cursor.close()
//        } catch (e: SQLException){
//            db.execSQL(selectQuery)
//            return ArrayList()
//        }
//        return happyPlaceList
//    }
//
//    fun deleteHaapyPlace(happyPlace: HappyPlaceModel): Int? {
//        val db = this.writableDatabase
//        var success: Int? = null
//        success = db.delete(TABLE_HAPPY_PLACE,"${KEY_ID}=${happyPlace.id}", null)
//        db.close()
//        return success
//    }
//
//    fun updateHappyPlace(happyPlace: HappyPlaceModel): Int {
//        val db = this.writableDatabase
//
//        val contentValues = ContentValues()
//        contentValues.put(KEY_TITLE, happyPlace.title)
//        contentValues.put(KEY_IMAGE, happyPlace.image)
//        contentValues.put(KEY_DESCRIPTION, happyPlace.description)
//        contentValues.put(KEY_DATE, happyPlace.date)
//        contentValues.put(KEY_LOCATION, happyPlace.location)
//        contentValues.put(KEY_LATITUDE, happyPlace.latitude)
//        contentValues.put(KEY_LONGITUDE, happyPlace.longitude)
//
//        val result = db.update(TABLE_HAPPY_PLACE, contentValues,"${KEY_ID}=${happyPlace.id}", null)
//
//        db.close()
//        return result
//    }
//}
