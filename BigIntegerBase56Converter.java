import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class BigIntegerBase56Converter {

    private static final String BASE56_CHARACTERS = "0123456789ABCDFHIJLMNOPQRSTUVWXYZabcefgijklmnopqrtuvwxyz"; // 注意自行替换和改造，需结合其他的class更改
    private static final BigInteger BASE = BigInteger.valueOf(56);

    private static final Map<Character, Integer> CHARACTER_INDEX_MAP = new HashMap<>();

    static {
        for (int i = 0; i < BASE56_CHARACTERS.length(); i++) {
            CHARACTER_INDEX_MAP.put(BASE56_CHARACTERS.charAt(i), i);
        }
    }

    public static String toBase56(BigInteger number) {
        if (number.equals(BigInteger.ZERO)) {
            return "0";
        }

        StringBuilder base56 = new StringBuilder();
        boolean isNegative = number.signum() == -1;
        if (isNegative) {
            number = number.negate();
        }

        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divAndRem = number.divideAndRemainder(BASE);
            base56.append(BASE56_CHARACTERS.charAt(divAndRem[1].intValue()));
            number = divAndRem[0];
        }

        if (isNegative) {
            base56.append("-");
        }

        return base56.reverse().toString();
    }

    public static BigInteger fromBase56(String base56) {
        if (base56 == null || base56.isEmpty()) {
            throw new IllegalArgumentException("Base56 string cannot be null or empty");
        }

        boolean isNegative = base56.startsWith("-");
        if (isNegative) {
            base56 = base56.substring(1);
        }

        BigInteger number = BigInteger.ZERO;
        for (char c : base56.toCharArray()) {
            Integer index = CHARACTER_INDEX_MAP.get(c);
            if (index == null) {
                throw new IllegalArgumentException("Invalid character in Base56 string: " + c);
            }
            number = number.multiply(BASE).add(BigInteger.valueOf(index));
        }

        return isNegative ? number.negate() : number;
    }

    public static void main(String[] args) {
        testBase56Conversion(new BigInteger("123456789123456789"));
        testBase56Conversion(new BigInteger("-987654321987654321"));
        testInvalidBase56Input();
    }

    private static void testBase56Conversion(BigInteger number) {
        String base56 = toBase56(number);
        BigInteger recovered = fromBase56(base56);
        System.out.println("Original: " + number);
        System.out.println("Base56: " + base56);
        System.out.println("Recovered: " + recovered);
        System.out.println("Test passed: " + number.equals(recovered));
        System.out.println();
    }

    private static void testInvalidBase56Input() {
        try {
            fromBase56(null);
        } catch (Exception e) {
            System.out.println("Passed null test: " + e.getMessage());
        }

        try {
            fromBase56("!");
        } catch (Exception e) {
            System.out.println("Passed invalid character test: " + e.getMessage());
        }
    }
}
