import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Stuff to test stuff
public class Test {

    public static void main(String args[]) throws IOException {

        int x = 200;
        String binaryString = Integer.toBinaryString(x);
        
        System.out.println("Decimal: " + x);
        System.out.println("Binary: " + binaryString);


        ArrayList<Integer> list = new ArrayList<>();

        char[] list1 = binaryString.toCharArray();

        for(char c: list1) {
            list.add((int) c % 8);
        }
        System.out.println(list);


        System.in.read();

    }
    
}
