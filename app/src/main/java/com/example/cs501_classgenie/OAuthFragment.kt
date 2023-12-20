package com.example.cs501_classgenie

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cs501_classgenie.databinding.FragmentOAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.text.SimpleDateFormat

//https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
private var DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") //doesn't work with string "2023-12-18T18:34:49.254-05:00"
//note: for notifications have a fall-back if unable to connect with google maps
//maybe cache google maps directions in advance in terms of time to destination?
//or, more simply just set it to 10 min before if unable to get google maps data

//what is left:
//automatic sync
//maybe more advanced sync action (instead of push a button, maybe try swiping down?)
class OAuthFragment : Fragment() {

    private var isLoggedIn = false

    companion object {
        private const val REQUEST_SIGN_IN = 1
        private lateinit var calendar: Calendar
        private lateinit var calendar_events: List<CalendarEvent>
        private var nextEvent: MutableLiveData<CalendarEvent?> = MutableLiveData<CalendarEvent?>()
        private var nextEventSummaryText: MutableLiveData<String> = MutableLiveData<String>()
        private var nextEventLocationText: MutableLiveData<String?> = MutableLiveData<String?>()
        private var nextEventStartText: MutableLiveData<String> = MutableLiveData<String>()

    }

    val calendarViewModel: CalendarViewModel by activityViewModels()

    private var _binding: FragmentOAuthBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOAuthBinding.inflate(inflater, container, false)



        val syncButton: Button = binding.syncButton
        val mapButton: Button = binding.mapButton
        val nextEventSummary: TextView = binding.nextEventSummary
        val nextEventStart: TextView = binding.nextEventStart
        val nextEventLocation: TextView = binding.nextEventLocation
        val swipeRefresh: SwipeRefreshLayout = binding.swiperefresh

        lifecycleScope.launch{

            Log.d("OAuth", "about to start initial auth coroutine")

            //source: https://kotlinlang.org/docs/flow.html#flow-context
            //source: https://stackoverflow.com/questions/67457208/kotlin-coroutines-why-withcontext-in-coroutinescope

            withContext(Dispatchers.IO){
                Log.d("OAuth", "authorization coroutine started")
                if (!isLoggedIn) {
                    requestSignIn(requireActivity().baseContext)

                }

            }

        }

        syncButton.setOnClickListener{
            Log.d("Calendar", "initializing refresh of cache")
            refresh_cache()
        }

        swipeRefresh.setOnRefreshListener {
            Log.i("Calendar", "onRefresh called from SwipeRefreshLayout")

            // This method performs the actual data-refresh operation and calls
            // setRefreshing(false) when it finishes.
            refresh_cache()
            swipeRefresh.isRefreshing = false
        }

        mapButton.setOnClickListener{
            val myIntent = Intent(getActivity(), MapsActivity::class.java)
            startActivity(myIntent)
        }

        nextEventSummaryText.observe(viewLifecycleOwner, Observer{
            nextEventSummary.text = nextEventSummaryText.value
        })

        nextEventLocationText.observe(viewLifecycleOwner, Observer{
            nextEventLocation.text = nextEventLocationText.value
        })

        nextEventStartText.observe(viewLifecycleOwner, Observer{
            nextEventStart.text = DATE_FORMATTER.format(DATE_FORMATTER.parse(nextEventStartText.value))
        })



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                calendarViewModel.events.collect {
                    events -> calendar_events = events
                }
            }
        }



    }



    private fun return_next_event(){

        val size = calendar_events.size
        Log.d("Calendar", size.toString())
        val now = DateTime(System.currentTimeMillis())

        if (size > 0){
            val max = DateTime(now.value+8*24*60*60*1000)
            var result = calendar_events[0]
            var current_smallest = max.value-now.value

            for (event in calendar_events){
                val difference = event.start.value - now.value

                if (difference < current_smallest){
                    current_smallest = difference
                    var result = event
                }
            }
            Log.d("Calendar", "found next event")
            Log.d("Calendar", result.location.toString())
            nextEventSummaryText.postValue(result.summary)
            nextEventLocationText.postValue(result.location)
            nextEventStartText.postValue(result.start.toString())
            nextEvent.postValue(result)


        } else {
            nextEventSummaryText.postValue("No Event Available")
            nextEventLocationText.postValue("")
            nextEventStartText.postValue(now.toString())

        }


    }

    private fun refresh_cache(){
        if (calendarViewModel.isOnline(requireActivity().baseContext)){
            try{

                viewLifecycleOwner.lifecycleScope.launch{
                    withContext(Dispatchers.IO){
                        calendarViewModel.clearAll()


                        val now = DateTime(System.currentTimeMillis())
                        Log.d("Calendar", now.toString())

                        val events: Events = calendar.events().list("primary")
                            .setTimeMin(now)
                            .setTimeMax(DateTime(now.value+7*24*60*60*1000))
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute()

                        val items: List<Event> = events.items
                        Log.d("Calendar", "events retrieved")

                        if (items.isEmpty()) {
                            Log.d("Calendar", "No upcoming events found.")
                        } else {

                            for (item in items){
                                //Log.d("Calendar", item.summary)
                                //Log.d("Calendar", item.start.dateTime.toString())
                                //Log.d("Calendar", item.end.dateTime.toString())
                                //Log.d("Calendar", item.location)
                                val event = CalendarEvent(UUID.randomUUID(), item.summary, item.start.dateTime, item.end.dateTime, item.location)
                                calendarViewModel.insertEvent(event)
                            }


                        }
                    }


                }
            } catch (e: Exception) {
                Log.d ("Calendar", "exception while refreshing cache")
            }
        } else {
            Log.d ("Calendar", "No internet connection")
        }

        return_next_event()
    }


    //source: https://stackoverflow.com/questions/74555485/java-lang-illegalargumentexception-the-name-must-not-be-empty-null-error-on
    private fun requestSignIn(context: Context) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, signInOptions)

        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("onActivityResult", "${requestCode},${resultCode}")
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener { account ->
                        val scopes = listOf(CalendarScopes.CALENDAR)
                        val credential = GoogleAccountCredential.usingOAuth2(requireActivity().baseContext, scopes)
                        credential.selectedAccount = account.account

                        val jsonFactory = GsonFactory.getDefaultInstance()

                        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                        calendar = Calendar.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(getString(R.string.app_name))
                            .build()
                        Log.d("Calendar", "calendar retrieved")
                        Log.d("Calendar", calendar.toString())

                        refresh_cache()

                    }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}