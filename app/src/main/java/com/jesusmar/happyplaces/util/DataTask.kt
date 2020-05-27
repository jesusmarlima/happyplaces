package com.jesusmar.happyplaces.util

import android.content.Context
import android.os.AsyncTask
import com.jesusmar.happyplaces.database.AppDatabase
import com.jesusmar.happyplaces.database.DatabaseHandler

open class DataTask: AsyncTask<Any, Void, Any?>() {

    lateinit var dtListener: DataTaskListener

    override fun doInBackground(vararg params: Any?): Any? {
        val handler = DatabaseHandler.getInstance(params[0] as Context)
        return dtListener.execute(handler)
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (result != null) {
            dtListener.onSuccess(result)
        }
    }

    fun setDataListener(listener: DataTaskListener ) {
        dtListener = listener
    }

    interface DataTaskListener {
        fun execute(handler: AppDatabase): Any?
        fun onSuccess(result: Any?)
        fun onFail()
    }
}
