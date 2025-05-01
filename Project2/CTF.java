// - CTF checks the validation number against a list of numbers received from the CLA.
// - If the validation number is there, the CTF crosses it out (to prevent someone from voting twice).
// - The CTF adds the identification number to the list of people who voted for a particular candidate and adds one to the tally.
// - After the election ends, the CTF publishes the outcome.

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static ArrayList<String> CLAValidationNumber = new ArrayList<>();
    public static ArrayList<Candidate> candidatesList = new ArrayList<>();
    private ServerSocket CTFServer;


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
            // Two should be receiving vote casted by Voter
            try{
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                //TODO decrypt the information coming from CLA and the Voter with CTF pirvate key

                int nextByte;
                StringBuilder clientMsg = new StringBuilder();

                while((nextByte = in.read()) != -1) {
                    // Line feed will be indicator the message is finished
                    if (nextByte == '\n') {
                        break;
                    }
                    clientMsg.append((char)nextByte);
                }
                
                //Turn the message recieved into a String
                String clientMsgToString = clientMsg.toString();

                //Create variables for the the candidate name and validation number
                //The first index "[0]" should be the candidate name while the next index is the validation
                String candidateName = clientMsgToString.split(":")[0];
                String validationNumber = clientMsgToString.split(":")[1];


                if (clientMsg.substring(0,3).equals("CLA")){
                    if (receiveValidationfromCLA(validationNumber) == -1){
                        out.write(("Validation Number: " + validationNumber + " already exists. This should not happen!\n").getBytes());
                    } else {
                        out.write(("Validation Number: " + validationNumber + " add successful!\n").getBytes());
                    }
                    out.flush();
                } else {
                    // This is from voter, proceed to vote
                    switch(processVote(candidateName, validationNumber)) {
                        case -1:
                            out.write(("Validation Number: " + validationNumber + " does not exist in CTF system!\n").getBytes());
                            break;
                        case 0:
                            out.write(("Validation Number: " + validationNumber + " is valid but cannot voting candidate!\n").getBytes());
                            break;
                        case 1:
                            out.write(("Validation Number: " + validationNumber + " is valid and your vote has been recorded!\n").getBytes());
                            printCandidates();
                            break;
                    }
                    out.flush();
                }
            } catch (SocketException e) {
                System.out.println("Socket Exception: " + e);
            } catch (IOException e){
                System.out.println("IO Exception: " + e);
            } finally {
                System.out.println("Disconnecting...");
            }
        }
    }

    public static int receiveValidationfromCLA(String validationNumber){
        boolean foundValidation = false;
        for(int i = 0; i < CLAValidationNumber.size(); i++){
            if (CLAValidationNumber.get(i).equals(validationNumber)){
                foundValidation = true;
                break;
            }
        }

        if (foundValidation){
            System.out.println("CTF already contain validation number: " + validationNumber + ". This should not happen.");
            return -1;
        } else {
            CLAValidationNumber.add(validationNumber);
            System.out.println("Validation Number: " + validationNumber + " has been added to CTF system successfully.");
            printCLAValidationNumber();
            return 0;
        }
    }

    public static int processVote(String candidate, String validationNumber){
        boolean foundValidation = false;
        for(int i = 0; i < CLAValidationNumber.size(); i++){
            if (CLAValidationNumber.get(i).equals(validationNumber)){
                foundValidation = true;
                CLAValidationNumber.remove(i);
                break;
            }
        }

        if (foundValidation){
            boolean candidateFound = false;
            for(int i = 0; i < candidatesList.size(); i++){
                if(candidatesList.get(i).name.equals(candidate)){
                    candidatesList.get(i).validationNumbers.add(validationNumber);
                    candidateFound = true;
                }
            }

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
                new Thread(new RequestHandler(CTFServer.accept())).run();
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

