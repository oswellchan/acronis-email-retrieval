import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class EmailBackup {

    private static EmailBackup instance = null;

    Gmail service = null;

    /** Application name. */
    private final String APPLICATION_NAME = "Email Backup";

    /** Directory to store user credentials for this application. */
    private final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/email-backup.json");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private HttpTransport HTTP_TRANSPORT;

    private String TOKEN_REVOKE_URL = "https://accounts.google.com/o/oauth2/revoke?token=";

    private final String FEEDBACK_LOGOUTSUCCESS = "Logout successful. It might take some time before the revocation has full effect.";

    private Credential credential = null;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/gmail-java-quickstart.json
     */
    private final List<String> SCOPES =
        Arrays.asList(GmailScopes.GMAIL_READONLY);

    private EmailBackup() {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            service = getGmailService();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
    public static EmailBackup getInstance() {
        if(instance == null) {
            instance = new EmailBackup();
        }
        return instance;
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    private Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
            EmailBackup.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    private Gmail getGmailService() throws IOException {
        credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Message> listMessagesMatchingQuery(String userId, String query) throws IOException {
        ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId).setQ(query)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        return messages;
    }

    public byte[] getMessageRaw(String id) throws IOException {
        String user = "me";
        Message m = service.users().messages().get(user, id).setFormat("raw").execute();
        return m.decodeRaw();
    }

    public String getEmailAddress() throws IOException {
        Profile response = service.users().getProfile("me").execute();
        return response.getEmailAddress();
    }

    public void revokeCredentials() {
        try {
            URLConnection connection = new URL(TOKEN_REVOKE_URL + credential.getAccessToken()).openConnection();
            connection.setRequestProperty("Accept-Charset", "utf-8");
            InputStream response = connection.getInputStream();

            Scanner scanner = new Scanner(response);
            String responseBody = scanner.useDelimiter("\\A").next();

            Path file = Paths.get(DATA_STORE_DIR + "\\StoredCredential");
            Files.delete(file);
            System.out.println(FEEDBACK_LOGOUTSUCCESS);
        } catch (Exception e) {
            System.out.println("Logout unsuccessful. Error: " + e.getMessage());
        }
    }
}