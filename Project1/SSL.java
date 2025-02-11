import java.util.*;

public class SSL {
    public static void main(String[] args){
        Random rand = new Random();
        int ndatabytes = 3;
        ArrayList<Integer> data_bytes = new ArrayList<>(Arrays.asList(Integer.valueOf(101), Integer.valueOf(181)));
        int ncheckbytes = 1;
        int pattern = 123;
        int k = 7;

        try{
            if(args[0] != null && args[1] != null && args[2] != null && args[3] != null && args[4] != null){
                ndatabytes = Integer.parseInt(args[0]);
                ncheckbytes = Integer.parseInt(args[1]);
                pattern = Integer.parseInt(args[2]);
                k = Integer.parseInt(args[3]);
                data_bytes.clear();
                for(int i = 0; i < args[4].length(); i++){
                    data_bytes.add((int)args[4].charAt(i));
                }
            }
        } catch (ArrayIndexOutOfBoundsException e){
//            e.printStackTrace();
        }

        int[] packet = generatePacket(data_bytes, ndatabytes, ncheckbytes, pattern, k);
        int[] oneTimeKey = generateOneTimeKey(packet.length, rand);
        int[] encodedPacket = encodePacket(packet, oneTimeKey);



//        for(int i = 0; i < encoded.length; i++){
//            System.out.println(encoded[i]);
//        }
    }

    public static int[] generatePacket(ArrayList<Integer> data_bytes, int ndatabytes, int ncheckbytes, int pattern, int k){
        // Use example data, get working operations first
        // This is the size of the packet
        int[] packet = new int[1 + ndatabytes + ncheckbytes];
        packet[0] = data_bytes.size();

        // Padding data_bytes to match ndatabytes
        for(int i = 0; i < ndatabytes - data_bytes.size(); i++){
            data_bytes.add(0);
        }

        data_bytes.add(generateChecksum(data_bytes, pattern, k, ncheckbytes));

        for(int i = 0; i < data_bytes.size(); i++){
            // From position 2 onward will be data_bytes with trailing 0 padding if necessary
            // Follow by checksum
            packet[1+i] = data_bytes.get(i);
        }

        return packet;
    }

//    public static ArrayList<Integer> checksum(ArrayList<Integer> data_bytes, int pattern){
    public static int generateChecksum(ArrayList<Integer> data_bytes, int pattern, int k, int ncheckbytes){
        int checksum = 0;
        for(int i = 0; i < data_bytes.size(); i++){
            checksum += (data_bytes.get(i) & pattern);
        }
        checksum = checksum * k;
        checksum = checksum % (int) Math.pow(2, 8*ncheckbytes);
        return checksum;
    }

    public static int[] generateOneTimeKey(int packetLength, Random rand){
        int[] oneTimeKey = new int[packetLength];
        for(int i = 0; i < packetLength; i++){
            oneTimeKey[i] = rand.nextInt((int) Math.pow(2, 8)) + 1; // A byte is 8 bit [1 to 256] including 1 and 256
        }
        return oneTimeKey;
    }

    public static int[] encodePacket(int[] packet, int[] oneTimeKey){
        int[] encoded = new int[packet.length];

        for(int i = 0; i < oneTimeKey.length; i++){
            encoded[i] = packet[i] ^ oneTimeKey[i];
        }

        return encoded;
    }
}
