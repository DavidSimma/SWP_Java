import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import org.apache.commons.io.IOUtils;
import java.util.*;

public class Programm {
    public static HashMap<LocalDate, String> feiertage = new HashMap();
    public static List<String> nix = new ArrayList<>();
    public static void main(String[] args){

        Scanner reader = new Scanner(System.in);
        int monday=0,tuesday=0,wednesday=0,thursday=0,friday=0,saturday=0,sunday=0, anfangsJahr, endjahr;
        LocalDate k = LocalDate.now();


        System.out.print("Anfangsjahr: ");
        anfangsJahr = reader.nextInt();
        System.out.print("Endjahr: ");
        endjahr = reader.nextInt();

        feiertageErzeugen(anfangsJahr, endjahr);

        for(LocalDate ld : feiertage.keySet()){
            switch (ld.getDayOfWeek()){
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
                    default:
                        break;
                }

        }

        System.out.println("Montag: " + monday);
        System.out.println("Dienstag: " + tuesday);
        System.out.println("Mittwoch: " + wednesday);
        System.out.println("Donnerstag: " + thursday);
        System.out.println("Freitag: " + friday);

    }

    public static void feiertageErzeugen(int anfang, int ende){
        feiertageEinlesen();
        for (int i = anfang; i <= ende; i++){
            for(int o = 0; o<nix.size(); o++) {
                String[] temp = new String[3];
                LocalDate temp2;
                temp = nix.get(o).split(",");
                temp2 = LocalDate.of(i, Integer.parseInt(temp[1]), Integer.parseInt(temp[0]));
                feiertage.put(temp2, temp[2].toString());

            }
            feiertageEinlesen(i, "Ostermontag");
            feiertageEinlesen(i, "Christi Himmelfahrt");
            feiertageEinlesen(i, "Pfingstmontag");
            feiertageEinlesen(i, "Fronleichnam");


        }

    }
    public static JSONObject APIEinlesen(String url){
        JSONObject json = new JSONObject();



        try {
                json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
            }
        catch (MalformedURLException e){
            System.out.println("URL funktioniert nicht");
        }catch (IOException e){
            System.out.println("Feiertage konnten nicht eingelesen werden");
        }
        return json;
    }

    private static void feiertageEinlesen(int year, String name) {
        String urlBase = "https://feiertage-api.de/api/?jahr=";
        JSONObject json = APIEinlesen(urlBase + year);
        String date = json.getJSONObject("BY").getJSONObject(name).get("datum").toString();
        feiertage.put(LocalDate.parse(date), name);
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
