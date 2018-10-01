

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

public class RSA {
    public BigInteger d;
    public BigInteger nLocal;
    public BigInteger nForeign;
    public BigInteger eLocal;
    public BigInteger eForeign;

    private SecureRandom rand = new SecureRandom();


    public RSA(int bits){

        BigInteger p = getNextPrime(new BigInteger(bits/2, rand));
        BigInteger q = getNextPrime(new BigInteger(bits/2, rand));
        eLocal = new BigInteger("65537");
        nLocal = p.multiply(q);
        BigInteger phiN = (p.subtract(BigInteger.ONE)).multiply((q.subtract(BigInteger.ONE)));
        while((GCD(eLocal, phiN).compareTo(BigInteger.ONE) > 0)){
            eLocal = eLocal.add(BigInteger.ONE);
        }
        d = EEA(eLocal, phiN);
    }

    private BigInteger getNextPrime(BigInteger start){
        BigInteger a = start;
        while(!fermatTest(a, 100)){
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

    private boolean fermatTest(BigInteger p, int certainty){
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

    private BigInteger EEA(BigInteger a, BigInteger b){
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

    private BigInteger GCD(BigInteger a, BigInteger b){
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


    private ArrayList<String> stringToBlock(String message, BigInteger n){
        int blocksize = (new String((n.toByteArray()))).length() - 1;
        ArrayList<String> messageList = new ArrayList<String>();

        if(message.length() <= blocksize){
            messageList.add(message);
            return messageList;
        }
        int count = blocksize;
        while(count < message.length()){
            messageList.add(message.substring(count - blocksize, count));
            count += blocksize;
        }
        count -= blocksize;
        messageList.add(message.substring(count));

        return messageList;
    }


    public synchronized ArrayList<String> encrypt(String s) {
        ArrayList<String> message = stringToBlock(s, nForeign);

        for(int i = 0; i < message.size(); i++){
            message.set(i, (new BigInteger(message.get(i).getBytes())).modPow(eForeign, nForeign).toString());
        }

        return message;


    }
    public synchronized String decrypt(ArrayList<String> message) {
        String finalMessage = "";

        for(int i = 0; i < message.size(); i++){

            finalMessage += new String((new BigInteger(message.get(i))).modPow(d, nLocal).toByteArray());
        }


        return finalMessage;
    }






}
