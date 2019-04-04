import java.util.Date;

public class UserEmail {
    private String email;
    private Date sentDate;

    UserEmail(String email, Date sentDate) {
        this.email = email;
        this.sentDate = sentDate;
    }

    public String getEmail() {
        return email;
    }

    public Date getSentDate() {
        return sentDate;
    }

    @Override
    public String toString() {
        return "UserEmail{" +
                "email='" + email + '\'' +
                ", sentDate=" + sentDate +
                '}';
    }
}
