import java.io.*;
import java.util.Base64;
import org.springframework.util.SerializationUtils;

public class CookieSerializationUtils {

    // Serializes an object and encodes it into a Base64 string for cookie storage
    public static String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    // Decodes a Base64 string and deserializes it back into a specific class
    public static <T> T deserialize(String cookieValue, Class<T> cls) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(cookieValue);
        Object object = SerializationUtils.deserialize(decodedBytes);
        return cls.cast(object);
    }
}