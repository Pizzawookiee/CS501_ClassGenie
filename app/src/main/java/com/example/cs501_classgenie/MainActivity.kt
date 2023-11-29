package com.example.cs501_classgenie

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.CalendarScopes
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


private const val TOKENS_DIRECTORY_PATH = "tokens"

private val httpTransport: HttpTransport = NetHttpTransport()

private val dataStoreFactory = FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)) //error message is java.io.IOException: unable to create directory: /tokens

private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

private const val CREDENTIALS_FILE_PATH = "/credentials.json"

private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 123

private const val RC_GET_AUTH_CODE = 1234



//source: https://github.com/0xsanchit/MongoDbRealmCourse_Android/blob/master/app/src/main/java/com/example/mongodbrealmcourse/MainActivity.java



/** Authorizes the installed application to access user's protected data.  */

/*
@Throws(Exception::class)
private fun authorize(): com.google.api.client.auth.oauth2.Credential {
    // load client secrets
    val clientSecrets = GoogleClientSecrets.load(
        JSON_FACTORY,
        InputStreamReader(FileInputStream(CREDENTIALS_FILE_PATH))
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
    return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
}
*/
/*
object CalendarQuickstart {
    /**
     * Application name.
     */
    private const val APPLICATION_NAME = "Google Calendar API Java Quickstart"

    /**
     * Global instance of the JSON factory.
     */
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

    /**
     * Directory to store authorization tokens for this application.
     */
    private const val TOKENS_DIRECTORY_PATH = "tokens"

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf<String>(CalendarScopes.CALENDAR_READONLY)
    private const val CREDENTIALS_FILE_PATH = "/credentials.json"

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): com.google.api.client.auth.oauth2.Credential {
        // Load client secrets.
        val `in` =
            CalendarQuickstart::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
                ?: throw FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY,
            InputStreamReader(`in`)
        )

        // Build flow and trigger user authorization request.
        val flow: GoogleAuthorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(8888).build()
        //returns an authorized Credential object.
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    @JvmStatic
    fun get_ten_events() {
        // Build a new authorized API client service.
        val HTTP_TRANSPORT: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val service: Calendar =
            Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build()

        // List the next 10 events from the primary calendar.
        val now = DateTime(System.currentTimeMillis())
        val events: Events = service.events().list("primary")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()
        val items: List<Event> = events.getItems()
        if (items.isEmpty()) {
            println("No upcoming events found.")
        } else {
            println("Upcoming events")
            for (event in items) {
                var start: DateTime = event.getStart().getDateTime()
                if (start == null) {
                    start = event.getStart().getDate()
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start)
            }
        }
    }
}
*/
//maybe make a 'map not available' feature somehow if the next event in schedule doesn't have location or network error or something else?
class MainActivity : AppCompatActivity() {

    //source: https://medium.com/@angakoko/firbase-google-sign-in-in-android-app-using-kotlin-d63ae127206d
    //source: https://stackoverflow.com/questions/75137115/startactivityforresultandroid-content-intent-int-is-deprecated-what-can-i-d
    //source: https://github.com/0xsanchit/MongoDbRealmCourse_Android/blob/master/app/src/main/java/com/example/mongodbrealmcourse/MainActivity.java

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .build()

    val googleSignInClient = GoogleSignIn.getClient(this, gso)

    private fun getAuthCode() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this,
            OnCompleteListener<Void?> { })
    }

    private fun revokeAccess() {
        googleSignInClient.revokeAccess().addOnCompleteListener(this,
            OnCompleteListener<Void?> { })
    }


    var launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val authCode = account.serverAuthCode
                Log.d("OAuth", authCode.toString())
            } catch (e: ApiException) {
                Log.w("OAuth", "Sign-in failed", e)
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sync_button: Button = findViewById(R.id.sync_button)

        sync_button.setOnClickListener {
            getAuthCode()
        }
    }
}