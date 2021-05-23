import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.Scanner;

public class Simulation {
    private static Scanner reader = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Verfügbare Firmen: " + Programm.txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\Aktien\\Aktienkürzel.txt"));
        System.out.print("Firma: ");
        String firma = reader.next().toLowerCase();
        System.out.print("Startdepot: ");
        double depot = reader.nextDouble();
        LocalDate startdate = LocalDate.of(2010,1,1);


        System.out.println(aktionBeiSchnitt(firma, depot, startdate));
    }
    public static double aktionBeiSchnitt(String firma, double depot, LocalDate startdate){
        String method = "Schnitt", simulationsFirma;
        ResultSet rs;
        connectToMySql(firma, method, depot, startdate);
        simulationsFirma = firma + method;
        try {
            Statement myStmt = Programm.connection.createStatement();
            for(LocalDate ld = startdate; ld.isBefore(LocalDate.now()); ld=ld.plusDays(1)){
                System.out.println(ld);
                if(börsentag(ld)){
                    String event = "";
                    double dep=0, shares=0;
                    String sellOrBuy = "select * from "+simulationsFirma+" order by datum desc limit 1;";
                    rs = myStmt.executeQuery(sellOrBuy);
                    System.out.println("Datenbank-Aufruf-simu");
                    while(rs.next()){
                        event = rs.getString("event");
                        dep = rs.getDouble("depot");
                        shares = rs.getDouble("shares");
                    }
                    System.out.println("|"+event+"|");
                    if(event.equals("sell")){
                        System.out.println(!ld.isEqual(LocalDate.now().minusDays(1)));
                        if(!ld.isEqual(LocalDate.now().minusDays(1))){
                            double close = 0, avg = 0;
                            String getValues = "select * from "+firma+" where datum = \'"+ld+"\';";
                            rs = myStmt.executeQuery(getValues);
                            System.out.println("Datenbank-Aufruf-ori-s");
                            while (rs.next()){
                                close = rs.getDouble("close");
                                avg = rs.getDouble("average");
                            }
                            System.out.println(close);
                            if(close > avg){
                                double share, depo;
                                if(dep % close == 0){
                                    share = dep/close;
                                    depo = 0;
                                }else{
                                    share = Math.floor(dep/close);
                                    depo = dep%close;
                                }
                                String insertBuy = "insert ignore into "+simulationsFirma+" values(\'"+ld+"\', \'buy\', "+share+", "+depo+");";
                                myStmt.executeUpdate(insertBuy);
                                System.out.println("Datenbank-Eintrag");
                            }
                        }
                    }else if(event.equals("buy")){
                        double close = 0, avg = 0;
                        String getValues = "select * from "+firma+" where datum = \'"+ld+"\';";
                        rs = myStmt.executeQuery(getValues);
                        System.out.println("Datenbank-Aufruf-ori-b");
                        while (rs.next()){
                            close = rs.getDouble("close");
                            avg = rs.getDouble("average");
                        }
                        if(close < avg){
                            double depo;
                            depo = (close*shares)+dep;
                            String insertBuy = "insert ignore into "+simulationsFirma+" values(\'"+ld+"\', \'sell\', 0, "+depo+");";
                            myStmt.executeUpdate(insertBuy);
                            System.out.println("Datenbank-Eintrag");
                        }
                    }


                }
            }
            String endDepot = "select * from "+simulationsFirma+" order by datum desc limit 1";
            rs = myStmt.executeQuery(endDepot);
            System.out.println("Datenbank-Aufruf");
            double endDepo=0;
            while (rs.next()){
                endDepo = rs.getDouble("depot");
            }
            return endDepo;

        }catch (SQLException e){
            e.printStackTrace();
        }
        return 420.69;

    }
    public static double aktionNaheSchnitt(String firma, double depot){
        return 420.69;
    }
    public static double einmaligeAktion(String firma, double depot){
        return 420.69;
    }

    public static boolean börsentag(LocalDate ld){
        if (DayOfWeek.SATURDAY.equals(ld) || DayOfWeek.SUNDAY.equals(ld)) {
            return false;
        } else if (ld.equals(LocalDate.of(ld.getYear(), 1,1))) {
            return false;
        }else if (ld.equals(LocalDate.of(ld.getYear(), 4,2))) {
            return false;
        }else if (ld.equals(LocalDate.of(ld.getYear(), 4,5))) {
            return false;
        }else if (ld.equals(LocalDate.of(ld.getYear(), 5,24))) {
            return false;
        }else if (ld.equals(LocalDate.of(ld.getYear(), 10,26))) {
            return false;
        }else if (ld.equals(LocalDate.of(ld.getYear(), 11,1))) {
            return false;
        }else if (ld.equals(LocalDate.of(ld.getYear(), 12,8))) {
            return false;
        }else if (ld.equals(LocalDate.of(ld.getYear(), 12,24))) {
            return false;
        }else if (ld.equals(LocalDate.of(ld.getYear(), 12,31))) {
            return false;
        }
        return true;
    }

    public static boolean connectToMySql(String firma, String method, double depot, LocalDate startdate){
        try {
            Programm.connection = DriverManager.getConnection(Programm.DBurl,"user",Programm.txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\MySQLPassword.txt").get(0));
            Statement myStmt = Programm.connection.createStatement();
            firma+=method;
            String tabelleErzeugen = "create table if not exists " + firma +"(datum DATE primary key, event varchar(6), shares DOUBLE, depot DOUBLE);";
            String addPseudo = "insert ignore into "+firma+" values(\'"+startdate.minusDays(1)+"\', \'sell\', 0, "+depot+")";
            myStmt.executeUpdate(tabelleErzeugen);
            myStmt.executeUpdate(addPseudo);
            System.out.println("Datenbank verknüpft");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}