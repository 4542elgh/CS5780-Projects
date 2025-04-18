// - CTF checks the validation number against a list of numbers received from the CLA.
    // - If the validation number is there, the CTF crosses it out (to prevent someone from voting twice).
// - The CTF adds the identification number to the list of people who voted for a particular candidate and adds one to the tally.
// - After the election ends, the CTF publishes the outcome.

import java.util.ArrayList;
public class CTF {
    public static class Candidate {
        public String name;
        public ArrayList<String> validationNumbers = new ArrayList<>();

        public Candidate(name){
            this.name = name;
        }
    }

    public static ArrayList<String> CLAValidationNumber = new ArrayList<>();
    public static ArrayList<Candidate> candidatesList = new ArrayList<>();

    public CTF(int p) throws Exception {
        serverSocket = new ServerSocket(p)
    }

    public class RequestHandler implements Runnable {
        private Socket socket;

        private RequestHandler(Socket x){
            socket = x
        }

        public void run() {
            // One should be receiving validation Number from CLA
            // Two should be receiving vote casted by Voter
            try{
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                int nextByte;
                StringBuilder clientMsg = new StringBuilder();

                while((nextByte = in.read()) != -1) {
                    // Line feed will be indicator the message is finished
                    if (nextByte == '\n') {
                        break;
                    }
                    clientMsg.append((char)nextByte);
                }
                // We identify if the message is from CLA or from Voter
                int validationNumber = clientMsg.split(":")[1];
                if (clientMsg.substring(0,3).equals("CLA")){
                    if (receiveValidationfromCLA(validationNumber) == -1){
                        out.write(("Validation Number: " + validationNumber + " already exists. This should not happen!\n").getBytes());
                    } else {
                        out.write(("Validation Number: " + validationNumber + " add successful!\n").getBytes());
                    }
                    out.flush();
                } else {
                    // This is from voter, proceed to vote
                    switch(processVote(clientMsg.split(":")[0], clientMsg.split(":")[1])) {
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
                    candidatesList.get(i).validationNumbers.push(validationNumber)
                    candidateFound = true;
                }
            }

            if (candidateFound){
                System.out.println("Validation number " + validationNumber + " casted to candidate successfully.")
                return 1;
            } else {
                // Did not find candidate
                System.out.println("Validation number " + validationNumber + " is valid but cannot find candidate!")
                return 0;
            }
        } else {
            // Did not find user validation number, its either not transferred from CLA, or the user is trying to cast vote twice!
            System.out.println("Validation number " + validationNumber + " is invalid. This could be because user voted or CLA did not transfer this validation number to CTF !")
            return -1;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("java CTF <port>");
            System.exit(1);
        }

        candidatesList.add(new Candidate("John"))
        candidatesList.add(new Candidate("David"))
        candidatesList.add(new Candidate("Elena"))
        candidatesList.add(new Candidate("Stephanie"))
        printCandidates();

        System.out.println("CTF socket listening on port: " + args[0]);
        // Create and start socket server connection, CLA.run() -> RequestHandler(port).run()
        new CTF(Integer.parseInt(args[0])).run();
    }

    public static void printCandidates(){
        System.out.println("============================================================")
        for(int i = 0; i<candidatesList.size(); i++){
            System.out.println("Candidate: " + candidatesList.get[i].name + " votes: " + candidatesList.get[i].validationNumbers.size().toString());
        }
        System.out.println("============================================================")
    }

    public static void printCLAValidationNumber(){
        System.out.println("============================================================")
        System.out.println("Validation Numbers:");
        for(int i = 0; i<CLAValidationNumber.size(); i++){
            System.out.println("- " + CLAValidationNumber.get[i]);
        }
        System.out.println("============================================================")
    }
}