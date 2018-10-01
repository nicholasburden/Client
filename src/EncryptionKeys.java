import java.math.BigInteger;
import java.security.SecureRandom;


public class EncryptionKeys{
	SecureRandom rand = new SecureRandom();
	public BigInteger nLocal, nForeign, eLocal, eForeign, d;
	public EncryptionKeys(int bits){
		
		BigInteger p = getNextPrime(new BigInteger(bits/2, rand));
		BigInteger q = getNextPrime(new BigInteger(bits/2, rand));
		nLocal = p.multiply(q);
		BigInteger phiN = (p.subtract(BigInteger.ONE)).multiply((q.subtract(BigInteger.ONE)));
		eLocal = new BigInteger("65537");
		while((GCD(eLocal, phiN).compareTo(BigInteger.ONE) > 0)){
			eLocal = eLocal.add(BigInteger.ONE);
		}
		d = EEA(eLocal, phiN);
		
	}
	public BigInteger getNextPrime(BigInteger startVal){
		BigInteger a = startVal;
		while(!fermatTest(a, 1)){
			a = a.add(BigInteger.ONE);
		}
		return a;
	}
	
	private BigInteger getFermatBase(BigInteger n){
		while(true){
			final BigInteger a = new BigInteger(n.bitLength(), rand);
			if(BigInteger.ONE.compareTo(a) <= 0 && a.compareTo(n) < 0){
				return a;
			}
		}
	}
	
	public boolean fermatTest(BigInteger p, int certainty){
		if(p.equals(BigInteger.ONE)){
			return false;
		}
		
		
		
		for(int i = 0; i < certainty; i++){
			BigInteger a = getFermatBase(p);
			a = a.modPow(p.subtract(BigInteger.ONE),  p);
			if(!a.equals(BigInteger.ONE)){
				return false;
			}
			
		}
		return true;
	}
	

	public BigInteger EEA(BigInteger a, BigInteger b){
		final BigInteger mod = b;
		BigInteger x = BigInteger.ZERO;
		BigInteger y = BigInteger.ONE;
		BigInteger lastX = BigInteger.ONE;
		BigInteger lastY = BigInteger.ZERO;
		BigInteger temp;
		while(!b.equals(BigInteger.ZERO)){
			BigInteger q = a.divide(b);
			BigInteger r = a.remainder(b);
			a = b;
			b = r;
			temp = x;
			x = lastX.subtract(q.multiply(x));
			lastX = temp;
			temp = y;
			y = lastY.subtract(q.multiply(y));
			lastY = temp;
		}
		while(lastX.signum() < 0){
			lastX = lastX.add(mod);
		}
		return lastX;
	}
	
	public BigInteger GCD(BigInteger a, BigInteger b){
		if(a.compareTo(b) < 0){
			BigInteger temp = a;
			a = b;
			b = temp;
		}
		while(!b.equals(BigInteger.ZERO)){
			BigInteger temp = b;
			b = a.remainder(b);
			a = temp;
			
		}
		return a;
	}
}