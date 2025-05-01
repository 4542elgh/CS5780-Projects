import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

// - Each voter will send a message to the CLA asking for a validation number, and CLA will return a random validation number to the user.
// - The CLA retains a list of validation numbers as well as a list of validation numbers' recipients to prevent a voter from voting twice.
// - Evan: CLA does not purge this list
// - Evan: Remember CLA does not know the voter voted for who, that is CTF job
// - Then, the CLA sends the same validation number to the CTF.
// - Evan: Retain "recipient name" information to CLA itself.
// - Evan: Remember CTF does not know who the voter is, just entry on "this validationNumber personnel" voted for this person
// - After a voter gets the validation number from CLA, the voter sends his/her vote and the validation number to CTF.

// Voter CLA model

// Run with java .\CLA.java 1220
public class CLA implements Runnable {
    public static class VoterValidation {
        public String voterId;
        public int validationNumber;

        public VoterValidation(String voterId, int validationNumber){
            this.voterId = voterId;
            this.validationNumber = validationNumber;
        }
    }

    public static ArrayList<VoterValidation> voterList = new ArrayList<>();
    private ServerSocket serverSocket;

    private String CTFHost;
    private int CTFPort;

    // CLA should be acting as a Server for Voter
    // CLA should be acting as a Client to send validation to CTF
    public CLA(int CLAPort, String CTFHost, int CTFPort) throws Exception {
        serverSocket = new ServerSocket(CLAPort);
        System.out.println("CLA socket listening on port: " + CLAPort);
        this.CTFHost = CTFHost;
        this.CTFPort = CTFPort;
    }

    public class RequestHandler implements Runnable {
        private Socket socket;
        private String CTFHost;
        private int CTFPort;

        private RequestHandler(Socket x, String CTFHost, int CTFPort){
            socket = x;
            this.CTFHost = CTFHost;
            this.CTFPort = CTFPort;
        }

        public void run() {
            try{
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Properties profileProperties = new Properties();
                try (FileInputStream input = new FileInputStream("users_cla.txt")) {
                    profileProperties.load(input);
                } catch (IOException e) {
                    System.out.println("Error reading profile file: " + e.getMessage());
                    throw e;
                }

                int nextByte;
                String voter = "", password = "";
                RSA.KR claKR = new RSA.KR(new BigInteger("2294623918866020884053396158761717371456600872053687952714581704730245302466597587647191046872571504872353524882987251273031113333967586716601221459041858136479784611059443308403092375173160156273326918202441309905181926490227303386321579033482919381568941967118127028467058261831999926010767704782657"), new BigInteger("4688740870912406875006445911106095067632194812574324514705835239373868219036801331852030657769767572075521450139675754090365663102562872514543748753030127981939700095555970656809128816220808822748574282202662756315734854454348063837568357951945429293674709798292836476841014231042515822865869861231333")); // Placeholder for private key
                String buffer = "";
                while((nextByte = in.read()) != -1) {
                    // Line feed will be indicator the message is finished
                    if (nextByte == '\n') {
                        String[] parts = RSA.decrypt(buffer, claKR).split("!");
                        voter = parts[0];
                        password = parts[1];
                        break;
                    }
                    buffer += (char)nextByte;
                }

                if (profileProperties.getProperty(voter + ".public_key") == null){
                    System.out.println("Voter not found");
                    out.write("-1\n".getBytes());
                    out.flush();
                    socket.close();
                    return;
                }

                if (!profileProperties.getProperty(voter + ".password").equals(password)) {
                    System.out.println("Voter password mismatch");
                    out.write("-1\n".getBytes());
                    out.flush();
                    socket.close();
                    return;
                }

                System.out.println("Voter: " + voter + " Password: " + password);
                int validationNumber = getValidationNumber(voter);
                RSA.KU voterKU = new RSA.KU(new BigInteger(profileProperties.getProperty(voter + ".public_key").split(",")[0]), new BigInteger(profileProperties.getProperty(voter + ".public_key").split(",")[1]));
                sendValidationToVoter(out, voter, voterKU, validationNumber);
                new CTFClient(this.CTFHost, this.CTFPort, validationNumber).run();
            } catch (SocketException e) {
//                System.out.println("Socket Exception: " + e);
            } catch (IOException e){
                System.out.println("IO Exception: " + e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

    public static void sendValidationToVoter(OutputStream out, String voter, RSA.KU voterKU, int validationNumber){
        try{
            if (validationNumber == -1){
                System.out.println("Voter is already registered");
                out.write("-1\n".getBytes());
            } else {
                System.out.println("Return " + voter + " a validation number: " + validationNumber);
                out.write(RSA.encrypt("" + validationNumber, voterKU).getBytes());
                out.write("\n".getBytes());
            }
            // Make sure to flush your message immediately after finish writing
            out.flush();
        } catch (SocketException e) {
            System.out.println("Socket Exception: " + e);
        } catch (IOException e){
            System.out.println("IO Exception: " + e);
        } catch (Exception e) {
            System.out.println("MISC EXCEPTION IN SENDING VALIDATION TO VOTER" + e);
        }
    }

    public class CTFClient {
        private Socket CTFClientSocket;
        private int validationNumber = -1;
    
        public CTFClient(String host, int port, int validationNumber) throws Exception {
            CTFClientSocket = new Socket(host, port);
            this.validationNumber = validationNumber;
        }
    
        public void run() throws Exception {
            // Register validation number to CTF
            OutputStream out = CTFClientSocket.getOutputStream();
            InputStream in = CTFClientSocket.getInputStream();
            
            // Problem 1: No encryption is used, but CTF expects encrypted data
            // Problem 2: The format sent doesn't match what CTF expects
            
            // Fix: Encrypt the validation number properly and format it correctly
            RSA.KU ctfKU = new RSA.KU(new BigInteger("2626397133379119473724051008683004030359875684611482215626489792880260882977831498475603342203258535446137757378503683648443659636668531934934174001743"), new BigInteger("6470971049575022323584066955179447090537310628982705159278108836959287258480825015769888337489211909496702625461027245224010775046279832544542060113946768350707643015188465108385343861931539063735908361865660658207991062229511162191872241529013803220408935866345741588540507792747255692360084684888001"));
            
            // Send encrypted validation number with proper format
            String message = "CLA:" + this.validationNumber;
            String encryptedMessage = RSA.encrypt(message, ctfKU);
            out.write((encryptedMessage + "\n").getBytes());
            out.flush();
    
            // Reading response from CTF server
            try {
                int nextByte;
                StringBuilder responseMsg = new StringBuilder();
                while((nextByte = in.read()) != -1) {
                    if (nextByte == '\n'){
                        break;
                    }
                    responseMsg.append((char)nextByte);
                }
                System.out.println("CTF Server response: " + responseMsg);
                CTFClientSocket.close();  // Close socket after receiving response
            } catch (Exception e){
                System.out.println("CTF server return error: " + e);
            }
        }
    }

    public void run() {
        while(true) {
            try {
                // Accept a connection and run handler in a new thread
                new Thread(new RequestHandler(serverSocket.accept(), this.CTFHost, this.CTFPort)).start();
            } catch(Exception e) {
                System.out.println("SERVER: " + e);
            }
        }
    }

    //java CLA.java 1220 127.0.0.1 1000
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("java CLA <port> <CTF_host> <CTF_Port>");
            System.exit(1);
        }
        // Create and start socket server connection, CLA.run() -> RequestHandler(port).run()
        int CLAPort = Integer.parseInt(args[0]);

        String CTFHost = args[1];
        int CTFPort = Integer.parseInt(args[2]);

        CLA cla = new CLA(CLAPort, CTFHost, CTFPort);
        cla.run();
        // System.out.println("CLA server started");
    }
}
