import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Voter {
    private Socket clientSocket;

    // Create new client socket instance
    public Voter(String host, int port) throws Exception {
        clientSocket = new Socket(host, port);
    }

    public void run() throws Exception {
        // Getting validation number
        OutputStream out = clientSocket.getOutputStream();
        InputStream in = clientSocket.getInputStream();

        out.write("mickey\n".getBytes());
        out.flush();

        StringBuilder validationNumber = new StringBuilder();

        try{
            int nextByte;
            while((nextByte = in.read()) != -1) {
                if (nextByte == '\n'){
                    break;
                }
                validationNumber.append((char)nextByte);
            }
            if (validationNumber.toString().equals("-1")){
                System.out.println("Invalid verification code, did you try to vote twice?");
            } else {
                System.out.println("Verification code: " + validationNumber);
            }
            clientSocket.close();
        } catch (Exception e){
            System.out.println("CLA server return error: " + e);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("java Voter <CLA_host> <CLA_port> <CTF_host> <CTF_port>");
            System.exit(1);
        }

        String CLAHost = args[0];
        int CLAPort = Integer.parseInt(args[1]);

//        String CTFHost = args[2];
//        int CTFPort = Integer.parseInt(args[3]);

        new Voter(CLAHost, CLAPort).run();
    }
}