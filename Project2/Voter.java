import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// Run with java .\Voter.java 127.0.0.1 1220 127.0.0.1 1000
// Where 127.0.0.1 1220 is your CLA host and port
// Where 127.0.0.1 1000 is your CTF host and port
public class Voter {
    private Socket clientSocket;

    // Create new client socket instance
    public Voter(String host, int port) throws Exception {
        clientSocket = new Socket(host, port);
    }

    public void getValidationNumber(String voterName) throws Exception {
        // Getting validation number
        OutputStream out = clientSocket.getOutputStream();
        InputStream in = clientSocket.getInputStream();

        out.write((voterName + "\n").getBytes());
        out.flush();

        // Reading validation from CLA server
        try{
            int nextByte;
            StringBuilder responseMsg = new StringBuilder();
            while((nextByte = in.read()) != -1) {
                if (nextByte == '\n'){
                    break;
                }
                responseMsg.append((char)nextByte);
            }
            if (Integer.parseInt(responseMsg.toString()) == -1){
                System.out.println("Invalid verification code, did you try to vote twice?");
            } else {
                System.out.println("CLA issued verification number: " + responseMsg);
            }
            clientSocket.close();
        } catch (Exception e){
            System.out.println("CLA server return error: " + e);
        }
    }

    public void castVote(String validationNumber, String candidateName) throws Exception {
        // Getting validation number
        OutputStream out = clientSocket.getOutputStream();
        InputStream in = clientSocket.getInputStream();

        out.write((candidateName + ":" + validationNumber + "\n").getBytes());
        out.flush();

        // Reading validation from CTF server
        try{
            int nextByte;
            StringBuilder responseMsg = new StringBuilder();
            while((nextByte = in.read()) != -1) {
                if (nextByte == '\n'){
                    break;
                }
                responseMsg.append((char)nextByte);
            }
            System.out.println(responseMsg);
            clientSocket.close();
        } catch (Exception e){
            System.out.println("CLA server return error: " + e);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 5 && args.length != 6) {
            System.out.println("java Voter <CLA_host> <CLA_port> <CTF_host> <CTF_port> <voter_name|validationNumber> [getValidation|castVote] <candidate> ");
            System.exit(1);
        }

        String CLAHost = args[0];
        int CLAPort = Integer.parseInt(args[1]);

        String CTFHost = args[2];
        int CTFPort = Integer.parseInt(args[3]);

        String action = args[5];

        if (action.equals("getValidation")){
            String voterName = args[4];
            new Voter(CLAHost, CLAPort).getValidationNumber(voterName);
        } else if (action.equals("castVote")){
            String validationNumber = args[4];
            String candidateName = args[6];
            new Voter(CTFHost, CTFPort).castVote(validationNumber, candidateName);
        }
    }
}