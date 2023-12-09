package com.example.cs501_classgenie

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.cs501_classgenie.databinding.FragmentOAuthBinding
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cs501_classgenie.database.EventDAO
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
import kotlinx.coroutines.withContext
import java.util.UUID

//private const val TOKENS_DIRECTORY_PATH = "/tokens"

//private val httpTransport: HttpTransport = NetHttpTransport()

//private var tokenFolder = File(Environment.getExternalStorageDirectory().toString() + File.separator + TOKENS_DIRECTORY_PATH)

//private val dataStoreFactory = FileDataStoreFactory(tokenFolder) //error message is java.io.IOException: unable to create directory: /tokens

//private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()


//note: for notifications have a fall-back if unable to connect with google maps
//maybe cache google maps directions in advance in terms of time to destination?
//or, more simply just set it to 10 min before if unable to get google maps data
class OAuthFragment : Fragment() {

    private var isLoggedIn = false

    companion object {
        private const val REQUEST_SIGN_IN = 1
        private lateinit var calendar: Calendar
        private lateinit var calendar_events: List<CalendarEvent>
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



        val sync_button: Button = binding.syncButton
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


        //to-do: start log-in automatically, not at the sync button, that button should be for pulling events from calendar

        sync_button.setOnClickListener{
            Log.d("Calendar", "initializing refresh of cache")
            refresh_cache()
        }




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
        Log.d("Calendar", calendar_events.size.toString())

        for (event in calendar_events){
            Log.d("Calendar", "event retrieved from database")
            event.start.let { Log.d("Calendar", it.toString())}
            event.location?.let { Log.d("Calendar", it)}
        }
    }

    private fun refresh_cache(){
        if (calendarViewModel.isOnline(requireActivity().baseContext)){
            try{

                //sample code for retrieving event data; the coroutine is necessary
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

        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener { account ->
                        val scopes = listOf(CalendarScopes.CALENDAR)
                        val credential = GoogleAccountCredential.usingOAuth2(requireActivity().baseContext, scopes)
                        credential.selectedAccount = account.account
                        /*
                        val app: Application = requireActivity().application

                        fun getTokenFolder(): File {
                            return File(app.getExternalFilesDir("")?.absolutePath + TOKENS_DIRECTORY_PATH)
                        }

                        fun getCredentialFileStream(): InputStream {

                            return app.resources.openRawResource(R.raw.credentials)
                        }

                         */

                        val jsonFactory = GsonFactory.getDefaultInstance()

                        // load client secrets (not needed)
                        /*
                        val clientSecrets = GoogleClientSecrets.load(
                            jsonFactory,
                            InputStreamReader(getCredentialFileStream())
                        )
                         */
                        //Log.d("OAuth", "clientSecrets loaded")

                        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                        calendar = Calendar.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(getString(R.string.app_name))
                            .build()
                        Log.d("Calendar", "calendar retrieved")
                        Log.d("Calendar", calendar.toString())

                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}