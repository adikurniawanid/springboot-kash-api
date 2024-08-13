import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class tes {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[100];
        SecureRandom.getInstanceStrong().nextBytes(bytes);
        System.out.println(Arrays.toString(bytes).formatted("%02x"));
    }
}
