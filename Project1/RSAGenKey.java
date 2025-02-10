//        Your program, “RSAGenKey.java”, either takes the key size as  input or  take  input as p, q, and e.
//
//        If  only one argument k is given, the program randomly picks p and q in k bits, and generates a key pair.  For example:
//
//        c:\> java  RSAGenKey 12
//
//        Two files will be created.: pub_key.txt and pri_key.txt.  pub_key.txt contains a public key in the following format:
//
//        e = 8311
//        n = 31005883
//        pri_key.txt contains the corresponding private key in the following format:
//        d = 11296191
//        n = 31005883
//
//        If the program takes p, q and e as the input (java RSAGenKey p q e), it should generate the corresponding private key. The key pair should also be saved in two files as described above. For example
//
//        c:\> java RSA 6551 4733 8311
//
//        The same files pub_key.txt and pri_key.txt should be created.

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class RSAGenKey {
    private static FileOutputStream out = null;
    private static final Random rnd = new Random();
    private static BigInteger p = null;
    private static BigInteger q = null;
    private static BigInteger e = null;
    private static BigInteger d = null;
    private static BigInteger n = null;
    private static BigInteger phi = null;
    private static int k = 0;

    public static void main(String[] args) throws IOException{
        if (args.length == 1){
            k = Integer.parseInt(args[0]);
            p = BigInteger.probablePrime(k,rnd);
            q = BigInteger.probablePrime(k,rnd);
            n = p.multiply(q);
            phi = phiFN(Integer.parseInt(p.toString()),Integer.parseInt(q.toString())); //get the phi value
            e = getE(phi); //get e value based on phi
            d = e.modInverse(phi); // Inverse of e mod Phi(n)

            writeToFile();
        }
        else{
            System.out.println("Your Key size is not valid");
        }
    }

    public static BigInteger phiFN(int input1, int input2){
        if (isPrime(BigInteger.valueOf(input1))){
            return BigInteger.valueOf((long) (input1 - 1) * (input2 - 1));
        }
        else{
            return BigInteger.valueOf(-1);
        }
    }

    public static BigInteger getE(BigInteger input){
        ArrayList<BigInteger> gcd = new ArrayList<>();
        BigInteger index = BigInteger.valueOf(0);

        while (true){
            if (index.compareTo(BigInteger.valueOf(9999)) == 1){
                return e = gcd.get(rnd.nextInt(gcd.size()));
            }
            else{
                if (input.gcd(index).compareTo(BigInteger.valueOf(1))==0){
                    if (index.toString().length()==4) {
                        gcd.add(index);
                    }
                }
            }
            index = index.add(new BigInteger("1"));
        }
    }

    // A list of functions to check if a number is prime
    public static boolean isPrime(BigInteger number) {
        //check via BigInteger.isProbablePrime(certainty)
        if (!number.isProbablePrime(5))
            return false;

        //check if even, even is definitely not prime
        BigInteger two = new BigInteger("2");
        if (!two.equals(number) && BigInteger.ZERO.equals(number.mod(two)))
            return false;

        //find divisor if any from 3 to 'number'
        for (BigInteger i = new BigInteger("3"); i.multiply(i).compareTo(number) < 1; i = i.add(two)) { //start from 3, 5, etc. the odd number, and look for a divisor if any
            if (BigInteger.ZERO.equals(number.mod(i))) //check if 'i' is divisor of 'number'
                return false;
        }
        return true;
    }

    public static void writeToFile() throws IOException{
        try {
            if(e.gcd(phi).compareTo(BigInteger.valueOf(1))!=0){
                System.err.println("Error GCD is not equal to 1"); // Meaning they are not relative prime
            }

            out = new FileOutputStream("pub_key.txt");
            out.write(("e = " + e + "\nn = " + n).getBytes());

            out = new FileOutputStream("pri_key.txt");
            out.write(("d = " + d + "\nn = " + n).getBytes());
        }
        catch (FileNotFoundException ex){
            // pass
        }
        finally{
            if (out!=null) {
                out.close();
            }
        }
    }
}
