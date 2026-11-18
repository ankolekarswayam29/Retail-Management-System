
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String password = "YOUR_ADMIN_PASSWORD";

        String newHash = encoder.encode(password);

        System.out.println("New BCrypt Hash: " + newHash);

        System.out.println(
                "Verification: " +
                        encoder.matches(password, newHash)
        );
    }
}
