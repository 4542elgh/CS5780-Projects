// - CTF checks the validation number against a list of numbers received from the CLA.
// - If the validation number is there, the CTF crosses it out (to prevent someone from voting twice).
// - The CTF adds the identification number to the list of people who voted for a particular candidate and adds one to the tally.
// - After the election ends, the CTF publishes the outcome.

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
public class CTF implements Runnable {

    //For easier testing purposes
    // cd C:\Users\vidal\Documents\CS Classes\Spring 2025\Advanced Information Security CS 5780\CS5780-Projects\Project2
    public static class Candidate {
        public String name;
        public ArrayList<String> validationNumbers = new ArrayList<>();

        public Candidate(String name){
            this.name = name;
        }
    }

    //Keep a list of the validation numbers and the candidates that voter's can vote for
    public static ArrayList<String> CLAValidationNumber = new ArrayList<>();
    public static ArrayList<Candidate> candidatesList = new ArrayList<>();
    private ServerSocket CTFServer;


    //Create server port
    public CTF(int CTFPort ) throws Exception {
        CTFServer = new ServerSocket(CTFPort);
    }

    public class RequestHandler implements Runnable {
        private Socket socket;

        //Maybe we dont need this socket and only a server
        private RequestHandler(Socket x){
            socket = x;
        }

        public void run() {
            // One should be receiving validation Number from CLA
            // Two should be receiving vote cast by Voter
            try {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream(); // Changed from System.out to socket output
        
                //Create the CTF private key pair
                RSA.KR ctfKR = new RSA.KR(new BigInteger("3858296000178602930736151323138739821989174465259043852247191229342460945883302518867017347232674120024226042234326000662621235210891672651600375938715438026953497161800698837867361236438979837229169157590283848507321199036867500643964432474300091266600178273030316460536810111720385283305163506049007"), new BigInteger("6470971049575022323584066955179447090537310628982705159278108836959287258480825015769888337489211909496702625461027245224010775046279832544542060113946768350707643015188465108385343861931539063735908361865660658207991062229511162191872241529013803220408935866345741588540507792747255692360084684888001"));
        
                int nextByte;
                StringBuilder buffer = new StringBuilder();
                String clientMsg = "";
        
                while((nextByte = in.read()) != -1) {
                    // Line feed will be indicator the message is finished
                    if (nextByte == '\n') {
                        //Decrypt incoming messages with the CTF private keys
                        clientMsg = RSA.decrypt(buffer.toString(), ctfKR);
                        break;
                    }
                    buffer.append((char)nextByte);
                }
        
                // Parse the message to determine if it's from CLA or Voter
                //This is so the CTF knows if it recieves data from the CLA or a Voter
                if (clientMsg.startsWith("CLA:")) {
                    // Message from CLA with validation number
                    String validationNumber = clientMsg.substring(4); // Extract validation number after "CLA:"
                    
                    if (receiveValidationfromCLA(validationNumber) == -1) {
                        out.write(("Validation Number: " + validationNumber + " already exists. This should not happen!\n").getBytes());
                    } else {
                        out.write(("Validation Number: " + validationNumber + " add successful!\n").getBytes());
                    }
                    out.flush();
                } else {
                    // Message from voter with candidate and validation number
                    //Split the message with ! to break it up into parts
                    String[] parts = clientMsg.split("!");
                    //Check if the parts length is 2 or more, any less is invalid
                    if (parts.length >= 2) {
                        String candidateName = parts[0];
                        String validationNumber = parts[1];
                        
                        //Process the vote with the candidate name and validation number recieved
                        switch(processVote(candidateName, validationNumber)) {
                            case -1:
                                out.write(("Validation Number: " + validationNumber + " does not exist in CTF system!\n").getBytes());
                                break;
                            case 0:
                                out.write(("Validation Number: " + validationNumber + " is valid but cannot find voting candidate!\n").getBytes());
                                break;
                            case 1:
                                out.write(("Validation Number: " + validationNumber + " is valid and your vote has been recorded!\n").getBytes());
                                printCandidates();
                                break;
                        }
                        out.flush();
                    } else {
                        out.write("Invalid message format\n".getBytes());
                        out.flush();
                    }
                }
                
                socket.close();  // Make sure to close the socket when done
                
            } catch (SocketException e) {
                System.out.println("Socket Exception: " + e);
            } catch (IOException e) {
                System.out.println("IO Exception: " + e);
            } catch (Exception e) {
                System.out.println("General Exception: " + e);
            } finally {
                // System.out.println("Connection handled, disconnecting...");
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e);
                }
            }
        }
    }

    public static int receiveValidationfromCLA(String validationNumber){
        boolean foundValidation = false;
        //Loop through our validation number list
        for(int i = 0; i < CLAValidationNumber.size(); i++){
            System.out.println("RECEIVE VALIDATION FROM CLA\n\n\n");
            //Check to see if the recieved validation number is in the list
            if (CLAValidationNumber.get(i).equals(validationNumber)){
                foundValidation = true;
                break;
            }
        }

        //If found notify the CTF and stop the process
        if (foundValidation){
            System.out.println("CTF already contain validation number: " + validationNumber + ". This should not happen.");
            return -1;
            
        } else { // Add the validation number to the list
            CLAValidationNumber.add(validationNumber);
            System.out.println("Validation Number: " + validationNumber + " has been added to CTF system successfully.");
            printCLAValidationNumber();
            return 0;
        }
    }

    public static int processVote(String candidate, String validationNumber){
        boolean foundValidation = false;
        //Loop through the list and see if the validation number is already present
        //If found remove the validation number from the list
        for(int i = 0; i < CLAValidationNumber.size(); i++){
            if (CLAValidationNumber.get(i).equals(validationNumber)){
                foundValidation = true;
                CLAValidationNumber.remove(i);
                break;
            }
        }

        //Check if the candidate is found in the list
        if (foundValidation){
            boolean candidateFound = false;
            //Loop through the candidate list and see if the candidate in the vote request is present
            for(int i = 0; i < candidatesList.size(); i++){
                if(candidatesList.get(i).name.equals(candidate)){
                    candidatesList.get(i).validationNumbers.add(validationNumber);
                    candidateFound = true;
                }
            }

            //Found the candidate
            if (candidateFound){
                System.out.println("Validation number " + validationNumber + " casted to candidate successfully.");
                return 1;
            } else {
                // Did not find candidate
                System.out.println("Validation number " + validationNumber + " is valid but cannot find candidate!");
                return 0;
            }
        } else {
            // Did not find user validation number, its either not transferred from CLA, or the user is trying to cast vote twice!
            System.out.println("Validation number " + validationNumber + " is invalid. This could be because user voted or CLA did not transfer this validation number to CTF !");
            return -1;
        }
    }

    //Added this run method to run the server, from the Runnable inherited class
    public void run(){
        while(true) {
            try {
                //.run() wasnt at the end so it wasn't reciving anything
                new Thread(new RequestHandler(CTFServer.accept())).start();
            } catch(Exception e) {
                System.out.println("SERVER: " + e);
            }
        }
    }

    //* Completed get the arguments that you putin the command line 
    //java CTF.java  1000
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("java CTF <port>: Define a CTF Server Port");
            System.exit(1);
        }

        System.out.println("CTF socket listening on port: " + args[0]);

        int CTFport = Integer.parseInt(args[0]);

        candidatesList.add(new Candidate("John"));
        candidatesList.add(new Candidate("David"));
        candidatesList.add(new Candidate("Elena"));
        candidatesList.add(new Candidate("Stephanie"));
        printCandidates();

        // Create and start socket server connection, CTF.run() -> RequestHandler(port).run()
        new CTF(CTFport).run();
    }

    public static void printCandidates(){
        System.out.println("============================================================");
        for(int i = 0; i<candidatesList.size(); i++){
            System.out.println("Candidate: " + candidatesList.get(i).name + " votes: " + candidatesList.get(i).validationNumbers.size());
        }
        System.out.println("============================================================");
    }

    public static void printCLAValidationNumber(){
        System.out.println("============================================================");
        System.out.println("Validation Numbers:");
        for(int i = 0; i<CLAValidationNumber.size(); i++){
            System.out.println("- " + CLAValidationNumber.get(i));
        }
        System.out.println("============================================================");
    }

}

