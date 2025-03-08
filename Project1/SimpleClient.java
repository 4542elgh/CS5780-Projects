import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;


public class SimpleClient {

   // network socket
   private Socket clientSocket;

   public SimpleClient(String host, int port) throws Exception {
      // open a connection to the server
      clientSocket = new Socket(host, port);
   }

   // data transfer
   public void execute() throws Exception {
      int c, k = 0, i = 0;

      String username = "mickey";
      String key = "xyz";
      // load the profile properties - contains user private key and server public key
      Properties profileProperties = new Properties();
      try (FileInputStream input = new FileInputStream(username + ".txt")) {
         profileProperties.load(input);
         // System.out.println(profileProperties.getProperty("private_key"));
      } catch (IOException e) {
         e.printStackTrace();
      }
      //String key = profileProperties.getProperty("k");

      // Get the public key from the server
      String[] serverPublicKey = profileProperties.getProperty("server.public_key").replaceAll("[{}]", "").split(",");
      RSA.KU serverKU = new RSA.KU(new BigInteger(serverPublicKey[0]), new BigInteger(serverPublicKey[1]));
      // Get the private key from the client
      String[] clientPrivateKey = profileProperties.getProperty("private_key").replaceAll("[{}]", "").split(",");
      RSA.KR clientKR = new RSA.KR(new BigInteger(clientPrivateKey[0]), new BigInteger(clientPrivateKey[1]));

      // Encrypt the username and add to encryptedHandshake
      ArrayList<BigInteger> encryptedHandshake = RSA.encryption(RSA.StringToBigIntegerList(username), serverKU);
      encryptedHandshake.add(BigInteger.valueOf(33));
      // Sign the encrypted company with the client private key
      encryptedHandshake.addAll(RSA.signing(RSA.StringToBigIntegerList(profileProperties.getProperty("company")), clientKR));
      encryptedHandshake.add(BigInteger.valueOf(33));
      // Encrypt the one time key with the server public key
      encryptedHandshake.addAll(RSA.encryption(RSA.StringToBigIntegerList(key), serverKU));
      encryptedHandshake.add(BigInteger.valueOf(33));
      // Send the encrypted handshake to the server
      for(int z = 0; z < encryptedHandshake.size(); z++) {
         if(encryptedHandshake.get(z).equals(BigInteger.valueOf(33))) {
            clientSocket.getOutputStream().write('!');
            continue;
         }
      // for(int z = 0; z < 1; z++) {
         clientSocket.getOutputStream().write((encryptedHandshake.get(z).toString() + '\n').getBytes());
      }
      // clientSocket.getOutputStream().flush();

      //Generate the packet but first get the values from the txt file
      int pattern = Integer.parseInt(profileProperties.getProperty("pattern"));
      int ndatabytes = Integer.parseInt(profileProperties.getProperty("ndatabytes"));
      int ncheckbytes = Integer.parseInt(profileProperties.getProperty("ncheckbytes"));
      int K = Integer.parseInt(profileProperties.getProperty("k"));
      // The method in the hash requires a random number to generate the key. Im confused on how
      // you would send the same random number to the server or if you would use K
      Random rand  = new Random();

      // read data from keyboard until end of file
      while ((c = System.in.read()) != -1) {

         // Convert the message into an array of binary numbers (data bytes)
         ArrayList<Integer> message = convertToArrayList(c);
         // Create the packet, the one time key, and encode the packet
         int[] packet = Hash.generatePacket(message, ndatabytes, ncheckbytes, pattern, K);
         int[] oneTimeKey = Hash.generateOneTimeKey(packet.length, rand);
         int[] encodedPacket = Hash.encodePacket(packet, oneTimeKey);
         // send it to server

         // Since the return types are int[], I use a for loop to go through each position
         // in the encodedPacket array and print out to the server that int. It will be a bunch of weird symbols.
         for( int x = 0; x < encodedPacket.length; x++) {
            clientSocket.getOutputStream().write(encodedPacket[x]);
         }

         clientSocket.getOutputStream().write(c);
         // if carriage return, flush stream
         if ((char) c == '\n' || (char) c == '\r')
         clientSocket.getOutputStream().flush();
         ++k;
       }
      clientSocket.getOutputStream().flush();

      while((c = clientSocket.getInputStream().read()) != -1) {
         System.out.write(c);
         if(++i == k) break;
      }
      System.out.println();
      System.out.println("wrote " + i + " bytes");
      clientSocket.close();
   }

   //Takes an into and returns an array list of its binary representation (Theres probably a better way to do this)
   public ArrayList<Integer> convertToArrayList(int message) {
      //Create a string with the message converted to a binary string
      String binaryString = Integer.toBinaryString(message);

      //Create the list that will be returned
      ArrayList<Integer> list = new ArrayList<>();

      // Turn the binary string into a char array
      char[] list1 = binaryString.toCharArray();

      //Loop through the char array
      for(char c: list1) {
         // Cast the char as an int and mod it by 8 to get the binary values
          list.add((int) c % 8);
      }
      return list;
   }

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


   public static void main(String[] argv) throws Exception {
      if (argv.length != 2) {
         System.out.println("java SimpleClient <host> <port>");
         System.exit(1);
      }

      String host = argv[0];
      int port = Integer.parseInt(argv[1]);

      new SimpleClient(host, port).execute();
   }
}
