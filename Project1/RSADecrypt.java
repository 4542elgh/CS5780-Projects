import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class RSADecrypt {
    private static BigInteger d = null;
    private static BigInteger n = null;
    private static String cipherString = "";
    private static FileOutputStream out = null;
    private static String output="";

    public static void main(String[] args){
        String encryptedFilePath = args[0];
        String privateKeyPath = args[1];

        try {
            cipherString = readEncryptedFile(encryptedFilePath, "encryptedText");
            readPrivateKey(privateKeyPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String[] cipherArray = cipherString.split(" ");
        String[] plaintextArray = new String[cipherArray.length];

        for (int i = 0; i < cipherArray.length; i++) {
            plaintextArray[i]=(decipherFN(new BigInteger(cipherArray[i])));
        }

        for (int i = 0; i < plaintextArray.length; i++) {
            if (plaintextArray[i].length()==6){
                char a = (char) (Integer.parseInt(plaintextArray[i].substring(0,2))+97);
                char b = (char) (Integer.parseInt(plaintextArray[i].substring(2,4))+97);
                char c = (char) (Integer.parseInt(plaintextArray[i].substring(4,6))+97);

                if (a == '{'){
                    output += " ";
                }
                else{
                    output+=a;
                }
                if (b == '{'){
                    output += " ";
                }
                else{
                    output += b;
                }
                if (c == '{'){
                    output += " ";
                }
                else{
                    output += c;
                }
            }
            else if (plaintextArray[i].length()==5){
                char a = (char)  (Integer.parseInt(plaintextArray[i].substring(0,1))+97);
                char b = (char)  (Integer.parseInt(plaintextArray[i].substring(1,3))+97);
                char c = (char)  (Integer.parseInt(plaintextArray[i].substring(3,5))+97);

                if (a == '{'){
                    output += " ";
                }
                else{
                    output += a;
                }
                if (b == '{'){
                    output += " ";
                }
                else{
                    output += b;
                }
                if (c == '{'){
                    output += " ";
                }
                else{
                    output += c;
                }
            }
            else if (plaintextArray[i].length()==4){
                output += "a";
                char a = (char)  (Integer.parseInt(plaintextArray[i].substring(0,2))+97);
                char b = (char)  (Integer.parseInt(plaintextArray[i].substring(2,4))+97);

                if (a == '{'){
                    output += " ";
                }
                else{
                    output += a;
                }
                if (b == '{'){
                    output += " ";
                }
                else{
                    output += b;
                }
            }

            else if (plaintextArray[i].length()==3){
                output+="a";
                char a = (char)  (Integer.parseInt(plaintextArray[i].substring(0,1))+97);
                char b = (char)  (Integer.parseInt(plaintextArray[i].substring(1,3))+97);

                if (a == '{'){
                    output += " ";
                }
                else{
                    output += a;
                }
                if (b == '{'){
                    output += " ";
                }
                else{
                    output += b;
                }
            }

            else{
                System.out.print( " " +plaintextArray[i] + " ");
            }
        }

        try {
            writeToFile();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private static String readEncryptedFile(String path, String type) throws IOException {
        FileInputStream in = null;
        String output = "";
        try {
            in = new FileInputStream(path);
            int c;
            while ((c = in.read()) != -1) {
                if (type.equals("key") && c!=13){
                    output += (char)c;
                }
                else{
                    output += (char)c;
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return output;
    }

    private static void readPrivateKey(String privateKey) throws IOException{
        String output = readEncryptedFile(privateKey,"key");
        String[] temp  = output.split("\n");
        d = new BigInteger(temp[0].split("=")[1].trim());
        n = new BigInteger(temp[1].split("=")[1].trim());
    }

    // M = C^d mod n
    private static String decipherFN(BigInteger input){
        return input.modPow(d,n)+"";
    }

    private static void writeToFile() throws IOException{
        try {
            out = new FileOutputStream("test.dec");
            out.write(output.getBytes());
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
