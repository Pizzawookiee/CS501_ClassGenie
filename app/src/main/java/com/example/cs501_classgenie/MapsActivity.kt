package com.example.cs501_classgenie

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cs501_classgenie.databinding.ActivityMapsRouteBinding
import com.example.kotlindemos.PermissionUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsRouteBinding

    private var permissionDenied = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // https://github.com/googlemaps-samples/android-samples/tree/main/ApiDemos/kotlin
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        val policy = ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)

        val bodyJson = """
          {
              "origin":{
                "address": "900 commonwealth ave"
              },
              "destination":{
                "address": "500 commonwealth ave"
              },
              "travelMode": "DRIVE",
              "polylineEncoding": "GEO_JSON_LINESTRING",
              "computeAlternativeRoutes": false,
              "routeModifiers": {
                "avoidTolls": false,
                "avoidHighways": false,
                "avoidFerries": false
              },
              "languageCode": "en-US",
              "units": "IMPERIAL"
            }
        """
        val body: RequestBody = bodyJson.toRequestBody("application/json".toMediaType())


        val httpRequest: Request = Request.Builder()
            .url("https://routes.googleapis.com/directions/v2:computeRoutes")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key","")
            .addHeader("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.polyline")
            .build()

        val client = OkHttpClient()
        val call: Call = client.newCall(httpRequest)
        val httpResponse: Response = call.execute()
        val jsonObject: JSONObject = JSONObject(httpResponse.body?.string() ?:"")
        """
            {
              "routes": [
                {
                  "distanceMeters": 1727,
                  "duration": "333s",
                  "polyline": {
                    "geoJsonLinestring": {
                      "coordinates": [
                        [
                          -71.1159892,
                          42.3510554
                        ],
                        [
                          -71.1067165,
                          42.3499204
                        ],
                        [
                          -71.102688399999991,
                          42.3494426
                        ],
                        [
                          -71.1022706,
                          42.349396200000008
                        ],
                        [
                          -71.1021779,
                          42.3493725
                        ],
                        [
                          -71.0979756,
                          42.3488836
                        ],
                        [
                          -71.0976709,
                          42.3488496
                        ],
                        [
                          -71.0972482,
                          42.348767599999995
                        ],
                        [
                          -71.095229,
                          42.3487976
                        ]
                      ],
                      "type": "LineString"
                    }
                  }
                }
              ]
            }
        """.trimIndent()

        val coors = jsonObject.getJSONArray("routes")
            .getJSONObject(0)
            .getJSONObject("polyline")
            .getJSONObject("geoJsonLinestring")
            .getJSONArray("coordinates")
        val LatLngList = mutableListOf<LatLng>()
        for (pair in 0..coors.length()-1){
            LatLngList.add(LatLng(
                coors.getJSONArray(pair)[1].toString().toDouble(),
                coors.getJSONArray(pair)[0].toString().toDouble()
            ))
        }

        Log.d("response",coors.toString())

        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.
        // [START maps_poly_activity_add_polyline_set_tag]
        // [START maps_poly_activity_add_polyline]
        val polyline1 = googleMap.addPolyline(
            PolylineOptions()
            .clickable(true)
            .add(
                *LatLngList.toTypedArray()
//                        LatLng(-29.501, 119.700),
//            LatLng(-27.456, 119.672),
//            LatLng(-25.971, 124.187),
//            LatLng(-28.081, 126.555),
//            LatLng(-28.848, 124.229),
//            LatLng(-28.215, 123.938)
            ))
        // [END maps_poly_activity_add_polyline]
        // [START_EXCLUDE silent]
        // Store a data object with the polyline, used here to indicate an arbitrary type.
        polyline1.tag = "B"
        // [END maps_poly_activity_add_polyline_set_tag]
        // Style the polyline.
        stylePolyline(polyline1)

        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(42.0, -71.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()
    }

    // [START maps_poly_activity_style_polyline]
    private val COLOR_BLACK_ARGB = -0x1000000
    private val POLYLINE_STROKE_WIDTH_PX = 12

    /**
     * Styles the polyline, based on type.
     * @param polyline The polyline object that needs styling.
     */
    private fun stylePolyline(polyline: Polyline) {
        // Get the data object stored with the polyline.
        val type = polyline.tag?.toString() ?: ""
        when (type) {
            "A" -> {
                // Use a custom bitmap as the cap at the start of the line.
                polyline.startCap = CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10f)
            }
            "B" -> {
                // Use a round cap at the start of the line.
                polyline.startCap = RoundCap()
            }
        }
        polyline.endCap = CustomCap(
            BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10f)
        polyline.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
        polyline.color = COLOR_BLACK_ARGB
        polyline.jointType = JointType.ROUND
    }
    // [END maps_poly_activity_style_polyline]

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun enableMyLocation() {
        if (!::mMap.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}