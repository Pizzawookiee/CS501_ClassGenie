package com.example.cs501_classgenie

import android.accounts.Account
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import com.example.cs501_classgenie.databinding.FragmentOAuthBinding
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Arrays
import java.util.Collections

private const val TOKENS_DIRECTORY_PATH = "/tokens"

private val httpTransport: HttpTransport = NetHttpTransport()

//private var tokenFolder = File(Environment.getExternalStorageDirectory().toString() + File.separator + TOKENS_DIRECTORY_PATH)

//private val dataStoreFactory = FileDataStoreFactory(tokenFolder) //error message is java.io.IOException: unable to create directory: /tokens

private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

class OAuthFragment : Fragment() {

    private var isLoggedIn = false

    companion object {
        private const val REQUEST_SIGN_IN = 1
        fun newInstance() = OAuthFragment()
    }

    val OAuthViewModel: OAuthViewModel by activityViewModels()

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

        val app: Application = requireActivity().application

        fun getTokenFolder(): File {
            return File(app.getExternalFilesDir("")?.absolutePath + TOKENS_DIRECTORY_PATH)
        }

        fun getCredentialFileStream(): InputStream {

            return app.resources.openRawResource(R.raw.credentials)
        }
        /*
        fun authorize(): GoogleAccountCredential {
            Log.d("OAuth", "authorize function started")

            var tokenFolder = getTokenFolder()
            //var credentialFile = getCredentialFile()

            if (!tokenFolder.exists()) {
                tokenFolder.mkdirs()
            }

            /*
            Log.d("OAuth", credentialFile.absolutePath)
            if (!credentialFile.exists()){
                Log.d ("OAuth", "File not found.")
            }
            */

            val dataStoreFactory = FileDataStoreFactory(tokenFolder)
            // load client secrets
            val clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                InputStreamReader(getCredentialFileStream())
            )
            Log.d("OAuth", "clientSecrets loaded")

            // set up authorization code flow
            Log.d("OAuth", "initializing flow creation")
            val flow = GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, setOf<String>(CalendarScopes.CALENDAR)
            ).setDataStoreFactory(dataStoreFactory)
                .build()

            Log.d("OAuth", "flow created")
            // authorize

            val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(8888).build()
            Log.d("OAuth", "receiver created")

            //override onAuthorization function to avoid browse() which relies on java.awt.Desktop which doesn't exist on Android
            //source: https://cloud.google.com/java/docs/reference/google-oauth-client/latest/com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
            //source: https://stackoverflow.com/questions/52850911/java-awt-desktop-class


            /*
            val result =  AuthorizationCodeInstalledApp(flow, receiver){
                fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl) {
                    val url = (authorizationUrl.build())
                    Log.d("OAuth", authorizationUrl.toString())
                    val browserIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    activity?.startActivity(browserIntent)
                }
            }.authorize("user")

            //doesn't open new window to log in
            Log.d("OAuth", result.refreshToken.toString())
            */
            val mCredential = GoogleAccountCredential.usingOAuth2(activity, Collections.singleton(CalendarScopes.CALENDAR)).setBackOff(
                ExponentialBackOff()
            )

            Log.d("OAuth", mCredential.toString())




            /*
            val signedInAccount: GoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
            val account: Account = signedInAccount.getAccount()

            val credential: com.google.api.client.auth.oauth2.Credential = com.google.api.client.auth.oauth2.Credential.usingOAuth2(
                    context,
            Collections.singleton("https://www.googleapis.com/auth/calendar"));
            credential.setSelectedAccount(account)

             */

            return mCredential

        }

        fun get_ten_events() {
            // Build a new authorized API client service.
            val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val service: Calendar =
                Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize())
                    .setApplicationName(resources.getString(R.string.app_name))
                    .build()
            Log.d("Calendar", "calendar built")
            // List the next 10 events from the primary calendar.
            val now = DateTime(System.currentTimeMillis())
            val events: Events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
            val items: List<Event> = events.items
            Log.d("Calendar", "events retrieved")
            if (items.isEmpty()) {
                Log.d("Calendar", "No upcoming events found.")
            } else {
                Log.d("Calendar", "Upcoming events")
                for (event in items) {
                    var start: DateTime = event.start.dateTime
                    if (start == null) {
                        start = event.start.date
                    }
                    Log.d("Calendar", event.start.toString())
                }
            }
        }

        */

        sync_button.setOnClickListener{
            lifecycleScope.launch{
                Log.d("OAuth", "about to start coroutine")

                //source: https://kotlinlang.org/docs/flow.html#flow-context
                //source: https://stackoverflow.com/questions/67457208/kotlin-coroutines-why-withcontext-in-coroutinescope

                withContext(Dispatchers.IO){
                    Log.d("OAuth", "authorization coroutine started")
                    if (!isLoggedIn) {
                        requestSignIn(requireActivity().baseContext)
                    }
                    //get_ten_events()
                }
                /*
                launch{//this doesn't work???

                }

                 */

            }
        }
        /*
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sync_button.setOnClickListener {
                    //run OAuth code from ViewModel
                    Log.d("OAuth", "making call to ViewModel")

                    launch{//this doesn't work
                        Log.d("OAuth", "authorization coroutine started")
                        OAuthViewModel.authorize()
                    }

                }
            }
        }

         */



        return binding.root
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

                        val jsonFactory = GsonFactory.getDefaultInstance()
                        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                        val calendar = Calendar.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(getString(R.string.app_name))
                            .build()
                        Log.d("Calendar", "calendar retrieved")
                        Log.d("Calendar", calendar.toString())

                        val now = DateTime(System.currentTimeMillis())
                        Log.d("Calendar", now.toString())
                        /*

                        val events: Events = calendar.events().list("primary")
                            .setMaxResults(10)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute()

                        val items: List<Event> = events.items
                        Log.d("Calendar", "events retrieved")

                        if (items.isEmpty()) {
                            Log.d("Calendar", "No upcoming events found.")
                        } else {
                            Log.d("Calendar", "Upcoming events")
                            for (event in items) {
                                var start: DateTime = event.start.dateTime
                                if (start == null) {
                                    start = event.start.date
                                }
                                Log.d("Calendar", event.start.toString())
                            }
                        }

                         */
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}