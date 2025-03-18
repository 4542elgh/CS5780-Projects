// - CTF checks the validation number against a list of numbers received from the CLA.
    // - If the validation number is there, the CTF crosses it out (to prevent someone from voting twice).
// - The CTF adds the identification number to the list of people who voted for a particular candidate and adds one to the tally.
// - After the election ends, the CTF publishes the outcome.

import java.util.ArrayList;
public class CTF {
    public static class Candidate{
        public String name;
        public int count = 0;
        public ArrayList<String> voterIds = new ArrayList<>();
    }

    public static ArrayList<String> CLAValidationNumber = new ArrayList<>();
    public static ArrayList<Candidate> candidatesList = new ArrayList<>();

    public static void receiveValidationfromCLA(String validationNumber){
        CLAValidationNumber.add(validationNumber);
    }

    public static boolean processVote(String candidate, String validationNumber){
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
                    candidatesList.get(i).count++;
                    candidateFound = true;
                }
            }

            if (candidateFound){
                return true;
            } else {
                // Did not find candidate
                return false;
            }
        } else {
            // Did not find user validation number, its either not transferred from CLA, or the user is trying to cast vote twice!
            return false;
        }
    }
}