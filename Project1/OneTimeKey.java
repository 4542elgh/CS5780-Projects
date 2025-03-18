import java.util.ArrayList;

// RUN: java .\OneTimeKey.java xyz 123abc
public class OneTimeKey {
    /**
     * @param args
     * args[0] - key: The one time key
     * args[1] - text: The text to be encoded
     * The text length must be perfectly divisible by the key length
     */
    public static void main(String[] args){
        // Key length must be a complete division of text length. So text.length % key.length = 0
        if (args[1].length() % args[0].length() != 0){
            System.out.println("The arguments are not compatible.");
            System.out.println("-----------------------------------");
            System.out.println("java .\\OneTimeKey.java <key> <text>");
            System.out.println("-----------------------------------");
            System.out.println("The text size must be divisible by the key size");
            System.exit(1);
        }

        ArrayList<Integer> keys = new ArrayList<>();
        ArrayList<Integer> textArr = new ArrayList<>();

        String key = args[0];
        String text = args[1];

        // Converting to ints
        for(int i = 0; i < key.length(); i++){
            keys.add((int)key.charAt(i));
        }

        for(int i = 0; i < text.length(); i++){
            textArr.add((int)text.charAt(i));
        }

        System.out.print("encoded to ");
        ArrayList<Integer> encoded = encode(keys, textArr);
        print(encoded);
        System.out.println();
        System.out.print("ddecoded to ");
        print(decode(keys, encoded));
    }

    /**
     * Encodes the input text using XOR with one time key.
     * Repeat key until matching text length
     *
     * @param key  ArrayList of Integer for one time key
     * @param text ArrayList of Integer for text
     * @return     ArrayList of Integer for encoded text
     */
    public static ArrayList<Integer> encode (ArrayList<Integer> key, ArrayList<Integer> text){
        ArrayList<Integer> encoded = new ArrayList<>();
        for (int i = 0; i < text.size(); i++){
            encoded.add(key.get(i%key.size()) ^ text.get(i));
        }
        return encoded;
    }

    /**
     * Decode the input text using XOR again with the same one time key.
     * Repeat key until matching text length
     *
     * @param key  ArrayList of Integer for one time key
     * @param encoded ArrayList of Integer for encoded text
     * @return     ArrayList of Integer for decoded text
     */
    public static ArrayList<Integer> decode (ArrayList<Integer> key, ArrayList<Integer> encoded){
        ArrayList<Integer> decoded = new ArrayList<>();
        for (int i = 0; i < encoded.size(); i++){
            decoded.add(key.get(i%key.size()) ^ encoded.get(i));
        }
        return decoded;
    }

    /**
     * Prints the encode or decoded text
     * Note: ASCII values outside the printable character range will render weird.
     *
     * @param text ArrayList of Integer for printing
     */
    public static void print(ArrayList<Integer> text){
        for (int i = 0; i < text.size(); i++){
            // Output for "java .\OneTimeKey.java xyz 123abc" is intended to miss letter d for decoded, since some char is small enough to fall out of ASCII alphabets, into special function category
            System.out.print((char)text.get(i).intValue());
        }
    }
}
