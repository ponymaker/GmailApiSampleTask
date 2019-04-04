import com.google.api.client.http.HttpTransport;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmailServiceExample {
    private static final String APPLICATION_NAME = "emailcollector";
    private static final Pattern emailPattern = Pattern.compile("([a-zA-Z0-9_.-]+)@([a-zA-Z0-9_.-]+)\\.[a-zA-Z]+");
    private static final String filterQuery = "from:gmail.com";

    private static HttpTransport httpTransport;

    public static void main(String[] args) {
        ArrayList<UserEmail> parsedEmails = new ArrayList<>();
        ArrayList<String> notParsedEmails = new ArrayList<>();
        try {
            Oath2GmailService.init(APPLICATION_NAME);
            List<MimeMessage> mimeMessages = Oath2GmailService.listMimeMessages(filterQuery);
            for (MimeMessage mimeMessage : mimeMessages) {
                String messageBody = (String) mimeMessage.getContent();
                Matcher matcher = emailPattern.matcher(messageBody);
                if (matcher.find()) {
                    parsedEmails.add(new UserEmail(matcher.group(), mimeMessage.getSentDate()));
                } else {
                    notParsedEmails.add(messageBody);
                }
            }
            System.out.println("Total message downloaded: " + mimeMessages.size());
        } catch (Exception e) {
            System.err.println("Something gone wrong!");
        }
        System.out.println("Parsed: " + parsedEmails.size());
        System.out.println(parsedEmails);
        System.out.println("Not parsed: " + notParsedEmails.size());
        System.out.println(notParsedEmails);
    }
}