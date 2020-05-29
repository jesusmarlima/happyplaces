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

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE HappyPlacesTable ("
                        + "_ID INTEGER PRIMARY KEY NOT NULL,"
                        + "TITLE TEXT NOT NULL,"
                        + "IMAGE TEXT,"
                        + "DESCRIPTION TEXT,"
                        + "DATE TEXT,"
                        + "LOCATION TEXT,"
                        + "LATITUDE TEXT,"
                        + "LONGITUDE TEXT);")

                database.execSQL("DROP TABLE IF EXISTS HappyPlacesTable;")
                database.execSQL(CREATE_HAPPY_PLACE_TABLE)

            }
        }

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE HappyPlacesTable ("
                        + "_id INTEGER PRIMARY KEY NOT NULL,"
                        + "title TEXT,"
                        + "image TEXT,"
                        + "description TEXT,"
                        + "date TEXT,"
                        + "location TEXT,"
                        + "latitude TEXT,"
                        + "longitude TEXT);")

                database.execSQL("DROP TABLE IF EXISTS HappyPlacesTable;")
                database.execSQL(CREATE_HAPPY_PLACE_TABLE)

            }
        }

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val ADD_NEW_COLUMN = ("ALTER TABLE HappyPlacesTable ADD COLUMN size TEXT")
                database.execSQL(ADD_NEW_COLUMN)

            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "HappyPlacesDatabase")
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
    }
}