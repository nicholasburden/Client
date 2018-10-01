import java.math.BigInteger;


public class RSA {
	public static synchronized String encrypt(String message, BigInteger n, BigInteger e) {
	    String x = (new BigInteger(message.getBytes())).modPow(e, n).toString();
	    return x;
	 }
	public static synchronized String decrypt(String message, BigInteger n, BigInteger d) {
		
		String x = new String((new BigInteger(message)).modPow(d, n).toByteArray());
		return x;
	}
}
