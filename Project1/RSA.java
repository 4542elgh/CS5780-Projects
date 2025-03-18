import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

// RUN: java -Dprime_size=500 .\RSA.java -gen "hello world"
public class RSA {
    private static final Random rnd = new Random(); // Prime seed
    //private static int k = 500; // This should be a user input, determine prime number length 2^k

    /**
     * Container class for RSA key components.
     */
    private static class RSAInt {
        BigInteger e;
        BigInteger d;
        BigInteger n;

        /**
         * Constructs an RSAInt with the given key components.
         * @param e Public exponent
         * @param d Private exponent
         * @param n Mod
         */
        public RSAInt(BigInteger e, BigInteger d, BigInteger n){
            this.e = e;
            this.d = d;
            this.n = n;
        }
    }

    public static class KU {
        BigInteger e;
        BigInteger n;

        /**
         * Constructs an public key with the given key components.
         * @param e Public exponent
         * @param n Mod
         */
        public KU(BigInteger e, BigInteger n){
            this.e = e;
            this.n = n;
        }
    }

    public static class KR {
        BigInteger d;
        BigInteger n;

        /**
         * Constructs an public key with the given key components.
         * @param d Private exponent
         * @param n Mod
         */
        public KR(BigInteger d, BigInteger n){
            this.d = d;
            this.n = n;
        }
    }

    /**
     * Main entry point for RSA operations.
     * @param args Command line arguments: -gen [user input payload string]
     */
    public static void main(String[] args) {

        if(args[0].equals("-help")) {
            System.out.println();
            System.out.println("Welcome to the help guide for RSA");
            System.out.println("To use the RSA algorithm, input the command as shown below:");
            System.out.println("-----------------------------------");
            System.out.println("java -Dprime_size=<inputSize> .\\RSA.java -gen <text>");
            System.out.println("-----------------------------------");
            System.out.println("This will generate private (KR) and public (KU) keys");
            System.out.println("based on the size you inputted and test them on <text>");
            System.out.println();
        }


        String payload = args[1]; // User input payload
        int k = Integer.parseInt(System.getProperty("prime_size"));

        ArrayList<BigInteger> payloadBigInt = new ArrayList<>(); // Convert user input string to ASCII int representation of bytes

        // Get string byte code then convert them to ascii
        for(int i = 0; i < payload.length(); i++){
            payloadBigInt.add(BigInteger.valueOf((int) payload.charAt(i)));
        }

        RSAInt RSA = generateRSA(k);
        KU publicKey = new KU(RSA.e, RSA.n);
        KR privateKey = new KR(RSA.d, RSA.n);

        // KR(KU(M))
        ArrayList<BigInteger> cipher = encryption(payloadBigInt, publicKey);
        ArrayList<BigInteger> decipher = decryption(cipher, privateKey);
        print(decipher, "encryption");

        // KU(KR(M))
        ArrayList<BigInteger> signed = signing(payloadBigInt, privateKey);
        ArrayList<BigInteger> verified = verifying(signed, publicKey);
        print(verified, "signing");
    }

    /**
     * Generates RSA key pairs using two large random prime numbers with BigInteger.
     * Output Private and Public key to console per ModelSolution
     * @param k Prime number's bit length, in form of 2^k
     * @return RSAInt object containing public key (e,n) and private key (d,n)
     */
    public static RSAInt generateRSA(int k){
        BigInteger p = BigInteger.probablePrime(k,rnd);
        BigInteger q = BigInteger.probablePrime(k,rnd);
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = relativePrime(k, phi); // e should be relative prime to Phi(n)
        BigInteger d = e.modInverse(phi); // Inverse of e mod Phi(n)

        System.out.println("KR={" + d + "," + n + "}");
        System.out.println("KU={" + e + "," + n + "}");

        return new RSAInt(e,d,n);
    }

    /**
     * Finds a number that is relative prime with phi. Try with gcd, and should only get 1
     * @param phi phi value of (p-1) * (q-1) aka. always even
     * @return A BigInteger that is relatively prime to phi
     */
    public static BigInteger relativePrime(int k ,BigInteger phi){
        while(true){
            BigInteger prime = BigInteger.probablePrime(k, rnd);
            if (prime.gcd(phi).equals(BigInteger.ONE)){
                return prime;
            }
        }
    }

    // Use public key to encrypt, and private key to decrypt
    /**
     * Encrypts a message using RSA public key.
     * @param payloadBigInt List of BigIntegers representing the message
     * @param publicKey Public key for encryption
     * @return List of encrypted BigIntegers
     */
    public static ArrayList<BigInteger> encryption(ArrayList<BigInteger> payloadBigInt, KU publicKey){
        ArrayList<BigInteger> cipher = new ArrayList<>();
        for(int i = 0; i < payloadBigInt.size(); i++){
            // C = M^e mod n // this is M.modPow(e, n)
            cipher.add(payloadBigInt.get(i).modPow(publicKey.e, publicKey.n));
        }
        return cipher;
    }

    /**
     * Decrypt a message using RSA private key.
     * @param cipher List of BigIntegers representing the message cipher
     * @param privateKey Private key for decryption
     * @return List of decrypted BigIntegers
     */
    public static ArrayList<BigInteger> decryption(ArrayList<BigInteger> cipher, KR privateKey){
        ArrayList<BigInteger> decipher = new ArrayList<>();
        for(int i = 0; i < cipher.size(); i++){
            // M = C^d mod n // this is C.modPow(d, n)
            decipher.add(cipher.get(i).modPow(privateKey.d, privateKey.n));
        }
        return decipher;
    }

    // Use private key to sign, and public key to validate
    /**
     * Signs a message using RSA private key.
     * @param payloadBigInt List of BigIntegers representing the message
     * @param privateKey Private key for signing
     * @return List of signed BigIntegers
     */
    public static ArrayList<BigInteger> signing(ArrayList<BigInteger> payloadBigInt, KR privateKey){
        ArrayList<BigInteger> signList = new ArrayList<>();
        for(int i = 0; i < payloadBigInt.size(); i++){
            // M = C^d mod n // this is C.modPow(d, n)
            signList.add(payloadBigInt.get(i).modPow(privateKey.d, privateKey.n));
        }
        return signList;
    }

    /**
     * Verifying a message using RSA public key.
     * @param signList List of BigIntegers representing the signed message
     * @param publicKey Public key for verifying
     * @return List of verified BigIntegers
     */
    public static ArrayList<BigInteger> verifying(ArrayList<BigInteger> signList, KU publicKey){
        ArrayList<BigInteger> verifyList = new ArrayList<>();
        for(int i = 0; i < signList.size(); i++){
            // C = M^e mod n // this is M.modPow(e, n)
            verifyList.add(signList.get(i).modPow(publicKey.e, publicKey.n));
        }
        return verifyList;
    }

    /**
     * Prints the result of encryption/decryption or signing/verification.
     * @param input List of BigIntegers to be converted to characters
     * @param type Operation type ("encryption" or "signing")
     */
    public static void print(ArrayList<BigInteger> input, String type){
        String output = "";
        for (int i = 0; i < input.size(); i++) {
            output += (char)input.get(i).intValue();
        }

        if (type.equals("encryption")){
            System.out.println("KR(KU(M))=" + output);
        } else {
            System.out.println("KU(KR(M))=" + output);
        }
    }

    /**
     * Converts a string to a list of BigIntegers representing the ASCII values of the characters.
     * @param payload
     * @return List of BigIntegers representing the ASCII values of the characters
     */
    public static ArrayList<BigInteger> StringToBigIntegerList(String payload) {
        ArrayList<BigInteger> payloadBigInt = new ArrayList<>(); // Convert user input string to ASCII int representation of bytes

        // Get string byte code then convert them to ascii
        for(int i = 0; i < payload.length(); i++){
            payloadBigInt.add(BigInteger.valueOf((int) payload.charAt(i)));
        }
        return payloadBigInt;
    }

    public static String BigIntegerListToString(ArrayList<BigInteger> payloadBigInt) {
        String output = "";
        for (int i = 0; i < payloadBigInt.size(); i++) {
            output += (char)payloadBigInt.get(i).intValue();
        }
        return output;
    }
}
