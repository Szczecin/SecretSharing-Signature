import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class SecretSharingStringWithMod {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // 模数 P，必须为一个足够大的素数
    private static final BigInteger PRIME_MODULUS = new BigInteger("340282366920938463463374607431768211507");

    /**
     * 将字符串秘密转换为 BigInteger，并生成分片
     */
    public static List<BigInteger[]> generateShares(String secret, int numShares, int threshold) {
        validateInput(secret, numShares, threshold);

        // 将字符串秘密转换为 BigInteger
        BigInteger secretNumber = stringToBigInteger(secret);

        // 验证秘密是否小于模数 P
        if (secretNumber.compareTo(PRIME_MODULUS) >= 0) {
            throw new IllegalArgumentException("秘密过大，请选择更大的模数 P 或分段处理秘密。");
        }

        // 生成随机系数
        List<BigInteger> coefficients = generateCoefficients(secretNumber, threshold);

        // 生成分片
        return calculateShares(coefficients, numShares);
    }

    /**
     * 根据分片恢复秘密
     */
    public static String reconstructSecret(List<BigInteger[]> shares) {
        if (shares == null || shares.size() < 2) {
            throw new IllegalArgumentException("至少需要两个分片才能恢复秘密。");
        }

        BigInteger recoveredSecret = lagrangeInterpolation(shares);
        return bigIntegerToString(recoveredSecret);
    }

    // 私有方法：字符串转换为 BigInteger
    private static BigInteger stringToBigInteger(String str) {
        return new BigInteger(str.getBytes());
    }

    // 私有方法：BigInteger 转换为字符串
    private static String bigIntegerToString(BigInteger number) {
        return new String(number.toByteArray());
    }

    // 私有方法：生成随机多项式系数
    private static List<BigInteger> generateCoefficients(BigInteger secret, int threshold) {
        List<BigInteger> coefficients = new ArrayList<>();
        coefficients.add(secret); // 常数项是秘密
        for (int i = 1; i < threshold; i++) {
            coefficients.add(new BigInteger(PRIME_MODULUS.bitLength() - 1, SECURE_RANDOM).mod(PRIME_MODULUS));
        }
        return coefficients;
    }

    // 私有方法：计算分片 (x, f(x))
    private static List<BigInteger[]> calculateShares(List<BigInteger> coefficients, int numShares) {
        List<BigInteger[]> shares = new ArrayList<>();
        for (int x = 1; x <= numShares; x++) {
            BigInteger xi = BigInteger.valueOf(x);
            BigInteger yi = evaluatePolynomial(coefficients, xi).mod(PRIME_MODULUS);
            shares.add(new BigInteger[] { xi, yi });
        }
        return shares;
    }

    // 私有方法：计算多项式 f(x)
    private static BigInteger evaluatePolynomial(List<BigInteger> coefficients, BigInteger x) {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < coefficients.size(); i++) {
            result = result.add(coefficients.get(i).multiply(x.pow(i))).mod(PRIME_MODULUS);
        }
        return result;
    }

    // 私有方法：使用拉格朗日插值恢复秘密
    private static BigInteger lagrangeInterpolation(List<BigInteger[]> shares) {
        BigInteger secret = BigInteger.ZERO;

        for (int i = 0; i < shares.size(); i++) {
            BigInteger[] shareI = shares.get(i);
            BigInteger xi = shareI[0];
            BigInteger yi = shareI[1];

            BigInteger li = BigInteger.ONE; // 拉格朗日基函数
            for (int j = 0; j < shares.size(); j++) {
                if (i != j) {
                    BigInteger xj = shares.get(j)[0];
                    BigInteger numerator = xj.negate().mod(PRIME_MODULUS); // -xj
                    BigInteger denominator = xi.subtract(xj).mod(PRIME_MODULUS).modInverse(PRIME_MODULUS);
                    li = li.multiply(numerator).multiply(denominator).mod(PRIME_MODULUS);
                }
            }
            secret = secret.add(yi.multiply(li)).mod(PRIME_MODULUS);
        }

        return secret;
    }

    // 私有方法：输入验证
    private static void validateInput(String secret, int numShares, int threshold) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("秘密不能为空或空字符串。");
        }
        if (threshold < 2) {
            throw new IllegalArgumentException("阈值必须至少为 2。");
        }
        if (numShares < threshold) {
            throw new IllegalArgumentException("分片总数必须大于或等于阈值。");
        }
    }
}
