package org.tensorflow.lite.examples.classification

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.squareup.picasso.Picasso
import models.Country
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import services.CountryService

class CountryResult : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_result)

        val mapFragment :SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val btnRegresar = findViewById<Button>(R.id.btnBack)
        btnRegresar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                backActivity(view)
            }
        })
    }

    fun backActivity(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun getDataFromService(countryCode: String): Call<Country>{
        val retrofit = Retrofit.Builder()
            .baseUrl("http://www.geognos.com/api/en/countries/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val countryService = retrofit.create(CountryService::class.java)
        val callCountryService: Call<Country> = countryService.getCountry(countryCode)
        return  callCountryService
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0;

        intent.getStringExtra("code")?.let {
            getDataFromService(it).enqueue(object : Callback<Country>{
                override fun onFailure(call: Call<Country>, t: Throwable) {
                    println("###Error###" + t.message)
                    Toast.makeText(applicationContext, "ERROR: " + t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<Country>, response: Response<Country>) {
                    val countryResult: Country = response.body()!!
                    addDataFromResponse(countryResult)

                    val defaultLat : LatLng = LatLng(countryResult.Results.GeoPt[0],countryResult.Results.GeoPt[1])

                    val north = countryResult.Results.GeoRectangle.North;
                    val west = countryResult.Results.GeoRectangle.West;
                    val east = countryResult.Results.GeoRectangle.East;
                    val south = countryResult.Results.GeoRectangle.South;

                    val rectagleOption : PolygonOptions = PolygonOptions()
                        .add(LatLng(north, west), //North + West
                            LatLng(south, west), //South + West
                            LatLng(south, east), //South + East
                            LatLng(north, east),  //North + East
                            LatLng(north, west))  //North + West
                        .strokeColor(Color.BLUE)

                    googleMap.addPolygon(rectagleOption)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLat, 4.0f))
                }
            })
        }
    }

    fun addDataFromResponse(countryResponse: Country){
        val pais = findViewById<TextView>(R.id.countryName)
        val capital = findViewById<TextView>(R.id.countryCapital)
        val codeIso2 = findViewById<TextView>(R.id.codeIso)
        val telPrefix = findViewById<TextView>(R.id.telPrefix)
        val rectangle = findViewById<TextView>(R.id.rectangles)
        val center = findViewById<TextView>(R.id.cordCenter)
        val img = findViewById<ImageView>(R.id.banderaImg)

        pais.setText(countryResponse.Results.Name)
        capital.setText(countryResponse.Results.Capital.Name)
        codeIso2.setText(countryResponse.Results.CountryCodes.iso2)
        telPrefix.setText(countryResponse.Results.TelPref)
        rectangle.setText(countryResponse.Results.GeoRectangle.West.toString() + " "
                + countryResponse.Results.GeoRectangle.North.toString() + " +"
                + countryResponse.Results.GeoRectangle.East.toString() + " "
                + countryResponse.Results.GeoRectangle.South.toString())
        center.setText(countryResponse.Results.GeoPt[0].toString() + " "
                + countryResponse.Results.GeoPt[1].toString())

        Picasso.get()
            .load("http://www.geognos.com/api/en/countries/flag/" + codeIso2.text.toString() + ".png")
            .into(img)
    }
}