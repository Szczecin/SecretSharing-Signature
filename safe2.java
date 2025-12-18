import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class safe2 {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String[] EXCLUDING_CHARACTERS = { "G", "h", "K", "d", "E", "s" }; // 注意自行替换和改造，需结合其他的class更改

    public static String clientsend(long timestamp) {
        int poolIndex = getPoolIndex(timestamp);

        String selectedPool = RandomCodeGenerator.getPool(poolIndex);
        String Safe1 = RandomCodeGenerator.generateRandomCode(selectedPool, RANDOM.nextInt(6) + 35);
        String Safe1MD5 = RandomCodeGenerator.getMD5Hash(Safe1);

        System.out.println("随机数选择池: " + poolIndex);
        System.out.println("safe1: " + Safe1);
        System.out.println("safe1 MD5-16: " + Safe1MD5);

        List<BigInteger[]> shares = SecretSharingStringWithMod.generateShares(Safe1MD5, 5, 2);

        System.out.println("生成的分片初始值: ");
        for (BigInteger[] share : shares) {
            System.out.printf("x: %s, y: %s%n", share[0], share[1]);
        }

        BigInteger y0 = shares.get(poolIndex % 4)[1].subtract(BigInteger.valueOf(poolIndex));
        BigInteger y1 = shares.get((poolIndex % 4) + 1)[1].add(BigInteger.valueOf(poolIndex));
        System.out.println("y0-base56: " + BigIntegerBase56Converter.toBase56(y0));
        System.out.println("y1-base56: " + BigIntegerBase56Converter.toBase56(y1));

        return BigIntegerBase56Converter.toBase56(y0) + EXCLUDING_CHARACTERS[poolIndex]
                + BigIntegerBase56Converter.toBase56(y1);
    }

    public static String servercheck(String newSafe1, long timestamp) {
        int poolIndex = getPoolIndex(timestamp);
        String delimiter = EXCLUDING_CHARACTERS[poolIndex];

        validateNewSafe1(newSafe1, delimiter);

        int delimiterIndex = newSafe1.indexOf(delimiter);
        String part1 = newSafe1.substring(0, delimiterIndex);
        String part2 = newSafe1.substring(delimiterIndex + 1);

        BigInteger y0 = BigIntegerBase56Converter.fromBase56(part1).add(BigInteger.valueOf(poolIndex));
        BigInteger y1 = BigIntegerBase56Converter.fromBase56(part2).subtract(BigInteger.valueOf(poolIndex));

        List<BigInteger[]> recoveryShares = new ArrayList<>();
        recoveryShares.add(new BigInteger[] { BigInteger.valueOf((poolIndex % 4) + 1), y0 });
        recoveryShares.add(new BigInteger[] { BigInteger.valueOf((poolIndex % 4) + 2), y1 });

        return SecretSharingStringWithMod.reconstructSecret(recoveryShares);
    }

    private static int getPoolIndex(long timestamp) {
        return (int) (timestamp % 10 % 6);
    }

    private static void validateNewSafe1(String newSafe1, String delimiter) {
        if (newSafe1 == null || !newSafe1.contains(delimiter)) {
            throw new IllegalArgumentException("Invalid newSafe1 format: Missing delimiter '" + delimiter + "'");
        }
    }

    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();
        String newSafe1 = clientsend(timestamp);
        System.out.println("newSafe1: " + newSafe1);
        String recoveredSecret = servercheck(newSafe1, timestamp);

        System.out.println("Recovered Secret: " + recoveredSecret);
    }
}
