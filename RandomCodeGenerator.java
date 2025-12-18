import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private enum CharacterPool {
        LOWERCASE_NUMERIC("abcdefghijklmnopqrstuvwxyz0123456789"),
        UPPERCASE_NUMERIC("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"),
        MIXEDCASE_NUMERIC("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"),
        UPPERCASE_EXCLUDE_XXXXX("ABCFIJLMNOPQRTUVWXYZ0123456789"), // 注意自行替换和改造，需结合其他的class更改
        LOWERCASE_EXCLUDE_xxxxx("abcefgijlmopqrstuvwxy0123456789"), // 注意自行替换和改造，需结合其他的class更改
        NUMERIC_EXCLUDE_528("0134679");

        private final String pool;

        CharacterPool(String pool) {
            this.pool = pool;
        }

        public String getPool() {
            return pool;
        }
    }

    public static String getPool(int index) {
        try {
            return CharacterPool.values()[index].getPool();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid pool index");
        }
    }

    public static String generateRandomCode(String pool, int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(pool.charAt(RANDOM.nextInt(pool.length())));
        }
        return code.toString();
    }

    public static String getMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());

            // Convert to a specific segment of hexadecimal representation
            StringBuilder hash = new StringBuilder();
            for (int i = 4; i < 12; i++) {
                hash.append(String.format("%02x", hashBytes[i]));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating MD5 hash", e);
        }
    }
}
