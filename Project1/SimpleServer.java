import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class SimpleServer implements Runnable {

   // server's socket
   private ServerSocket serverSocket;

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
      private_key = new RSA.KR(new BigInteger(keys[0].trim()), new BigInteger(keys[1].trim()));

      // open server socket and start listening
      serverSocket = new ServerSocket(port = p);
   }

   public class RequestHandler implements Runnable {
      private Socket socket;

      private RequestHandler(java.net.Socket x) {
         socket = x;
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
               while((c = socket.getInputStream().read()) != '!') {
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
               System.out.println("company mismatch... closing connection...");
               socket.close();
               return;
            }
            System.out.println("company: " + company);

            // Decrypt the key
            String key = RSA.BigIntegerListToString(RSA.decryption(handshake.get(2), private_key));
            System.out.println("key: " + key);

            // Send a response to the client
            socket.getOutputStream().write(("Hello " + username + " from " + company + ", I have received your key: " + key + "\n").getBytes());


            // read the bytes from the socket
            // and convert the case
            while((c = socket.getInputStream().read()) != -1) {
               
               if(c >= 97 && c <= 122) {
                  c -= 32;
               } 
               else if (c >= 65 && c <=90) {
                  c += 32;
               }
               // write it back
               socket.getOutputStream().write(c);
               // flush output if no more data on input
               if (socket.getInputStream().available() == 0) {
               socket.getOutputStream().flush();
               }
            }


            this.socket.getOutputStream().flush();
            this.socket.close();
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
            new Thread(new RequestHandler(serverSocket.accept())).run();
         } catch(Exception e) {
            System.out.println("SERVER: " + e);
         }
      }
   }

  public static void main(String[] argv) throws Exception {
     if (argv.length != 1) {
        System.out.println("java SimpleServer <port>");
        System.exit(1);
     }
     new SimpleServer(Integer.parseInt(argv[0])).run();
  }
   

}
