import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

// Run with java .\Voter.java 127.0.0.1 1220 127.0.0.1 1000
// Where 127.0.0.1 1220 is your CLA host and port
// Where 127.0.0.1 1000 is your CTF host and port
public class Voter {
    private Socket clientSocket;

    // Create new client socket instance
    public Voter(String host, int port) throws Exception {
        clientSocket = new Socket(host, port);
    }

    public void getValidationNumber(String voterName, String password) throws Exception {
        // Getting validation number
        OutputStream out = clientSocket.getOutputStream();
        InputStream in = clientSocket.getInputStream();
        
        RSA.KU claKU = new RSA.KU(new BigInteger("2752699802175698850321457863889903443914071654066322070974944948003506590573712261860530370923018405259337417184753389933831681398543528970205492004681"), new BigInteger("4688740870912406875006445911106095067632194812574324514705835239373868219036801331852030657769767572075521450139675754090365663102562872514543748753030127981939700095555970656809128816220808822748574282202662756315734854454348063837568357951945429293674709798292836476841014231042515822865869861231333")); // Placeholder for public key
        ArrayList<BigInteger> encrypted_identity = RSA.encryption(RSA.StringToBigIntegerList(voterName + "!" + password), claKU);
        for (int i = 0; i < encrypted_identity.size(); i++){
            out.write(encrypted_identity.get(i).toString().getBytes());
            out.write('!');
        }
        // Send encrypted identity to CLA server
        out.write('\n');
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
            String voterPassword = "abc123"; // Placeholder for password
            new Voter(CLAHost, CLAPort).getValidationNumber(voterName, voterPassword);
        } else if (action.equals("castVote")){
            String validationNumber = args[4];
            String candidateName = args[6];
            new Voter(CTFHost, CTFPort).castVote(validationNumber, candidateName);
        }
    }
}