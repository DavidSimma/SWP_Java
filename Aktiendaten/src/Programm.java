import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class Programm {

    public static Scanner reader = new Scanner(System.in);
    public static Connection connection;
    public static String DBurl = "jdbc:mysql://localhost:3306/aktiendaten?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    public static void main(String[] args) {
        String key = txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\AktienDatenPW.txt");
        //System.out.println(key);
        System.out.print("Firmenabkürzung angeben: ");
        String firma = reader.next();

        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + firma+ "&apikey=" + key;
        //System.out.println(url);
        //System.out.println(closeDatenEinlesen(url));

        connectToMySql(firma);
        datenEinlesenUndSchreiben(url, firma);
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

    public static List<Double> datenEinlesenUndSchreiben(String url, String firma){
        List<Double> data = new ArrayList<>();
        JSONObject jsonObject = Jsoneinlesen(url);
        for (LocalDate i = LocalDate.now().minusDays(1); i.isAfter(LocalDate.now().minusDays(100)); i=i.minusDays(1)){
            try{
                double temp = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("4. close");
                data.add(temp);
                writeDataInDB(i, firma, temp);
            }
            catch (JSONException e){

            }

        }
        return data;

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

    public static boolean connectToMySql(String firma){
        try {
            connection = DriverManager.getConnection(DBurl,"admin",txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\MySQLPassword.txt"));
            Statement myStmt = connection.createStatement();
            String tabelleErzeugen = "create table if not exists " + firma +"(datum DATE, kurs DOUBLE);";
            myStmt.executeUpdate(tabelleErzeugen);
            System.out.println("Datenbank verknüpft");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static void writeDataInDB(LocalDate date, String firma, double kurs){
        try {
            Statement myStmt = connection.createStatement();
            String writeData = "insert into "+ firma +"(datum, kurs) values(\'"+date+"\', "+kurs+") on duplicate key update datum = datum, kurs = kurs";
            myStmt.executeUpdate(writeData);
            System.out.println("Datenbank verknüpft");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
