import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class RSA {
    private static final Random rnd = new Random();
    private static BigInteger p = null;
    private static BigInteger q = null;
    private static BigInteger e = null;
    private static BigInteger d = null;
    private static BigInteger n = null;
    private static BigInteger phi = null;
    private static String payload = null;
    private static ArrayList<BigInteger> cipherList = new ArrayList<>();
    private static ArrayList<BigInteger> decipherList = new ArrayList<>();
    private static int k = 500; // This should be an user input

    public static void main(String[] args) throws IOException{
        if (args[0].equals("-gen")){
            payload = args[1];
        }
        generateRSA();
        encryption();
        decryption();
    }

    public static void generateRSA(){
        k = Integer.parseInt(System.getProperty("prime_size"));
        p = BigInteger.probablePrime(k,rnd);
        q = BigInteger.probablePrime(k,rnd);
        n = p.multiply(q);
        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = relativePrime(phi); // e should be relative prime to Phi(n)
        d = e.modInverse(phi); // Inverse of e mod Phi(n)

        System.out.println("KR={" + d + "," + n + "}");
        System.out.println("KU={" + e + "," + n + "}");
    }

    public static BigInteger relativePrime(BigInteger phi){
        while(true){
            BigInteger prime = BigInteger.probablePrime(k, rnd);
            if (prime.gcd(phi).equals(BigInteger.ONE)){
                return prime;
            }
        }
    }

    public static void encryption(){
        // Get string byte code then convert them to ascii
        ArrayList<BigInteger> MList = new ArrayList<>();
        for(int i = 0; i < payload.length(); i++){
            MList.add(BigInteger.valueOf((int) payload.charAt(i)));
        }

        for(int i = 0; i < MList.size(); i++){
            // C = M^e mod n // this is M.modPow(e, n)
            cipherList.add(MList.get(i).modPow(e,n));
        }

        for (int i = 0; i < cipherList.size(); i++) {
            System.out.println(cipherList.get(i));
        }
    }

    public static void decryption(){
        for(int i = 0; i < cipherList.size(); i++){
            // M = C^d mod n // this is C.modPow(d, n)
            decipherList.add(cipherList.get(i).modPow(d,n));
        }

        for (int i = 0; i < decipherList.size(); i++) {
            System.out.println(decipherList.get(i));
        }
    }
}
