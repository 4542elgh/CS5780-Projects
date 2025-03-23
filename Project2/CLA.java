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

public class CLA implements Runnable {
    public static ArrayList<Recipientvalidation> recipientValidation = new ArrayList<>();
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
                    if (nextByte == '\n') {
                        break;
                    }
                    voter.append((char)nextByte);
                }
//
//                System.out.println("Voter name is " + voter);
//
                int validationNumber = getValidationNumber(voter.toString());

                if (validationNumber == -1){
                    System.out.println("Voter is already registered");
                    out.write("-1\n".getBytes());
                } else {
                    System.out.println("Return " + voter + " a validation number: " + validationNumber);
                    out.write((validationNumber + "\n").getBytes());
                }
                out.flush();

            } catch (SocketException e) {
                System.out.println("Socket Exception: " + e);
            } catch (IOException e){
                System.out.println("IO Exception: " + e);
            } finally {
                System.out.println("Disconnecting...");
            }
        }
    }

    public static class Recipientvalidation {
        public String voterId;
        public int validationNumber;

        public Recipientvalidation(String voterId, int validationNumber){
            this.voterId = voterId;
            this.validationNumber = validationNumber;
        }
    }

    public static int getValidationNumber(String voterId){
        // Trigger a test case where voterExist is true
        // recipientValidation.add(new Recipientvalidation("tester1", "test1123"));

        // Need to check if voter id is in existing list first
        for(int i = 0; i < recipientValidation.size(); i++){
            if (recipientValidation.get(i).voterId.equals(voterId)){
                return -1;
            }
        }

        Random rand = new Random();
        int validationNumber = rand.nextInt(9999);
        recipientValidation.add(new Recipientvalidation(voterId, validationNumber));
        sendValidationToCTF(validationNumber);
        return validationNumber;
    }

    public static boolean castVote(String voteFor, int validationNumber){
        return true;
    }

    public static void sendValidationToCTF(int validationNumber){

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

    public static void main(String[] args) throws Exception {
//        int validationNumber = getValidationNumber("tester1");
//        if (validationNumber != -1){
//            // Cast a vote
//            castVote("candidate1", validationNumber);
//        }
        if (args.length != 1) {
            System.out.println("java CLA <port>");
            System.exit(1);
        }
        System.out.println("CLA socket listening on port: " + args[0]);
        // Create and start socket server connection
        new CLA(Integer.parseInt(args[0])).run();
    }
}

