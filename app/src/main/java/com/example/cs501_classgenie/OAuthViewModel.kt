package com.example.cs501_classgenie

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
import java.io.InputStream
import java.io.InputStreamReader

private const val TOKENS_DIRECTORY_PATH = "/tokens"

private val httpTransport: HttpTransport = NetHttpTransport()

//private var tokenFolder = File(Environment.getExternalStorageDirectory().toString() + File.separator + TOKENS_DIRECTORY_PATH)

//private val dataStoreFactory = FileDataStoreFactory(tokenFolder) //error message is java.io.IOException: unable to create directory: /tokens

private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

//private const val CREDENTIALS_FILE_PATH = "/credentials.json"

//source: https://github.com/0xsanchit/MongoDbRealmCourse_Android/blob/master/app/src/main/java/com/example/mongodbrealmcourse/MainActivity.java
//source: https://stackoverflow.com/questions/25094834/is-it-possible-to-use-com-sun-net-httpserver-package-in-android-program


/** Authorizes the installed application to access user's protected data.  */

//source: Google Calendar API Quickstart Java (https://developers.google.com/calendar/api/quickstart/java)
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

class OAuthViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = getApplication()

    private  fun getTokenFolder(): File {
        return File(app.getExternalFilesDir("")?.absolutePath + TOKENS_DIRECTORY_PATH)
    }

    private  fun getCredentialFileStream(): InputStream {

        return app.resources.openRawResource(R.raw.credentials)
    }

    fun authorize(): com.google.api.client.auth.oauth2.Credential{
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

        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")

    }


}