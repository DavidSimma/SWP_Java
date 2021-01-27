import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Programm extends Application{

    public static Scanner reader = new Scanner(System.in);
    public static Connection connection;
    public static String DBurl = "jdbc:mysql://localhost:3306/aktiendaten?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    public static List<Double> close = new ArrayList<>();
    public static List<Double> open = new ArrayList<>();
    public static List<Double> high = new ArrayList<>();
    public static List<Double> low = new ArrayList<>();
    public static List<LocalDate> date = new ArrayList<>();
    public static List<Double> gleitenderDurchschnitt = new ArrayList<>();


    public static void main(String[] args) {
        String key = txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\AktienDatenPW.txt");
        System.out.print("Firmenabkürzung angeben: ");
        String firma = reader.next().toUpperCase();

        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + firma+ "&outputsize=full&apikey=" + key;

        connectToMySql(firma);
        datenEinlesenUndSchreiben(url, firma);
        getDataFromDB(firma);
        durchschnitt();

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
        double temp1, temp2, temp3, temp4;
        JSONObject jsonObject = Jsoneinlesen(url);
        for (LocalDate i = LocalDate.now().minusDays(1); i.isAfter(LocalDate.now().minusDays(500)); i=i.minusDays(1)){
            try{
                temp1 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("4. close");
                close.add(temp1);
                temp2 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("1. open");
                open.add(temp2);
                temp3 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("2. high");
                high.add(temp3);
                temp4 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("3. low");
                low.add(temp4);
                low.add(temp4);
                date.add(i);
                writeDataInDB(i, firma, temp1, temp2, temp3, temp4);
            }
            catch (JSONException e){
            }
        }
    }

    public static String txtEinlesen(String url){
        try {
            BufferedReader br = new BufferedReader(new FileReader(url));
            return br.readLine();
        }catch (IOException e){
            System.out.println("Text konnten nicht eingelesen werden");
        }
        return "";
    }

    static void durchschnitt() {
        int count = 0;
        double wert = 0, x,avg;
        for(int i = 0; i <= close.size()-1; i++){
            count++;
            if(count <= 200){
                wert = wert + close.get(i);
                avg = wert/count;
                gleitenderDurchschnitt.add(avg);
            }
            if(count > 200) {
                x = close.get(i-200);
                wert = wert - x;
                wert = wert + close.get(i);
                avg = wert/200;
                gleitenderDurchschnitt.add(avg);
            }
        }
    }

    public static boolean connectToMySql(String firma){
        try {
            connection = DriverManager.getConnection(DBurl,"user",txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\MySQLPassword.txt"));
            Statement myStmt = connection.createStatement();
            String tabelleErzeugen = "create table if not exists " + firma +"(datum DATE primary key, open DOUBLE, close DOUBLE, high DOUBLE, low DOUBLE);";
            myStmt.executeUpdate(tabelleErzeugen);
            System.out.println("Datenbank verknüpft");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static void writeDataInDB(LocalDate date, String firma, double open, double close, double high, double low){
        try {
            Statement myStmt = connection.createStatement();
            String writeData = "insert ignore into "+ firma +"(datum, open, close, high, low) values(\'"+date+"\', "+open+","+close+","+high+","+low+")";
            myStmt.executeUpdate(writeData);
            System.out.println("Datensatz eingetragen");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getDataFromDB(String firma){
        clearLists();
        try {
            Statement myStmt = connection.createStatement();
            String querry = "SELECT * from " + firma;
            ResultSet rs = myStmt.executeQuery(querry);
            while (rs.next()){
                date.add(LocalDate.parse(rs.getString("datum")));
                open.add(rs.getDouble("open"));
                close.add(rs.getDouble("close"));
                high.add(rs.getDouble("high"));
                low.add(rs.getDouble("low"));
                }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void clearLists(){
        date.clear();
        open.clear();
        close.clear();
        high.clear();
        low.clear();
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Datum");
            yAxis.setLabel("close-Wert");
            final LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis);
            lineChart.setTitle("Aktienkurs");

            XYChart.Series<String, Number> aktienDaten = new XYChart.Series();
            aktienDaten.setName("Close-Werte");
            for (int i = 0; i < close.size()-1; i++) {
                aktienDaten.getData().add(new XYChart.Data(date.get(i).toString(), close.get(i)));
            }
            
            XYChart.Series<String, Number> durchschnitt = new XYChart.Series();

            durchschnitt.setName("gleitender Durchschnitt");
            for (int i = 0; i < gleitenderDurchschnitt.size()-1; i++) {
                durchschnitt.getData().add(new XYChart.Data(date.get(i).toString(), gleitenderDurchschnitt.get(i)));
            }

            Scene scene = new Scene(lineChart, 800, 600);
            lineChart.getData().add(aktienDaten);
            lineChart.getData().add(durchschnitt);
            aktienDaten.nodeProperty().get().setStyle("-fx-stroke: #000000; ");
            durchschnitt.nodeProperty().get().setStyle("-fx-stroke: #FFFFFF; ");

            if(close.get(aktienDaten.getData().size()-1) < gleitenderDurchschnitt.get(aktienDaten.getData().size()-1)) {
                scene.getStylesheets().add("redChart.css");
            }
            if(close.get(aktienDaten.getData().size()-1) > gleitenderDurchschnitt.get(aktienDaten.getData().size()-1)) {
                scene.getStylesheets().add("greenChart.css");
            }

            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(Collections.min(close) * 0.8);
            yAxis.setUpperBound(Collections.max(close) * 1.2);

            lineChart.setCreateSymbols(false);

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
