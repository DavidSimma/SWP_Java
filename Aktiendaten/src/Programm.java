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
import java.util.*;

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

import fxjava.*;

public class Programm extends Application{

    public static Scanner reader = new Scanner(System.in);
    public static Connection connection;
    public static String DBurl = "jdbc:mysql://localhost:3306/aktiendaten?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    public static List<Double> close = new ArrayList<>();
    public static List<Double> open = new ArrayList<>();
    public static List<Double> high = new ArrayList<>();
    public static List<Double> low = new ArrayList<>();


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
        for (LocalDate i = LocalDate.now().minusDays(1); i.isAfter(LocalDate.now().minusDays(100)); i=i.minusDays(1)){
            try{
                temp1 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("4. close");
                close.add(temp1);
                temp2 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("1. open");
                open.add(temp2);
                temp3 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("2. high");
                high.add(temp3);
                temp4 = jsonObject.getJSONObject("Time Series (Daily)").getJSONObject(i.toString()).getDouble("3. low");
                low.add(temp4);
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

    public static boolean connectToMySql(String firma){
        try {
            connection = DriverManager.getConnection(DBurl,"admin",txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\MySQLPassword.txt"));
            Statement myStmt = connection.createStatement();
            String tabelleErzeugen = "create table if not exists " + firma +"(datum DATE, open DOUBLE, close DOUBLE, high DOUBLE, low DOUBLE);";
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
            System.out.println("Datenbank verknüpft");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        CandleStickChart candleStickChart = new CandleStickChart("Aktienwerte der letzen 100 Tage", buildBars());
        Scene scene = new Scene(candleStickChart);
        scene.getStylesheets().add("/styles/CandleStickChartStyles.css");

        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();

        candleStickChart.setYAxisFormatter(new DecimalAxisFormatter("#000.00"));
    }

    public List<BarData> buildBars() {


        final List<BarData> bars = new ArrayList<>();
        GregorianCalendar now = new GregorianCalendar();
        for (int i = 0; i < open.size(); i++) {
            double _open = open.get(i);
            double _close = close.get(i);
            double _high = high.get(i);
            double _low = low.get(i);

            BarData bar = new BarData((GregorianCalendar) now.clone(), _open, _high, _low, _close, 1);
            //
            now.add(Calendar.DATE, +1);
            bars.add(bar);
        }
        return bars;
    }

}
