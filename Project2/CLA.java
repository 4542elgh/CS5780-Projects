import java.util.ArrayList;
import java.util.UUID;

// - Each voter will send a message to the CLA asking for a validation number, and CLA will return a random validation number to the user.
// - The CLA retains a list of validation numbers as well as a list of validation numbers' recipients to prevent a voter from voting twice.
    // - Evan: CLA does not purge this list
    // - Evan: Remember CLA does not know the voter voted for who, that is CTF job
// - Then, the CLA sends the same validation number to the CTF.
    // - Evan: Retain "recipient name" information to CLA itself.
    // - Evan: Remember CTF does not know who the voter is, just entry on "this validationNumber personnel" voted for this person
// - After a voter gets the validation number from CLA, the voter sends his/her vote and the validation number to CTF.

public class CLA {
    public static class Recipientvalidation {
        public String voterId;
        public String validationNumber;

        public Recipientvalidation(String voterId, String validationNumber){
            this.voterId = voterId;
            this.validationNumber = validationNumber;
        }
    }

    public static ArrayList<Recipientvalidation> recipientValidation = new ArrayList<>();

    public static String getValidationNumber(String voterId){
        // Trigger a test case where voterExist is true
        // recipientValidation.add(new Recipientvalidation("tester1", "test1123"));

        // Need to check if voter id is in existing list first
        for(int i = 0; i < recipientValidation.size(); i++){
            if (recipientValidation.get(i).voterId.equals(voterId)){
                return null;
            }
        }

        String validationNumber = UUID.randomUUID().toString();
        recipientValidation.add(new Recipientvalidation(voterId, validationNumber));
        sendValidationToCTF(validationNumber);
        return validationNumber;
    }

    public static boolean castVote(String voteFor, String validationNumber){
        return true;
    }

    public static void sendValidationToCTF(String validationNumber){

    }

    public static void main(String[] args){
        String validationNumber = getValidationNumber("tester1");
        if (validationNumber != null){
            // Cast a vote
            castVote("candidate1", validationNumber);
        }
    }
}

