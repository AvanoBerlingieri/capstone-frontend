package capstone.safeline.api.dto;

public class RegisterRequest {
    private String email;
    private String password;
    private String username;

    public RegisterRequest(String email, String password, String username){
        this.email = email;
        this.password = password;
        this.username = username;
    }

    public String getUsername() {return username;}
    public String getEmail() {return email;}
    public String getPassword() {return password;}
}
