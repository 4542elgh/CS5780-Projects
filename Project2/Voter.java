import java.net.Socket;

public class Voter {
    private Socket CLAClientSocket;
    private Socket CTFClientSocket;

//    public CLAClient(String host, int port) throws Exception {
//        // open a connection to the server
//        CLAClientSocket = new Socket(host, port);
//    }
//
//    public CTFClient(String host, int port) throws Exception {
//        // open a connection to the server
//        CTFClientSocket = new Socket(host, port);
//    }

    public Voter(String CLAHost, int CLAPort, String CTFHost, int CTFPort) throws Exception {
        CLAClientSocket = new Socket(CLAHost, CLAPort);
        CTFClientSocket = new Socket(CTFHost, CTFPort);
    }

    public String getVerificationNumber() throws Exception {

    }

    public boolean castVote() throws Exception {

    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("java Voter <CLA_host> <CLA_port> <CTF_host> <CTF_port>");
            System.exit(1);
        }

        String CLAHost = args[0];
        int CLAPort = Integer.parseInt(args[1]);

        String CTFHost = args[2];
        int CTFPort = Integer.parseInt(args[3]);

        new Socket(CLAHost, CLAPort).getVerificationNumber();
        new CTFClient(CTFHost, CTFPort).castVote();
    }
}