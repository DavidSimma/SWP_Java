import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;

public class Programm {
    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        Random rnd = new Random();

        int searchedNumber, sizeNumbers;
        System.out.print("Welche Ganzahl soll gesucht werden?: ");
        searchedNumber = reader.nextInt();
        System.out.print("Wie viele Zahlen sollen durchsucht werden?: ");
        sizeNumbers = reader.nextInt();
        int[] numbers = new int[sizeNumbers];
        for (int i = 0; i < sizeNumbers; i++) {
            numbers[i] = rnd.nextInt(searchedNumber * 5);
        }
        Arrays.sort(numbers);
        /*for (int i = 0; i < sizeNumbers; i++) {
            System.out.println(numbers[i]);
        }

         */

        System.out.println("\n");
        final long timeStartNormal = System.currentTimeMillis();
        System.out.println(normalSearch(searchedNumber, numbers));
        System.out.println(System.currentTimeMillis()-timeStartNormal);
        System.out.println("\n");
        final long timeStartBinary = System.currentTimeMillis();
        System.out.println(binarySearch(searchedNumber, numbers));
        System.out.println(System.currentTimeMillis()-timeStartBinary);
    }

    public static int normalSearch(int searchedNumber, int[] numbers) {

        boolean found=false;
        for (int i = 0; i < numbers.length; i++) {
            if (searchedNumber == numbers[i]) {
                return i;
            }
        }
        return 0;
    }

    public static int binarySearch(int searchedNumber, int[] numbers) {
        int min = 0, max = numbers.length-1;
        boolean found=false;
        while (min <= max) {
            int half =min+((max-min)/2);
            if(numbers[half] > searchedNumber){
                max = half;
            }
            if(numbers[half] < searchedNumber){
                min = half;
            }
            if(numbers[half] == searchedNumber){
                return half;
            }
        }
        return 0;
    }

}