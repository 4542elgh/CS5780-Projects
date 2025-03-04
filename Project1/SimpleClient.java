import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;


public class SimpleClient {

   // network socket
   private Socket s;

   public SimpleClient(String host, int port) throws Exception {
      // open a connection to the server
      s = new Socket(host, port);
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
            s.getOutputStream().write('!');
            continue;
         }
      // for(int z = 0; z < 1; z++) {
         s.getOutputStream().write((encryptedHandshake.get(z).toString() + '\n').getBytes());
      }
      s.getOutputStream().flush();

      // read data from keyboard until end of file
      // while ((c = System.in.read()) != -1) {

      //    // send it to server
      //    s.getOutputStream().write(c);
      //    // if carriage return, flush stream
      //    if ((char) c == '\n' || (char) c == '\r')
      //       s.getOutputStream().flush();
      //    ++k;
      // }
      s.getOutputStream().flush();

      // read until end of file or same number of characters
      // read from server
      while ((c = s.getInputStream().read()) != -1) {
         System.out.write(c);
         if (++i == k)
            break;
      }
      System.out.println();
      System.out.println("wrote " + i + " bytes");
      s.close();
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
