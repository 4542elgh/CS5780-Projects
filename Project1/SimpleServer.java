import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class SimpleServer implements Runnable {

   // server's socket
   private ServerSocket s;

   // server's port
   private int port;

   // server's private key
   private RSA.KR private_key;

   public SimpleServer(int p) throws Exception {
      // load server private key
      String[] keys = null;
      try (FileInputStream input = new FileInputStream("private_key.txt")) {
         keys = new String(input.readAllBytes()).replaceAll("[{}\\n]", "").split(",");
      } catch (IOException e) {
         System.out.println("Error reading private key file: " + e.getMessage());
         throw e;
      }
      private_key = new RSA.KR(new BigInteger(keys[0]), new BigInteger(keys[1]));

      // open server socket and start listening
      s = new ServerSocket(port = p);
   }

   public class RequestHandler implements Runnable {
      private Socket sock;

      private RequestHandler(java.net.Socket x) {
         sock = x;
      }

      public void run() {
         try {
            System.out.println("connect...");
            int c;

            // Extract 3 parts of the handshake, (username, company, key)
            ArrayList<ArrayList<BigInteger>> handshake = new ArrayList<ArrayList<BigInteger>>();
            String curr = "";
            while(handshake.size() < 3) {
               handshake.add(new ArrayList<BigInteger>());
               while((c = sock.getInputStream().read()) != '!') {
                  if (c == '\n') {
                     handshake.get(handshake.size() - 1).add(new BigInteger(curr));
                     curr = "";
                     continue;
                  }
                  curr += (char)c;
               }
            }

            // Decrypt the username
            String username = RSA.BigIntegerListToString(RSA.decryption(handshake.get(0), private_key));
            System.out.println("username: " + username);

            // Verify the company
            RSA.KU userKU;
            Properties profileProperties = new Properties();
            try (FileInputStream input = new FileInputStream("users.txt")) {
               profileProperties.load(input);
            } catch (IOException e) {
               System.out.println("Error reading profile file: " + e.getMessage());
               throw e;
            }
            // Get the public key for the user to decrypt the company
            String[] userPublicKey = profileProperties.getProperty(username + ".public_key").replaceAll("[{}]", "").split(",");
            userKU = new RSA.KU(new BigInteger(userPublicKey[0]), new BigInteger(userPublicKey[1]));
            String company = RSA.BigIntegerListToString(RSA.verifying(handshake.get(1), userKU));
            // If the company does not match the company in the profile, close the connection
            if (!company.equals(profileProperties.getProperty(username + ".company"))) {
               System.out.println("company mismatch");
               sock.close();
               return;
            }
            System.out.println("company: " + company);

            // Decrypt the key
            String key = RSA.BigIntegerListToString(RSA.decryption(handshake.get(2), private_key));
            System.out.println("key: " + key);

            // Send a response to the client
            sock.getOutputStream().write(("Hello " + username + " from " + company + ", I have received your key: " + key + "\n").getBytes());
            // flush output if no more data on input
            if (sock.getInputStream().available() == 0) {
               sock.getOutputStream().flush();
            }
            sock.getOutputStream().flush();
            sock.close();
            System.out.println("disconnect...");
         } catch (Exception e) {
            System.out.println("HANDLER: " + e);
         }
      } 
   }

   public void run() {
      while(true) {
         try {
            // accept a connection and run handler in a new thread
            new Thread(new RequestHandler(s.accept())).run();
         } catch(Exception e) {
            System.out.println("SERVER: " + e);
         }
      }
   } 

   // Method to transform lower case characters to upppercase characters and vice versa
   public static String serverModifyData(String data) {
      StringBuilder formattedData = new StringBuilder();

      for(char c: data.toCharArray()){
         if(Character.isUpperCase(c)){
            formattedData.append(Character.toLowerCase(c));
         } 
         else if(Character.isLowerCase(c)){
            formattedData.append(Character.toUpperCase(c));
         } 
         else {
            formattedData.append(c);
         }

      }

      return formattedData.toString();
   }


  public static void main(String[] argv) throws Exception {
     if (argv.length != 1) {
        System.out.println("java SimpleServer <port>");
        System.exit(1);
     }
     new SimpleServer(Integer.parseInt(argv[0])).run();
  }
   

}
