import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Programm {
    public static List<LocalDate> feiertage = new ArrayList<>();
    public static List<String> nix = new ArrayList<>();
    public static void main(String[] args){

        Scanner reader = new Scanner(System.in);
        int monday=0,tuesday=0,wednesday=0,thursday=0,friday=0,saturday=0,sunday=0, years;
        LocalDate k = LocalDate.now();


        System.out.print("Wie viele Jahre sollen vorausgerechnet werden?: ");
        years = reader.nextInt();

        feiertageEinlesen();
        feiertageErzeugen(years);

        for(int i = 0; i < feiertage.size(); i++){
            switch (feiertage.get(i).getDayOfWeek()){
                    case MONDAY:
                        monday+=1;
                        break;
                    case TUESDAY:
                        tuesday+=1;
                        break;
                    case WEDNESDAY:
                        wednesday+=1;
                        break;
                    case THURSDAY:
                        thursday+=1;
                        break;
                    case FRIDAY:
                        friday+=1;
                        break;
                    case SATURDAY:
                        saturday+=1;
                        break;
                    case SUNDAY:
                        sunday+=1;
                        break;
                    default:
                        break;
                }

        }

        System.out.println("Montag: " + monday);
        System.out.println("Dienstag: " + tuesday);
        System.out.println("Mittwoch: " + wednesday);
        System.out.println("Donnerstag: " + thursday);
        System.out.println("Freitag: " + friday);
        System.out.println("Samstag: " + saturday);
        System.out.println("Sonntag: " + sunday);

    }

    public static void feiertageErzeugen(int years){

        for (int i = 2020; i <= 2020+years; i++){
            for(int o = 0; o<nix.size(); o++) {
                String[] temp = new String[2];
                temp = nix.get(o).split(",");
                feiertage.add(LocalDate.of(i, Integer.parseInt(temp[1]), Integer.parseInt(temp[0])));
            }


        }
    }
    public static void feiertageEinlesen(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("feiertage.txt"));
            String temp = br.readLine();
            while(temp != null){
                nix.add(temp);
                temp = br.readLine();

            }
        }catch (IOException e){
            System.out.println("Feiertage konnten nicht eingelesen werden");
        }
    }
}
