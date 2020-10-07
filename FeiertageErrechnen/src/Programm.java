import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import org.apache.commons.io.IOUtils;
import java.util.*;

public class Programm extends Application {
    public static HashMap<LocalDate, String> feiertage = new HashMap();
    public static List<String> nix = new ArrayList<>();
    public static int monday=0,tuesday=0,wednesday=0,thursday=0,friday=0, anfangsJahr, endjahr;;

    public static void main(String[] args){

        Scanner reader = new Scanner(System.in);

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

        launch(args);

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
    public static JSONObject Jsoneinlesen(String url){
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
        JSONObject json = Jsoneinlesen(urlBase + year);
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

    @Override
    public void start(Stage stage) throws Exception {
        String mo="Monday", tu="Tuesday", we="Wednessday", th="Thursday", fr="Frayday";

        final NumberAxis yAxis = new NumberAxis();
        final CategoryAxis xAxis = new CategoryAxis();

        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Free Weekdays from " + anfangsJahr + " until " + endjahr);
        xAxis.setLabel("Weekdays");
        yAxis.setLabel("Days");
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();

        series1.setName("Amout Days");
        series1.getData().add(new XYChart.Data<>(mo, monday));
        series1.getData().add(new XYChart.Data<>(tu, tuesday));
        series1.getData().add(new XYChart.Data<>(we, wednesday));
        series1.getData().add(new XYChart.Data<>(th, thursday));
        series1.getData().add(new XYChart.Data<>(fr, friday));

        VBox vbox = new VBox(barChart);

        Scene scene = new Scene(vbox, 640, 480);
        barChart.getData().add(series1);
        stage.setScene(scene);
        stage.show();
    }
}
