package com.example.simpleapicalldemo

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CallAPILoginAsyncTask().execute()
    }

    private inner class CallAPILoginAsyncTask():AsyncTask<Any, Void, String>(){
        private lateinit var customProgressDialog: Dialog

        override fun onPreExecute(){
            super.onPreExecute()

            showProgressDialog()
        }
        override fun doInBackground(vararg p0: Any?): String {
            var result: String

            var connection: HttpURLConnection? = null

            try {
                val url = URL("https://run.mocky.io/v3/24bde847-1da8-40b2-9a78-b28f928e7012")
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true // get data
                connection.doOutput = true // send data

                val httpResult: Int = connection.responseCode // status code
                if(httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val stringBuilder = StringBuilder()
                    var line: String?
                    try {
                        while(reader.readLine().also { line = it }!= null){
                            stringBuilder.append(line + "\n")
                        }
                    }catch (e: IOException){
                        e.printStackTrace()
                    }finally {
                        inputStream.close()
                    }
                    result = stringBuilder.toString()
                }else{
                    result = connection.responseMessage
                }
            }catch (e: SocketTimeoutException){
                result = "Connection timeout"
            }catch (e: Exception){
                result = "Error" + e.message
            }finally {
                connection?.disconnect()
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            cancelProgressDialog()
            if (result != null) {
                Log.i("JSON Response Result", result)
            }
        }

        private fun showProgressDialog(){
            customProgressDialog = Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.show()
        }
        private fun cancelProgressDialog(){
            customProgressDialog.dismiss()
        }
    }
}