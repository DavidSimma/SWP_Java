import java.sql.*;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Simulation {
    private static Scanner reader = new Scanner(System.in);

    public static void main(String[] args) {
        List<String> firmen = Programm.txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\Aktien\\Aktienkürzel.txt");
        double depot=0,splitDepot=0,toleranz=0;
        LocalDate startdate = null;
        do{
            System.out.print("Startdepot: ");
            depot = reader.nextDouble();
        }while(Double.isNaN(depot));

        splitDepot = depot / firmen.size();
        do {
            System.out.print("Startdatum[yyyy-mm-dd]: ");
            startdate = LocalDate.parse(reader.next());
        }while(!isDateValid(startdate.toString()));

        do {
            System.out.print("Toleranz: ");
            toleranz = reader.nextDouble();
        }while (Double.isNaN(toleranz));

        double endMoney = 0, endMoney2 = 0;
        for (String s : firmen){
            System.out.println(s);
            endMoney += einmaligeAktion(s, splitDepot, startdate);
            endMoney2 += aktionBeiSchnitt(s, splitDepot, startdate, toleranz);
        }
        System.out.println("Ihr Geldbetrag nach Ausführung von Buy and Hold beträgt " + endMoney + "$");
        System.out.println("Ihr Geldbetrag nach Ausführung von Aktion bei 200er Schnitt beträgt " + endMoney2 + "$");
    }
    public static double aktionBeiSchnitt(String firma, double depot, LocalDate startdate, double toleranz){
        String method = "Schnitt", simulationsFirma;
        ResultSet rs;
        connectToMySql(firma, method, depot, startdate);
        simulationsFirma = firma + method;
        try {
            Statement myStmt = Programm.connection.createStatement();
            for(LocalDate ld = startdate; ld.isBefore(LocalDate.now()); ld=ld.plusDays(1)){
                List<Double> result = selectByLastDate(myStmt, simulationsFirma, Arrays.asList("event", "depot", "shares"));
                double event = result.get(0), dep=result.get(1), shares=result.get(2);
                if (ld.isEqual(getLastDay(myStmt, firma))) {
                    sellLast(myStmt, firma, ld, toleranz, shares, dep, simulationsFirma);
                    break;
                }

                if(event == 0){
                    buy(myStmt, firma, ld, toleranz, dep, simulationsFirma);
                }else if(event == 1){
                    sell(myStmt, firma, ld, toleranz, shares, dep, simulationsFirma);
                }
            }
            List<Double> result = selectByLastDate(myStmt, simulationsFirma, Arrays.asList("depot"));
            double endDepo=result.get(0);
            return endDepo;

        }catch (SQLException e){
            e.printStackTrace();
        }
        return 420.69;

    }
    public static double einmaligeAktion(String firma, double depot, LocalDate startdatum){

        try {
            connectToMySql();
            Statement myStmt = Programm.connection.createStatement();
            try {
                startdatum = getDay(myStmt, firma, startdatum);
                double closeStart = selectByDate(myStmt, firma, startdatum, Arrays.asList("close")).get(0);

                double share, depoRest;
                if (depot % closeStart == 0) {
                    share = depot / closeStart;
                    depoRest = 0;
                } else {
                    share = Math.floor(depot / closeStart);
                    depoRest = depot % closeStart;
                }
                double closeEnd = selectByLastDate(myStmt, firma, Arrays.asList("close")).get(0);
                return closeEnd * share + depoRest;
            }catch (IndexOutOfBoundsException e){
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return 420.69;
    }

    public static void buy(Statement myStmt, String firma, LocalDate ld, double toleranz, double dep, String simulationsFirma){
        try {
            List<Double> result2 = selectByDate(myStmt, firma, ld, Arrays.asList("close", "average"));
            double close = result2.get(0), avg = result2.get(1);
            avg *= (1 + (toleranz / 100));
            if (close > avg) {
                double share, depo;
                if (dep % close == 0) {
                    share = dep / close;
                    depo = 0;
                } else {
                    share = Math.floor(dep / close);
                    depo = dep % close;
                }
                insert(myStmt, simulationsFirma, ld, 1, share, depo);
            }
        }catch (IndexOutOfBoundsException e){
        }
    }
    public static void sell(Statement myStmt, String firma, LocalDate ld, double toleranz, double shares, double dep, String simulationsFirma){
        try {
            List<Double> result3 = selectByDate(myStmt, firma, ld, Arrays.asList("close", "average"));
            double close = result3.get(0), avg = result3.get(1);
            avg *= (1 + (toleranz / 100));
            if (close < avg) {
                double depo;
                depo = (close * shares) + dep;
                insert(myStmt, simulationsFirma, ld, 0, 0, depo);
            }
        }catch (IndexOutOfBoundsException e){
        }
    }
    public static void sellLast(Statement myStmt, String firma, LocalDate ld, double toleranz, double shares, double dep, String simulationsFirma){
        try {
            List<Double> result3 = selectByDate(myStmt, firma, ld, Arrays.asList("close"));
            double close = result3.get(0);
            double depo;
            depo = (close * shares) + dep;
            insert(myStmt, simulationsFirma, ld, 0, 0, depo);
        }catch (IndexOutOfBoundsException e){
        }
    }


    public static boolean connectToMySql(String firma, String method, double depot, LocalDate startdate){
        try {
            Programm.connection = DriverManager.getConnection(Programm.DBurl,"user",Programm.txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\MySQLPassword.txt").get(0));
            Statement myStmt = Programm.connection.createStatement();
            firma+=method;
            String tabelleLöschen = "drop table if exists "+firma;
            String tabelleErzeugen = "create table if not exists " + firma +"(datum DATE primary key, event double, shares DOUBLE, depot DOUBLE);";
            String addPseudo = "insert ignore into "+firma+" values(\'"+startdate.minusDays(1)+"\', 0, 0, "+depot+")";
            myStmt.executeUpdate(tabelleLöschen);
            myStmt.executeUpdate(tabelleErzeugen);
            myStmt.executeUpdate(addPseudo);
            System.out.println("Datenbank verknüpft");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean connectToMySql(){
        try {
            Programm.connection = DriverManager.getConnection(Programm.DBurl,"user",Programm.txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\MySQLPassword.txt").get(0));
            System.out.println("Datenbank verknüpft");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void insert(Statement myStmt, String firma, LocalDate ld, double action, double shares, double depot){
        try {
            String sql = "insert into "+firma+" values(\'"+ld+"\', "+action+", "+shares+", "+depot+") "
                    + "on duplicate key update event = "+action+", shares = "+shares+", depot = "+depot+";";
            myStmt.executeUpdate(sql);
       } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public static List<Double> selectByDate(Statement myStmt, String firma, LocalDate ld, List<String> col){
        List<Double> result = new ArrayList<>();

        String getValues = "select * from "+firma+" where datum = \'"+ld+"\' having datum is not null;";
        try {
            ResultSet rs = myStmt.executeQuery(getValues);
            while (rs.next()){
                for(String s : col){
                    result.add(rs.getDouble(s));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }
    public static List<Double> selectByLastDate(Statement myStmt, String firma, List<String> col){
        List<Double> result = new ArrayList<>();
        String getValues = "select * from "+firma+" order by datum desc limit 1;";
        try {
            ResultSet rs = myStmt.executeQuery(getValues);
            while (rs.next()){
                for(String s : col){
                    result.add(rs.getDouble(s));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }
    public static LocalDate getLastDay(Statement myStmt, String simulationsFirma){
        LocalDate ld = null;
        String getValues = "select * from "+simulationsFirma+" order by datum desc limit 1;";
        try {
            ResultSet rs = myStmt.executeQuery(getValues);
            while (rs.next()){
                ld = LocalDate.parse(rs.getDate("datum").toString());
            }
            return ld;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static LocalDate getDay(Statement myStmt, String firma, LocalDate ld){
        LocalDate date = null;
        String getValues = "select * from "+firma+" where datum >= '"+ld+"' order by datum asc limit 1;";
        try {
            ResultSet rs = myStmt.executeQuery(getValues);
            while (rs.next()){
                date = LocalDate.parse(rs.getDate("datum").toString());
            }
            return date;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
    public static boolean isDateValid(String date){
        try{
            LocalDate ld = LocalDate.parse(date);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}