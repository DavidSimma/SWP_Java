import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;

public class Programm extends Application{

    private static Scanner reader = new Scanner(System.in);
    public static Connection connection;
    public static String DBurl = "jdbc:mysql://localhost:3306/aktiendaten?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static List<Double> close = new ArrayList<>();
    private static List<LocalDate> date = new ArrayList<>();
    private static List<Double> gleitenderDurchschnitt = new ArrayList<>();
    private static String firma;
    private static List<String> firmen = txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\Aktien\\Aktienkürzel.txt");
    private static String key = txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\Aktien\\AktienDatenPW.txt").get(0);

    public static void main(String[] args) {
        System.out.println(firmen);
        launch(args);
    }

    public static JSONObject Jsoneinlesen(String url){
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
        }
        catch (MalformedURLException e){
            System.out.println("URL funktioniert nicht");
        }catch (IOException e){
            System.out.println("Aktien konnten nicht eingelesen werden");
        }
        return json;
    }

    public static void datenEinlesenUndSchreiben(String url, String firma){
        double temp1;
        JSONObject jsonObject = Jsoneinlesen(url);
        for (LocalDate i = LocalDate.now().minusDays(1); i.isAfter(LocalDate.now().minusDays(5000)); i=i.minusDays(1)){
            try{
                temp1 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("5. adjusted close");
                NumberFormat n = NumberFormat.getInstance();
                n.setMaximumFractionDigits(2);
                n.format(temp1);
                close.add(temp1);
                date.add(i);
            }
            catch (JSONException e){
            }
        }
    }


    public static List<String> txtEinlesen(String url){
        try {
            BufferedReader br = new BufferedReader(new FileReader(url));
            List<String> temp = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null){
                temp.add(line);
            }
            return temp;
        }catch (IOException e){
            System.out.println("Text konnten nicht eingelesen werden");
        }
        return null;
    }

    static void durchschnitt(int schnitt) {
        gleitenderDurchschnitt.clear();
        double tempWert=0, x,avg;
        for(int i = 0; i < close.size(); i++){

            for (int o = i-schnitt; o <= i; o++) {
                tempWert = 0;
                for(int p = 0; p < schnitt; p++){
                    if(o+p >= 0 && o+p <close.size()) {
                        tempWert += close.get(o+p);
                    }
                    else {
                        break;
                    }
                }

            }
            if(i > close.size()-200){
                avg = tempWert / (close.size() - i);
                gleitenderDurchschnitt.add(avg);
            }
            else {
                avg = tempWert / schnitt;
                gleitenderDurchschnitt.add(avg);
            }
        }

    }
    public static boolean connectToMySql(String firma){
        try {
            connection = DriverManager.getConnection(DBurl,"user",txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\MySQLPassword.txt").get(0));
            Statement myStmt = connection.createStatement();
            String tabelleErzeugen = "create table if not exists " + firma +"(datum DATE primary key, close DOUBLE, average DOUBLE);";
            myStmt.executeUpdate(tabelleErzeugen);
            System.out.println("Datenbank verknüpft");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static void writeDataInDB(LocalDate date, String firma, double close, double average){
        try {
            Statement myStmt = connection.createStatement();
            String writeData = "insert into "+ firma +"(datum, close, average) values(\'"+date+"\', "+close+", "+average+") " +
                    "on duplicate key update close = "+close+", average = "+average+";";
            myStmt.executeUpdate(writeData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getDataFromDB(String firma){
        clearLists();
        double temp;
        int temp2;
        try {
            Statement myStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String querry = "SELECT * from " + firma;
            ResultSet rs = myStmt.executeQuery(querry);
            while(rs.next()){
                date.add(LocalDate.parse(rs.getString("datum")));
                close.add(rs.getDouble("close"));
                gleitenderDurchschnitt.add(rs.getDouble("average"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void clearLists(){
        date.clear();
        close.clear();
        gleitenderDurchschnitt.clear();
    }

    @Override
    public void start(Stage primaryStage) {

        try {
            for(int count = 0; count < firmen.size(); count++) {
                clearLists();
                System.out.println(firmen.get(count));
                firma = firmen.get(count);

                String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + firma + "&outputsize=full&apikey=" + key;

                if(connectToMySql(firma)) {
                    datenEinlesenUndSchreiben(url, firma);
                    durchschnitt(200);
                    for (int i = 0; i < close.size(); i++) {
                        writeDataInDB(date.get(i), firma, close.get(i), gleitenderDurchschnitt.get(i));
                    }


                    getDataFromDB(firma);
                }


                final CategoryAxis xAxis = new CategoryAxis();
                final NumberAxis yAxis = new NumberAxis();
                xAxis.setLabel("Datum");
                yAxis.setLabel("close-Wert");
                final LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
                lineChart.setTitle("Aktienkurs - " + firma);

                XYChart.Series<String, Number> aktienDaten = new XYChart.Series();
                aktienDaten.setName("Close-Werte");
                for (int i = 0; i < close.size() - 1; i++) {
                    aktienDaten.getData().add(new XYChart.Data(date.get(i).toString(), close.get(i)));
                }

                XYChart.Series<String, Number> durchschnitt = new XYChart.Series();
                durchschnitt.setName("gleitender Durchschnitt");
                for (int i = 0; i < gleitenderDurchschnitt.size(); i++) {
                    durchschnitt.getData().add(new XYChart.Data(date.get(i).toString(), gleitenderDurchschnitt.get(i)));
                }

                Scene scene = new Scene(lineChart, 800, 600);
                lineChart.getData().add(aktienDaten);
                lineChart.getData().add(durchschnitt);
                aktienDaten.nodeProperty().get().setStyle("-fx-stroke: #000000; ");
                durchschnitt.nodeProperty().get().setStyle("-fx-stroke: #FFFFFF; ");

                if (close.get(aktienDaten.getData().size()) <= gleitenderDurchschnitt.get(aktienDaten.getData().size())) {
                    scene.getStylesheets().add("redChart.css");
                }
                if (close.get(aktienDaten.getData().size()) > gleitenderDurchschnitt.get(aktienDaten.getData().size())) {
                    scene.getStylesheets().add("greenChart.css");
                }

                yAxis.setAutoRanging(false);
                yAxis.setLowerBound(Collections.min(close) * 0.8);
                yAxis.setUpperBound(Collections.max(close) * 1.2);

                lineChart.setCreateSymbols(false);

                WritableImage image = scene.snapshot(null);
                File file = new File("C:\\Users\\simma\\Documents\\Schule\\SWP\\Aktien\\Aktiengrafiken\\" +
                        LocalDate.now().getYear() + "-" + LocalDate.now().getMonth() + "-" + LocalDate.now().getDayOfMonth() + "_" + firma + "_aktienkurs.png");
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);


                primaryStage.setScene(scene);
                primaryStage.close();




            }
            System.exit(0);



        } catch(Exception e) {
            e.printStackTrace();
        }


    }
}
