public class Test {
    public static void main(String[] args) {

        StringBuilder string = new StringBuilder("Testing: this: string");
        String clientMsgToString = string.toString();
        String test = clientMsgToString.split(":")[0];

        System.out.println(test);

    }
}

