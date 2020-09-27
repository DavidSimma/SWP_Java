import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

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
        for (int i = 0; i < sizeNumbers; i++){
            numbers[i] = rnd.nextInt(searchedNumber*5);
        }
        Arrays.sort(numbers);
    }
    public void normalSearch(int searchedNumber, int[] numbers){
        for (int i = 0; i <numbers.length; i++){
            if(searchedNumber == numbers[i]){
                System.out.println("Zahl enthalten!");
                break;
            }
        }
        System.out.println("Zahl nicht enthalten!");
    }
    public void binarySearch(int searchedNumber, int[] numbers){
            
        }
    }
}
