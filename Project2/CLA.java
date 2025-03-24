import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

// - Each voter will send a message to the CLA asking for a validation number, and CLA will return a random validation number to the user.
// - The CLA retains a list of validation numbers as well as a list of validation numbers' recipients to prevent a voter from voting twice.
    // - Evan: CLA does not purge this list
    // - Evan: Remember CLA does not know the voter voted for who, that is CTF job
// - Then, the CLA sends the same validation number to the CTF.
    // - Evan: Retain "recipient name" information to CLA itself.
    // - Evan: Remember CTF does not know who the voter is, just entry on "this validationNumber personnel" voted for this person
// - After a voter gets the validation number from CLA, the voter sends his/her vote and the validation number to CTF.

// Run with java .\CLA.java 1220
public class CLA implements Runnable {
    public static ArrayList<VoterValidation> voterList = new ArrayList<>();
    private ServerSocket serverSocket;

    public CLA(int p) throws Exception {
        serverSocket = new ServerSocket(p);
    }

    public class RequestHandler implements Runnable {
        private Socket socket;

        private RequestHandler(Socket x){
            socket = x;
        }

        public void run() {
            try{
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                int nextByte;
                StringBuilder voter = new StringBuilder();

                while((nextByte = in.read()) != -1) {
                    // Line feed will be indicator the message is finished
                    if (nextByte == '\n') {
                        break;
                    }
                    voter.append((char)nextByte);
                }

                int validationNumber = getValidationNumber(voter.toString());
                sendValidationToVoter(out, voter.toString(), validationNumber);
//                sendValidationToCTF(validationNumber);
            } catch (SocketException e) {
                System.out.println("Socket Exception: " + e);
            } catch (IOException e){
                System.out.println("IO Exception: " + e);
            } finally {
                System.out.println("Disconnecting...");
            }
        }
    }

    // Voter CLA model
    public static class VoterValidation {
        public String voterId;
        public int validationNumber;

        public VoterValidation(String voterId, int validationNumber){
            this.voterId = voterId;
            this.validationNumber = validationNumber;
        }
    }

    public static int getValidationNumber(String voterId){
        Random rand = new Random();
        int validationNumber;
        while (true){
            // Using Random here for simplicity, UUID will guarantee uniqueness without needing to check
            validationNumber = rand.nextInt(9999);
            boolean validationNumberCollided = false;
            // Check if voter already in list and check new random collided
            for(int i = 0; i < voterList.size(); i++){
                if (voterList.get(i).voterId.equals(voterId)){
                    return -1;
                }
                if (voterList.get(i).validationNumber == validationNumber){
                    validationNumberCollided = true;
                }
            }

            if (!validationNumberCollided){
                break;
            }
        }
        voterList.add(new VoterValidation(voterId, validationNumber));
        return validationNumber;
    }

    public static void sendValidationToCTF(int validationNumber){

    }

    public static void sendValidationToVoter(OutputStream out, String voter, int validationNumber){
        try{
            if (validationNumber == -1){
                System.out.println("Voter is already registered");
                out.write("-1\n".getBytes());
            } else {
                System.out.println("Return " + voter + " a validation number: " + validationNumber);
                out.write((validationNumber + "\n").getBytes());
            }
            // Make sure to flush your message immediately after finish writing
            out.flush();
        } catch (SocketException e) {
            System.out.println("Socket Exception: " + e);
        } catch (IOException e){
            System.out.println("IO Exception: " + e);
        } finally {
            System.out.println("Disconnecting...");
        }
    }

    public void run() {
        while(true) {
            try {
                // Accept a connection and run handler in a new thread
                new Thread(new RequestHandler(serverSocket.accept())).run();
            } catch(Exception e) {
                System.out.println("SERVER: " + e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("java CLA <port>");
            System.exit(1);
        }
        System.out.println("CLA socket listening on port: " + args[0]);
        // Create and start socket server connection, CLA.run() -> RequestHandler(port).run()
        new CLA(Integer.parseInt(args[0])).run();
    }
}

