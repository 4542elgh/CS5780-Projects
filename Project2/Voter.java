import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

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
        //Open the voter file
        OutputStream out = clientSocket.getOutputStream();
        InputStream in = clientSocket.getInputStream();
        File voterFile = new File(voterName + ".txt");
        if (!voterFile.exists()){
            System.out.println("Voter file not found");
            return;
        }
        // Read the public key from the file
        String[] keys = null;
        try (InputStream input = new java.io.FileInputStream(voterFile)) {
            keys = new String(input.readAllBytes()).split(",");
        } catch (Exception e) {
            System.out.println("Error reading voter file: " + e.getMessage());
            throw e;
        }
        //Use our RSA functions to generate a private key pair for the voter and a public key pair for the CLA
        RSA.KR voterKR = new RSA.KR(new BigInteger(keys[0].trim()), new BigInteger(keys[1].trim()));
        RSA.KU claKU = new RSA.KU(new BigInteger("2752699802175698850321457863889903443914071654066322070974944948003506590573712261860530370923018405259337417184753389933831681398543528970205492004681"), new BigInteger("4688740870912406875006445911106095067632194812574324514705835239373868219036801331852030657769767572075521450139675754090365663102562872514543748753030127981939700095555970656809128816220808822748574282202662756315734854454348063837568357951945429293674709798292836476841014231042515822865869861231333")); // Placeholder for public key
        
        //Get the password from the voter file
        String password = keys[2].trim();
        ArrayList<BigInteger> encrypted_identity = RSA.encryption(RSA.StringToBigIntegerList(voterName + "!" + password), claKU);
        
        //Encrypt the user's identity
        for (int i = 0; i < encrypted_identity.size(); i++){
            out.write(encrypted_identity.get(i).toString().getBytes());
            out.write('!');
        }
        // Send encrypted identity to CLA server
        out.write('\n');
        out.flush();

        // Reading encrypted validation from CLA server
        try{
            int nextByte;
            StringBuilder responseMsg = new StringBuilder();
            while((nextByte = in.read()) != -1) {
                if (nextByte == '\n'){
                    break;
                }
                responseMsg.append((char)nextByte);
            }

            if (responseMsg.toString().length() == 2 && Integer.parseInt(responseMsg.toString()) == -1){
                System.out.println("Invalid verification code, did you try to vote twice?");
            } else {
                System.out.println("CLA issued verification number: " + RSA.decrypt(responseMsg.toString().substring(0, responseMsg.length()-1), voterKR));
            }
            clientSocket.close();
        } catch (Exception e){
            System.out.println("CLA server return error: " + e);
        }
    }

    public void castVote(String validationNumber, String candidateName) throws Exception {

        //Generate ctf public key pair
        RSA.KU ctfKU = new RSA.KU(new BigInteger("2626397133379119473724051008683004030359875684611482215626489792880260882977831498475603342203258535446137757378503683648443659636668531934934174001743"), new BigInteger("6470971049575022323584066955179447090537310628982705159278108836959287258480825015769888337489211909496702625461027245224010775046279832544542060113946768350707643015188465108385343861931539063735908361865660658207991062229511162191872241529013803220408935866345741588540507792747255692360084684888001"));
        ArrayList<BigInteger> encrypted_vote = RSA.encryption(RSA.StringToBigIntegerList(candidateName + "!" + validationNumber), ctfKU);

        // Getting validation number
        OutputStream out = clientSocket.getOutputStream();
        InputStream in = clientSocket.getInputStream();
        
        // Send encrypted vote to CTF server
        for (int i = 0; i < encrypted_vote.size(); i++){
            out.write(encrypted_vote.get(i).toString().getBytes());
            out.write('!');
        }
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
            System.out.println(responseMsg);
            clientSocket.close();
        } catch (Exception e){
            System.out.println("CLA server return error: " + e);
        }
    }

    //java Voter.java 127.0.0.1 1220 127.0.0.1 1000 alice getValidation
    //                CLA Host CLAPort CTFHost CTFPort
    // java Voter.java 127.0.0.1 1220 127.0.0.1 1000 validationNumber castVote John
    public static void main(String[] args) throws Exception {
    
        //Get the arguments from the voter
        String CLAHost = args[0];
        int CLAPort = Integer.parseInt(args[1]);

        String CTFHost = args[2];
        int CTFPort = Integer.parseInt(args[3]);

        String action = args[5];


        if (action.equals("getValidation")){
            File userFile = new File(args[4] + ".txt");
            if (!userFile.exists()){
                System.out.println("User file not found");
                return;
            }
            // Read the public key from the file
            String[] keys = null;
            try (InputStream input = new FileInputStream(userFile)) {
                keys = new String(input.readAllBytes()).split(",");
            } catch (Exception e) {
                System.out.println("Error reading user file: " + e.getMessage());
                throw e;
            }

            String voterName = args[4];
            new Voter(CLAHost, CLAPort).getValidationNumber(voterName);
        } else if (action.equals("castVote")){
            String validationNumber = args[4];
            String candidateName = args[6];
            new Voter(CTFHost, CTFPort).castVote(validationNumber, candidateName);
        }
    }
}