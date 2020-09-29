import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Programm {
    public static List<LocalDate> feiertage = new ArrayList<>();
    public static void main(String[] args){

        Scanner reader = new Scanner(System.in);
        int monday=0,tuesday=0,wednesday=0,thursday=0,friday=0,saturday=0,sunday=0, years;
        LocalDate k = LocalDate.now();


        System.out.print("Wie viele Jahre sollen vorausgerechnet werden?: ");
        years = reader.nextInt();
        feiertageErzeugen(years);
        gewuenschtenFeiertage();

        for(int i = 0; i < feiertage.size(); i++){
            //System.out.println(i);



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
            feiertage.add(LocalDate.of(i,1,1));
            feiertage.add(LocalDate.of(i,6,1));
            feiertage.add(LocalDate.of(i,5,1));
            feiertage.add(LocalDate.of(i,10,26));
            feiertage.add(LocalDate.of(i,11,1));
            feiertage.add(LocalDate.of(i,12,8));
            feiertage.add(LocalDate.of(i,12,25));
            feiertage.add(LocalDate.of(i,12,26));

        }

        try (FileOutputStream fos = new FileOutputStream("feiertage.bin"); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(feiertage);
        } catch (IOException e) {
            System.out.println("Etwas ist schief gelaufen! Bitte wenden Sie sich an den Callcenter-Support!");
        }
        feiertage.clear();
    }

    public static void gewuenschtenFeiertage(){

        try (FileInputStream fis = new FileInputStream("feiertage.bin"); ObjectInputStream ois = new ObjectInputStream(fis)) {
            feiertage = (List<LocalDate>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Serialisierung hat nicht funktioniert!");
        }



    }
}
