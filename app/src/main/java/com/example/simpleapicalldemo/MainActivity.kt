package com.example.simpleapicalldemo

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
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

        CallAPILoginAsyncTask("isa","12345").execute()
    }

    private inner class CallAPILoginAsyncTask(val username: String, val password: String):AsyncTask<Any, Void, String>(){
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

                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type","application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.useCaches = false // cache removed

                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                jsonRequest.put("username", username)
                jsonRequest.put("password", password)

                writeDataOutputStream.writeBytes(jsonRequest.toString())
                Log.i("username", username)
                writeDataOutputStream.flush()
                writeDataOutputStream.close()

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

            val jsonObj = JSONObject(result)
            val message = jsonObj.optString("message")
            Log.i("Message", message)
            val userId = jsonObj.optInt("user_id")
            val name = jsonObj.optString("name")
            val e_mail = jsonObj.optString("e-mail")
            val mobile = jsonObj.optInt("mobile")

            // json obj -> profile details
            val profile_details_obj = jsonObj.optJSONObject("profile-details")
            val isProfileCompleted = profile_details_obj.optBoolean("is-profile-details")
            val rating = profile_details_obj.optString("rating")

            // list
            val dataListArr = jsonObj.optJSONArray("data-list")
            Log.i("Data length","${dataListArr.length()}")

            for(item in 0 until dataListArr.length()){
                Log.i("Value: $item", "${dataListArr[item]}")

                // list has an multiple obj
                val dataItemObj: JSONObject = dataListArr[item] as JSONObject
                val id = dataItemObj.optInt("id")
                Log.i("ID","$id")
                val value = dataItemObj.optString("value")
                Log.i("Value", value)
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