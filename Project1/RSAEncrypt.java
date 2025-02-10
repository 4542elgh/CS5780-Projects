import java.io.*;
import java.math.BigInteger;

public class RSAEncrypt {
    public static BigInteger e = null;
    public static BigInteger n = null;
    private static FileOutputStream out = null;

    public static void main(String args[]){
        String plaintext = args[0]; //this is for terminal execution
    //       String plaintext = "src/test.txt"; //this is for IDE execution
        String publicKey = args[1];
    //       String publicKey = "src/pub_key.txt"; //this is for IDE execution
        String fileOutput="";

        try {
            fileOutput = readPlaintextFile(plaintext,"plaintext");
            readPubKey(publicKey);
        } catch (IOException e){
            e.printStackTrace();
        }

        String[] threeBlockArray =  readThreeByte(fileOutput);
        BigInteger[] outputingArray = new BigInteger[threeBlockArray.length];

        for (int i = 0; i < threeBlockArray.length; i++) {
            String integer = convertByteToInt(threeBlockArray[i]);
            outputingArray[i]= singleBlockEncryption(new BigInteger(integer),e,n);
        }

        try {
            writeToFile(outputingArray);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static String readPlaintextFile(String plaintextPath, String type) throws IOException{
        FileInputStream in = null;
        String output = "";
        try {
            in = new FileInputStream(plaintextPath);
            int c;
            while ((c = in.read()) != -1) {
                if (type.equals("key")){
                    if (c!=13){
                        output+=(char)c;
                    }
                }
                else{
                    if (c!=13 && c!=10 && c!=44 && c!=46){
                        output+=(char)c;
                    }
                }
            }
        }finally {
            if (in != null) {
                in.close();
            }
        }
        return output.toLowerCase();
    }

    private static void readPubKey(String pubKeyPath) throws IOException{
        String output = readPlaintextFile(pubKeyPath,"key");
        String[] temp  = output.split("\n");
        e = new BigInteger(temp[0].split("=")[1].trim());
        n = new BigInteger(temp[1].split("=")[1].trim());
    }

    private static BigInteger singleBlockEncryption(BigInteger plaintext, BigInteger e, BigInteger n){
        return plaintext.modPow(e,n);
    }

   private static String[] readThreeByte(String plaintext){ //this will return the entire string into array of 3 character each element
       //this will determine if length/3 will have remainder (which we need to add 1 to endindex, other wise we will miss some character)
        int endIndex;
        if ((plaintext.length()%3)==0){
            endIndex=plaintext.length()/3;
        }
        else{
            endIndex=(plaintext.length()/3)+1;
        }

       //set the new temporary array to that endIndex size
        String[] temp = new String[endIndex];

        for (int i = 0; i < endIndex; i++) {
            //if this index * 3 + 3 is greater than length,
                //that means this is the last index (and mod is not 0), (if mod is 0, endIndex wont have this extra 1 index) see above
                //possible less than 3,
                //so we should handle if the substring is less than 3 index away from current index
                //so we dont get null index
            //this also prevent overflowing when +3
                //because if you use index * 3 (without the +3) and this return false,
                //while the else statement substring ends in index+2 you will get an error
            if ((3*i+3)>plaintext.length()){
                int remainingIndex = plaintext.length()%3;
                temp[i] = plaintext.substring(3*i,3*i+remainingIndex);
            }
            else{
                temp[i] = plaintext.substring(3*i,3*i+3);
            }
        }

        return temp;
    }

    private static String convertByteToInt(String input){
        String temp = "";
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i)>=97 && input.charAt(i)<=122){
                if (input.charAt(i)-97<10){
                    temp+="0"+(input.charAt(i)-97);
                }
                else{
                    temp+=(input.charAt(i)-97)+"";
                }
            }
            else if (input.charAt(i)==32){
                temp+=(26+"");
            }
            else{
                System.err.println("Error converting");
            }
        }
        return temp;
    }

    private static void printArray(String[] input){
        for (int i = 0; i < input.length; i++) {
            System.out.println(i);
            System.out.println(input[i]);
        }
    }


    public static void writeToFile(BigInteger[] input) throws IOException{
        try {
            out = new FileOutputStream("test.enc");
            for (int i = 0; i <input.length; i++) {
                out.write((input[i].intValue()+" ").getBytes());
            }
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        finally{
            if (out!=null) {
                out.close();
            }
        }
    }
}
