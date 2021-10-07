package com.example.weatherapp

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    lateinit var input :EditText
    private var zipCode = "10001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAPI()
    }
    //Fetch data from url
    fun fetchWeatherData(): String{
     var response = ""
        try {
         response = URL("https://api.openweathermap.org/data/2.5/weather?zip=$zipCode&units = metric&appid=13ec31280d48d9fd29411986595a44e1").readText(Charsets.UTF_8)
        }catch (e: Exception){
            Log.d("Response","The ISSUE is --> $e")
        }
        return response
    }
    //parsing json data
    suspend fun parsingWeatherData(data:String) {
        withContext(Main) {
            //jsonObjects
            val json = JSONObject(data)
            val main = json.getJSONObject("main")
            val wind = json.getJSONObject("wind").getString("speed")
            val sys = json.getJSONObject("sys")
            val weather = json.getJSONArray("weather").getJSONObject(0)
            //jsonObjects values
            val city = json.getString("name")
            val humidity = main.getString("humidity")
            val pressure = main.getString("pressure")
            val tempDegree = main.getDouble("temp")
            val minTemperature = main.getString("temp_min")
            val tempLow = "Low: " + minTemperature.substring(0, minTemperature.indexOf("."))+"°C"
            val maxTemperature = main.getString("temp_max")
            val tempHigh = "High: " + maxTemperature.substring(0, maxTemperature.indexOf("."))+"°C"
            val skyDescription = weather.getString("description")
            val sunrise = sys.getLong("sunrise")
            val sunset = sys.getLong("sunset")
            val country = sys.getString("country")
            val dateTime: Long = json.getLong("dt")
            val lastUpdateText = "Updated at: " + SimpleDateFormat(
                "dd/MM/yyyy hh:mm a",
                Locale.ENGLISH
            ).format(Date(dateTime * 1000))

            // set values to the text view
            findViewById<TextView>(R.id.tvCity).text = "$country , $city"
        findViewById<TextView>(R.id.tvCity).setOnClickListener {
             showAlertDialog()
        }
            findViewById<TextView>(R.id.tvDateTime).text = lastUpdateText
            findViewById<TextView>(R.id.textView3).text =
                skyDescription.capitalize(Locale.getDefault())
            findViewById<TextView>(R.id.tvTemp).text = tempDegree.toInt().toString() + "Cْ"
            findViewById<TextView>(R.id.tvLowTemp).text = tempLow
            findViewById<TextView>(R.id.tvHighTemp).text = tempHigh
            findViewById<TextView>(R.id.tvSunrise).text = SimpleDateFormat(
                "hh:mm a",
                Locale.ENGLISH
            ).format(Date(sunrise * 1000))
            findViewById<TextView>(R.id.tvSunset).text = SimpleDateFormat(
                "hh:mm a",
                Locale.ENGLISH
            ).format(Date(sunset * 1000))
            findViewById<TextView>(R.id.tvWind).text = wind
            findViewById<TextView>(R.id.tvPressure).text = pressure
            findViewById<TextView>(R.id.tvHumidity).text = humidity
            findViewById<LinearLayout>(R.id.llRefresh).setOnClickListener { requestAPI() }

        }
    }
   //request data from API
    fun requestAPI() {
       CoroutineScope(IO).launch {
         //  updateStatus(-1)
        val data = async { fetchWeatherData() }.await()
           if(data.isNotEmpty()){
                   parsingWeatherData(data)
              // updateStatus(0)
               }else{
               //updateStatus(1)
                   Log.d("Request","Unable to get data")
               }
           }
    }
    // enter anther Zip code
    private fun showAlertDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Enter Zip Code :")
            .setCancelable(false)
        val layout = LinearLayout(this)
        val inputTask = EditText(this)
        inputTask.hint="enter the zip code here..."
        layout.addView(inputTask)
        dialogBuilder.setPositiveButton("Go", DialogInterface.OnClickListener {
                    dialog, id -> zipCode = inputTask.text.toString()
                                          requestAPI()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle("Weather App")
        alert.setView(layout)
        alert.show()
    }
    fun celsiusToFahrenheit(c : Int):String{
        return(( c * 9/5.0)+32).roundToInt().toString()
    }
    fun fahrenheitToCelsius(f:Int):String{
        return(( f - 32 ) * 5/9.0).roundToInt().toString()
    }
}