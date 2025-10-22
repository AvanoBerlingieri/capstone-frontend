package capstone.safeline.api;

import java.util.Date;

public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private String status;
    private Date created_at;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreated_at() {
        return created_at;
    }
}
