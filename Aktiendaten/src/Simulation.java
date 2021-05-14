import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.DayOfWeek;

public class Simulation {



    public static void main(String[] args) {
        System.out.println(aktionBeiSchnitt("aapl", 20000));
    }
    public static double aktionBeiSchnitt(String firma, double depot){
        String method = "Schnitt", Simulationfirma;
        connectToMySql(firma, method, depot);
        Simulationfirma = firma + method;
        try {
            Statement myStmt = Programm.connection.createStatement();
            for(LocalDate ld = LocalDate.of(2010,1,1); ld.isBefore(LocalDate.now()); ld=ld.plusDays(1)){
                System.out.println(ld);
                if(börsentag(ld)){
                    String event = "";
                    double dep=0, shares=0;
                    String sellOrBuy = "select * from "+Simulationfirma+" order by datum desc limit 1;";
                    ResultSet rs = myStmt.executeQuery(sellOrBuy);
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
                            ResultSet rss = myStmt.executeQuery(getValues);
                            System.out.println("Datenbank-Aufruf-ori-s");
                            while (rss.next()){
                                close = rss.getDouble("close");
                                avg = rss.getDouble("average");
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
                                String insertBuy = "insert into "+Simulationfirma+" values(\'"+ld+"\', \'buy\'"+share+", "+depo+");";
                                myStmt.executeUpdate(insertBuy);
                                System.out.println("Datenbank-Eintrag");
                            }
                        }
                    }else if(event.equals("buy")){
                        double close = 0, avg = 0;
                        String getValues = "select * from "+firma+" where datum = \'"+ld+"\';";
                        ResultSet rss = myStmt.executeQuery(getValues);
                        System.out.println("Datenbank-Aufruf-ori-b");
                        while (rss.next()){
                            close = rss.getDouble("close");
                            avg = rss.getDouble("average");
                        }
                        if(close < avg){
                            double depo;
                            depo = (close*shares)+dep;
                            String insertBuy = "insert into "+Simulationfirma+" values(\'"+ld+"\', \'buy\', 0, "+depo+");";
                            myStmt.executeUpdate(insertBuy);
                            System.out.println("Datenbank-Eintrag");
                        }
                    }


                }
            }
            String endDepot = "select * from "+Simulationfirma+" order by datum desc limit 1";
            ResultSet ress = myStmt.executeQuery(endDepot);
            System.out.println("Datenbank-Aufruf");
            double endDepo=0;
            while (ress.next()){
                endDepo = ress.getDouble("depot");
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

    public static boolean connectToMySql(String firma, String method, double depot){
        try {
            Programm.connection = DriverManager.getConnection(Programm.DBurl,"user",Programm.txtEinlesen("C:\\Users\\simma\\Documents\\Schule\\SWP\\MySQLPassword.txt").get(0));
            Statement myStmt = Programm.connection.createStatement();
            firma+=method;
            String tabelleErzeugen = "create table if not exists " + firma +"(datum DATE primary key, event varchar(6), shares DOUBLE, depot DOUBLE);";
            String addPseudo = "insert ignore into "+firma+" values(\'1950-01-01\', \'sell\', 0, "+depot+")";
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
