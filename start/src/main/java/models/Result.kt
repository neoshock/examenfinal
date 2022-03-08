package models

import java.io.Serializable

class Result(val Name: String, val SeqID: Int, val GeoPt: Array<Double>, val TelPref: String, val CountryInfo: String,
             val Capital: Capital, val GeoRectangle: GeoRectangle, val CountryCodes: CountryCodes):Serializable {
}