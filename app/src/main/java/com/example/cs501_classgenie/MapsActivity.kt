package com.example.cs501_classgenie

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.cs501_classgenie.databinding.ActivityMapsRouteBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.Calendar


class MapsActivity : AppCompatActivity(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsRouteBinding

    val calendarViewModel: CalendarViewModel by viewModels()

    private var routeTravelTimeInSecond = 30L
    private var permissionDenied = false

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    // A default location (725 Commonwealth Ave, Boston) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(42.3503127, -71.1058402)

    private lateinit var pendingIntent: PendingIntent

    private lateinit var nextEvent: CalendarEvent

    var destination = """
        "address": "720 commonwealth ave"
        """.trimIndent()
    val alternativeAddressFormat = """
        "location":{
            "latLng":{
                "latitude": 42.3503127,
                "longitude": -71.1058402
            }
        }
        """.trimIndent()

    private lateinit var alarmManager: AlarmManager

    private var requestNotificationPermission = true



    private lateinit var polyline: Polyline


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission", "ScheduleExactAlarm")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(getString(R.string.channel_id), name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        nextEvent = calendarViewModel.events.value[0]

        getAllPermission()
        getLocationPermission()
        getNotificationPermission()
        getAlarmPermission()

//        registerAlarm()
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        getLocationPermission()
        mMap = googleMap
        // Add a marker in Boston and move the camera
//        val boston = LatLng(42.0, -71.0)
//        mMap.addMarker(MarkerOptions().position(boston).title("Marker in Boston"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(boston))

        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
//        enableMyLocation()
//        getDeviceLocation()

        val currentLocation: LatLng
        if (lastKnownLocation != null) {
            currentLocation = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
        } else {
            currentLocation = defaultLocation
        }
        mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Position"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,DEFAULT_ZOOM.toFloat()))
//        mMap.isMyLocationEnabled = true
        getNotificationPermission()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
//        getLocationPermission()
        getDeviceLocation() // and then draw route and then set alarm
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    // https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial#kotlin
    /**
     * Prompts the user for permission to use the device location.
     */

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
//        enableMyLocation()
//        getLocationPermission()
//        try {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = task.result
                if (lastKnownLocation != null) {
                    mMap?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude
                            ), DEFAULT_ZOOM.toFloat()
                        )
                    )
                }
                Log.d(TAG, "Current location " + lastKnownLocation.toString())
                Log.d(TAG, "task " + task.exception)
                // draw route after getting current location
                drawRoute(mMap)
                getNotificationPermission()
            } else {
                Log.d(TAG, "Current location is null. Using defaults.")
                Log.e(TAG, "Exception: %s", task.exception)
                mMap?.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                )
//                    mMap?.uiSettings?.isMyLocationButtonEnabled = false
            }
        }

//        } catch (e: SecurityException) {
//            Log.e("Exception: %s", e.message, e)
//        }
    }

    // https://github.com/googlemaps-samples/android-samples/tree/main/ApiDemos/kotlin
    /**
     * draw route.
     */
    fun drawRoute(googleMap: GoogleMap) {
        try {
            polyline.remove()
        } catch (e: UninitializedPropertyAccessException) {

        }

        // https://stackoverflow.com/a/9289190
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        nextEvent = calendarViewModel.events.value[0]
        destination = """
        "address": "${nextEvent.location}"
        """.trimIndent()

        val origin = """
            "location":{
                "latLng":{
                    "latitude": ${lastKnownLocation!!.latitude},
                    "longitude": ${lastKnownLocation!!.longitude}
                }
            }
        """.trimIndent()

        val bodyJson = """
          {
              "origin":{
                ${origin}
              },
              "destination":{
                ${destination}
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
            .addHeader("X-Goog-Api-Key", "${BuildConfig.MAPS_API_KEY}")
            .addHeader(
                "X-Goog-FieldMask",
                "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.polyline"
            )
            .build()

        val client = OkHttpClient()
        val call: Call = client.newCall(httpRequest)
        val httpResponse: Response = call.execute()
        val jsonObject: JSONObject = JSONObject(httpResponse.body?.string() ?: "")
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
                        // longitude
                          -71.1159892,
                        // latitude
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

        val routeDuration = jsonObject.getJSONArray("routes")
            .getJSONObject(0)
            .getString("duration")
        routeTravelTimeInSecond = routeDuration.dropLast(1).toLong()
        Log.d("notification","estimated route duration ${routeTravelTimeInSecond}")
        val routeCoordinates = jsonObject.getJSONArray("routes")
            .getJSONObject(0)
            .getJSONObject("polyline")
            .getJSONObject("geoJsonLinestring")
            .getJSONArray("coordinates")
        val LatLngList = mutableListOf<LatLng>()
        for (pair in 0..routeCoordinates.length() - 1) {
            LatLngList.add(
                LatLng(
                    routeCoordinates.getJSONArray(pair)[1].toString().toDouble(),
                    routeCoordinates.getJSONArray(pair)[0].toString().toDouble()
                )
            )
        }

        Log.d("response", routeCoordinates.toString())

        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.
        // [START maps_poly_activity_add_polyline_set_tag]
        // [START maps_poly_activity_add_polyline]
        polyline = googleMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .add(
                    *LatLngList.toTypedArray()
                )
        )
        // [END maps_poly_activity_add_polyline]
        // [START_EXCLUDE silent]
        // Store a data object with the polyline, used here to indicate an arbitrary type.
        polyline.tag = "B"
        // [END maps_poly_activity_add_polyline_set_tag]
        // Style the polyline.
        stylePolyline(polyline)
        getAlarmPermission()
        registerAlarm()
    }

    private fun registerAlarm() {
        // cancel previous alarm
        try {
            pendingIntent.cancel()
        } catch (e: UninitializedPropertyAccessException) {

        }

        nextEvent = calendarViewModel.events.value[0]

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java)
        intent.action = "com.example.cs501_classgenie.alarmManager"
        intent.putExtra("textTitle", nextEvent.summary)
        intent.putExtra("textContent", nextEvent.location)

        pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            AlarmReceiver.requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        var eventStart = nextEvent.start
        var alarmTime = LocalDateTime.ofEpochSecond(eventStart.value, 0, UTC)

//        var current = LocalDateTime.now()
        alarmTime = alarmTime.minusSeconds(routeTravelTimeInSecond)

        val cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarmTime.hour);  // set hour
        cal.set(Calendar.MINUTE, alarmTime.minute);          // set minute
        cal.set(Calendar.SECOND, alarmTime.second);               // set seconds
        Log.d("notification","alarm set ${cal.get(Calendar.HOUR_OF_DAY)}:${cal.get(Calendar.MINUTE)}:${cal.get(Calendar.SECOND)}")
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.getTimeInMillis(),
            pendingIntent
        );
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
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10f
                )
            }

            "B" -> {
                // Use a round cap at the start of the line.
                polyline.startCap = RoundCap()
            }
        }
        polyline.endCap = CustomCap(
            BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10f
        )
        polyline.width = POLYLINE_STROKE_WIDTH_PX.toFloat()
        polyline.color = COLOR_BLACK_ARGB
        polyline.jointType = JointType.ROUND
    }
    // [END maps_poly_activity_style_polyline]

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     * hope this always work
     */
    private fun enableMyLocation() {
//        if (!::mMap.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
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


    /**
     * Getting Permissions
     */
    private fun getAllPermission(){
        PermissionUtils.requestPermission(
            this,
            LOCATION_PERMISSION_REQUEST_CODE,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.SCHEDULE_EXACT_ALARM
                ),
            true
        )
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            //mMap.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(
                this,
                LOCATION_PERMISSION_REQUEST_CODE,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                true
            )
        }
    }

    private fun getNotificationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
//            requestNotificationPermission = false
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(
                this,
                NOTIFICATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.POST_NOTIFICATIONS,
                true
            )
        }
    }

    private fun getAlarmPermission(
    ) {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
            == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(
                this,
                ALARM_PERMISSION_REQUEST_CODE,
                Manifest.permission.SCHEDULE_EXACT_ALARM,
                true
            )
        }
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d("onRequestPermissionsResult",requestCode.toString())
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(
                    permissions,
                    grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || PermissionUtils.isPermissionGranted(
                    permissions,
                    grantResults,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                Log.d(TAG,"address premission granted")
                enableMyLocation()
            } else {
//                permissionDenied = true
//                throw Exception("location permission not granted")
            }
        }

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(
                    permissions,
                    grantResults,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG,"notification premission granted")
                    val notification: Notification =
                        NotificationCompat.Builder(this, getString(R.string.channel_id))
                            .setSmallIcon(R.drawable.ic_arrow)
                            .setContentTitle("test permission")
                            .setContentText("test permission")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .build()
                    // notificationId is a unique int for each notification that you must define.
                    val notificationManager = NotificationManagerCompat.from(this)
                    notificationManager.notify(2, notification)
                } else {
                    throw Exception("location permission should be granted")
                }
            } else {
//                throw Exception("location permission not granted")
            }
        }

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(
                    permissions,
                    grantResults,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                )
            ) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG,"alarm premission granted")
                } else {
//                    throw Exception("location permission should be granted")
                }
            } else {
//                throw Exception("location permission not granted")
            }
        }

        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
//        if (PermissionUtils.isPermissionGranted(
//                permissions,
//                grantResults,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            )
//        ) {
//            // Enable the my location layer if the permission has been granted.
//            enableMyLocation()
//        } else {
//            // Permission was denied. Display an error message
//            // [START_EXCLUDE]
//            // Display the missing permission error dialog when the fragments resume.
//            permissionDenied = true
//            // [END_EXCLUDE]
//        }
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 114514
        private const val ALARM_PERMISSION_REQUEST_CODE = 3
    }
}