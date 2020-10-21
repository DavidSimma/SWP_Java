import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import org.apache.commons.io.IOUtils;
import org.sqlite.date.DateFormatUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class Programm extends Application {
    public static HashMap<LocalDate, String> feiertage = new HashMap();
    public static HashMap<LocalDate, String> ferien = new HashMap();
    public static List<String> nix = new ArrayList<>();
    public static int monday=0,tuesday=0,wednesday=0,thursday=0,friday=0, anfangsJahr, endjahr;
    public static List<Integer> time = new ArrayList<>();

    public static void main(String[] args){

        Scanner reader = new Scanner(System.in);

        LocalDate k = LocalDate.now();

        long start = System.currentTimeMillis();

        System.out.print("Anfangsjahr: ");
        anfangsJahr = reader.nextInt();
        System.out.print("Endjahr: ");
        endjahr = reader.nextInt();

        feiertageErzeugen(anfangsJahr, endjahr);

        for(LocalDate ld : feiertage.keySet()){
            switch (ld.getDayOfWeek()){
                case MONDAY:
                    if (!ferien.containsKey(ld)) monday+=1;
                    break;
                case TUESDAY:
                    if (!ferien.containsKey(ld)) tuesday+=1;
                    break;
                case WEDNESDAY:
                    if (!ferien.containsKey(ld)) wednesday+=1;
                    break;
                case THURSDAY:
                    if (!ferien.containsKey(ld)) thursday+=1;
                    break;
                case FRIDAY:
                    if (!ferien.containsKey(ld)) friday+=1;
                    break;
                default:
                    break;
            }

        }
        long end = System.currentTimeMillis();
        createNewDatabase("hollidays.db");
        connect();
        createNewTable();
        insert((int)(end - start));
        selectAll();

        int all = 0;
        for(int i : time){
            all +=i;
        }
        System.out.println("Das Programm dauert duchschnittlich "+all/time.size()+" Millisekunden");
        launch(args);



    }



    public static void createNewDatabase(String fileName) {

        String url = "jdbc:sqlite:C:/sqlite/" + fileName;

        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:C:/sqlite/hollidays.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void createNewTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:C://sqlite/hollidays.db";

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS zeiten (datum DATETIME_INTERVAL_CODE, zeit INT NOT NULL);";

        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("ö"+e.getMessage());
        }
    }

    private static Connection connectToTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:C://sqlite/hollidays.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("ä" + e.getMessage());
        }
        return conn;
    }

    public static void insert(int time) {
        String sql = "INSERT INTO zeiten(datum, zeit) VALUES(?,?)";

        try{
            Connection conn = connectToTable();
            LocalDateTime dateTime = LocalDateTime.now();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setDouble(2, time);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ü"+e.getMessage());
        }
    }

    public static void selectAll(){
        String sql = "SELECT * FROM zeiten";

        try {
            Connection conn = connectToTable();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                System.out.println(
                        rs.getDate("datum") + "\t" +
                        rs.getInt("zeit"));
                time.add(rs.getInt("zeit"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void feiertageErzeugen(int anfang, int ende){
        String urlBase = "https://feiertage-api.de/api/?jahr=";
        String urlBase2 = "https://ferien-api.de/api/v1/holidays/BY/";
        feiertageEinlesen();
        for (int i = anfang; i <= ende; i++){
            for(int o = 0; o<nix.size(); o++) {
                String[] temp = new String[3];
                LocalDate temp2;
                temp = nix.get(o).split(",");
                temp2 = LocalDate.of(i, Integer.parseInt(temp[1]), Integer.parseInt(temp[0]));
                feiertage.put(temp2, temp[2].toString());

            }
            feiertageEinlesen(urlBase, i, "Ostermontag");
            feiertageEinlesen(urlBase, i, "Christi Himmelfahrt");
            feiertageEinlesen(urlBase, i, "Pfingstmontag");
            feiertageEinlesen(urlBase, i, "Fronleichnam");
            feiertageEinlesen(urlBase2, i, "winterferien");
            feiertageEinlesen(urlBase2, i, "osterferien");
            feiertageEinlesen(urlBase2, i, "pfingstferien");
            feiertageEinlesen(urlBase2, i, "sommerferien");
            feiertageEinlesen(urlBase2, i, "herbstferien");
            feiertageEinlesen(urlBase2, i, "weihnachtsferien");

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

    public static JSONArray JsonArrayEinlesen(String url){
        JSONArray json = new JSONArray();
        try {
            json = new JSONArray(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
        }
        catch (MalformedURLException e){
            System.out.println("URL funktioniert nicht");
        }catch (IOException e){
            System.out.println("Feiertage konnten nicht eingelesen werden");
        }
        return json;
    }

    private static void feiertageEinlesen(String urlBase, int year, String name) {


        if(urlBase == "https://feiertage-api.de/api/?jahr=") {
            JSONObject json = Jsoneinlesen(urlBase + year);
            String date = json.getJSONObject("BY").getJSONObject(name).get("datum").toString();
            feiertage.put(LocalDate.parse(date), name);
        }
        if(urlBase == "https://ferien-api.de/api/v1/holidays/BY/"){
            JSONArray json = JsonArrayEinlesen(urlBase + year);
            for (int i = 0; i <= json.length()-1; i++){
                JSONObject jObject = (JSONObject) json.get(i);
                String temp = (String) jObject.get("end");
                String endDate = temp.split("T")[0];
                String temp2 = (String) jObject.get("start");
                String startDate = temp2.split("T")[0];
                for (LocalDate o = LocalDate.parse(startDate); o.isBefore(LocalDate.parse(endDate).plusDays(1)); o = o.plusDays(1)){
                    ferien.put(o, name);
                    //System.out.println(o);
                }
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
