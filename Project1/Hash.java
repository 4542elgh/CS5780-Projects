import java.util.*;

// RUN: java .\Hash.java 13 2 131 7 hello
public class Hash {
    /**
     * @param args Command line:
     *             args[0] - ndatabytes: Number of data bytes
     *             args[1] - ncheckbytes: Number of checksum bytes
     *             args[2] - pattern: Bit pattern (should be < 256)
     *             args[3] - k: (must be odd)
     *             args[4] - Input string
     */
    public static void main(String[] args){
        Random rand = new Random();
        ArrayList<Integer> data_bytes = new ArrayList<>();
         
        int ndatabytes = -1;
        int ncheckbytes = -1;
        int pattern = -1;
        int k = -1;
        
        //ArrayList<Integer> data_bytes = new ArrayList<>(Arrays.asList(Integer.valueOf(101), Integer.valueOf(181)));
        //int ndatabytes = 3;
        //int ncheckbytes = 1;
        //int pattern = 123;
        //int k = 7;

        try{
            if(args[0] != null && args[1] != null && args[2] != null && args[3] != null && args[4] != null){
                ndatabytes = Integer.parseInt(args[0]);
                ncheckbytes = Integer.parseInt(args[1]);
                pattern = Integer.parseInt(args[2]);
                k = Integer.parseInt(args[3]);
                if (k % 2 != 1){
                    System.exit(1);
                }
                data_bytes.clear();
                //Use this to convert input message in Simple Client into data bytes
                for(int i = 0; i < args[4].length(); i++){
                    data_bytes.add((int)args[4].charAt(i));
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            System.out.println("Missing property");
        }

        int[] packet = generatePacket(data_bytes, ndatabytes, ncheckbytes, pattern, k);
        System.out.print("Packet: ");
        print(packet);

        int[] oneTimeKey = generateOneTimeKey(packet.length, rand);
        System.out.print("One Time Key: ");
        print(oneTimeKey);

        int[] checksum = generateChecksum(data_bytes, pattern, k, ncheckbytes);
        System.err.println("Checksum: " + checksum);

        int[] encodedPacket = encodePacket(packet, oneTimeKey);
        System.out.print("Encoded Packet: ");
        print(encodedPacket);

        int[] decodedPacket = decodePacket(encodedPacket, oneTimeKey);
        System.out.print("Decoded Packet: ");
        print(decodedPacket);
    }

    /**
     * Generates a packet containing the data bytes, padding, and checksum.
     *
     * @param data_bytes   List of input data
     * @param ndatabytes   Total number of data bytes
     * @param ncheckbytes  Number of checksum bytes
     * @param pattern      Bit pattern
     * @param k            Must be odd
     * @return            An integer array representing the complete packet
     */
    public static int[] generatePacket(ArrayList<Integer> data_bytes, int ndatabytes, int ncheckbytes, int pattern, int k){
        // System.out.println(data_bytes.size());
        // Use example data, get working operations first
        // This is the size of the packet
        int[] packet = new int[1 + ndatabytes + ncheckbytes];
        packet[0] = data_bytes.size();

        int padding = ndatabytes - data_bytes.size();
        // Padding data_bytes to match ndatabytes
        for(int i = 0; i < padding; i++){
            data_bytes.add(0);
        }
        int[] checksum = generateChecksum(data_bytes, pattern, k, ncheckbytes);
        for(int b: checksum) {
            // System.out.print(b + ", ");
            data_bytes.add(b);
        }
        // System.out.println();
        // System.out.println("DataBytes:" + data_bytes.size());
        for(int i = 0; i < data_bytes.size(); i++){
            // From position 2 onward will be data_bytes with trailing 0 padding if necessary
            // Follow by checksumbytes
            
            packet[1+i] = data_bytes.get(i);
        }
        // System.out.print("From HASH: Packet: ");
        // for(int i = 0; i < packet.length; i++) {
        //     System.out.print(packet[i] + " ");
        // }
        // System.out.println();
        return packet;
    }

    /**
     * Calculates checksum for data bytes.
     *
     * @param data_bytes   List of input data
     * @param pattern      Bit pattern
     * @param k            Must be odd
     * @param ncheckbytes  Number of checksum bytes
     * @return             Checksum value
     */
    public static int[] generateChecksum(ArrayList<Integer> data_bytes, int pattern, int k, int ncheckbytes){
        int checksum = 0;
        for(int i = 0; i < data_bytes.size(); i++){
            checksum += (data_bytes.get(i) & pattern);
        }
        checksum = checksum * k;
        checksum = checksum % (int) Math.pow(2, 8*ncheckbytes);
        int[] result = new int[ncheckbytes];
        for(int i = 0; i < ncheckbytes; i++){
            result[i] = (checksum >> (8 * i)) & 0xFF;
        }
        return result;
    }

    /**
     * Generates a one-time key for packet encryption.
     *
     * @param packetLength Length of packet
     * @param rand         Random number generator instance
     * @return             An integer array containing random values
     */
    public static int[] generateOneTimeKey(int packetLength, Random rand){
        int[] oneTimeKey = new int[packetLength];
        for(int i = 0; i < packetLength; i++){
            oneTimeKey[i] = rand.nextInt((int) Math.pow(2, 8));
        }
        return oneTimeKey;
    }

    /**
     * Encodes a packet using XOR operation with one time key.
     *
     * @param packet     The packet to encode
     * @param oneTimeKey One time key
     * @return           Encoded packet
     */
    public static int[] encodePacket(int[] packet, int[] oneTimeKey){
        int[] encoded = new int[packet.length];
        for(int i = 0; i < packet.length; i++){
            encoded[i] = packet[i] ^ oneTimeKey[i];
        }
        return encoded;
    }

    /**
     * Decodes an encoded packet using XOR operation with same one time key.
     *
     * @param encodedPacket The encoded packet to decode
     * @param oneTimeKey    The one time key
     * @return              Decoded packet
     */
    public static int[] decodePacket(int[] encodedPacket, int[] oneTimeKey){
        int[] decoded = new int[encodedPacket.length];
        for(int i = 0; i < encodedPacket.length; i++){
            decoded[i] = encodedPacket[i] ^ oneTimeKey[i];
        }
        return decoded;
    }

    /**
     * Prints the contents of the packet into binary numbers in the 8 bit long format
     *
     * @param result The integer array to print
     */
    public static void print(int[] result){
        for(int i = 0; i < result.length; i++) {
            //Add padding
            String temp = Integer.toBinaryString(result[i]);
            for(int x = 0; x < 8 - temp.length(); x++) {
                System.out.print("0");
            }
            System.out.print( Integer.toBinaryString(result[i]) + " ");
        }
        System.out.println();
    }
}
