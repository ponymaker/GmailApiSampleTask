import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Oath2GmailService {

    /**
     * OAuth 2.0 scopes.
     */
    private static final List<String> SCOPES = Collections.singletonList(
            "https://www.googleapis.com/auth/gmail.readonly"
    );
    private static FileDataStoreFactory dataStoreFactory;
    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(".store/oauth2_sample");
    private static final JsonFactory GSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Magic constant to work with the same user
     */
    private static final String userId = "me";

    private static HttpTransport httpTransport;

    private static Gmail gmailService;

    /**
     * Authorizes the installed application to access user's protected data.
     */
    private static Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(GSON_FACTORY,
                new InputStreamReader(GmailServiceExample.class.getResourceAsStream("/client_secrets.json")));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/ "
                    + "into oauth2-cmdline-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }
        // set up authorization code flow
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, GSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(
                dataStoreFactory).build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * Performs authorization and initialization of GmailService.
     */
    public static void init(String applicationName) throws Exception {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize();
        gmailService = new Gmail.Builder(httpTransport, GSON_FACTORY, credential).setApplicationName(applicationName).build();
    }

    public static List<MimeMessage> listMimeMessages(String query) throws IOException {
        Gmail.Users.Messages.List messagesList = gmailService.users().messages().list(userId);
        ListMessagesResponse response = ("all".equals(query) ? messagesList : messagesList.setQ(query)).execute();
        List<Message> messages = new ArrayList<>();
        int counter = 0;
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            System.out.println("Loaded: " + counter++ + " from " + response.getResultSizeEstimate());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = gmailService.users().messages().list(userId).setQ(query)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }
        return messages.stream().parallel().map(msg -> getMimeMessage(msg.getId())).collect(Collectors.toList());
    }

    private static MimeMessage getMimeMessage(String messageId) {
        Message message = null;
        try {
            message = gmailService.users().messages().get(userId, messageId).setFormat("raw").execute();
            byte[] emailBytes = Base64.decodeBase64(message.getRaw());
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            return new MimeMessage(session, new ByteArrayInputStream(emailBytes));
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
