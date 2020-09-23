import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Programm {
    public static void main(String[] args){

        List<LocalDate> feiertage = new ArrayList<>();
        int monday=0,tuesday=0,wednesday=0,thursday=0,friday=0,saturday=0,sunday=0;

        LocalDate k = LocalDate.now();
        System.out.println(k.plusDays(1));
        /*for (int i = 2020; i <= 2120; i++){
            feiertage.add(LocalDate.of(i,1,1));
            feiertage.add(LocalDate.of(i,6,1));
            feiertage.add(LocalDate.of(i,5,1));
            feiertage.add(LocalDate.of(i,10,26));
            feiertage.add(LocalDate.of(i,11,1));
            feiertage.add(LocalDate.of(i,12,8));
            feiertage.add(LocalDate.of(i,12,25));
            feiertage.add(LocalDate.of(i,12,26));

        }



        System.out.println(feiertage);
        System.out.println(LocalDate.now());

        try (FileOutputStream fos = new FileOutputStream("feiertage.bin"); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(feiertage);
        } catch (IOException e) {
            System.out.println("Etwas ist schief gelaufen! Bitte wenden Sie sich an den Callcenter-Support!");
        }
        */

        try (FileInputStream fis = new FileInputStream("feiertage.bin"); ObjectInputStream ois = new ObjectInputStream(fis)) {
            feiertage = (List<LocalDate>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Serialisierung hat nicht funktioniert!");
        }
        System.out.println(feiertage);
        for(LocalDate i = LocalDate.of(2020, 1, 1); i.isBefore(LocalDate.of(2020, 1, 2)); i.plusDays(1)){
            System.out.println(i);
            if (feiertage.contains(i)){

                switch (i.getDayOfWeek()){
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
        }

        System.out.println("Montag: " + monday);
        System.out.println("Dienstag: " + tuesday);
        System.out.println("Mittwoch: " + wednesday);
        System.out.println("Donnerstag: " + thursday);
        System.out.println("Freitag: " + friday);
        System.out.println("Samstag: " + saturday);
        System.out.println("Sonntag: " + sunday);

    }
}
