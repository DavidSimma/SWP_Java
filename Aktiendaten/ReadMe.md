Ziel:
  Durch das Ausführen einer CMD-Datei werden mehrere Aktienkurse von verschiedenen Firmen erstellt und als PNG abgespeichert. Dabei wird einerseits ein Graph der Close-Werte         angezeigt, sowie ein Graph des gleitende Mittelwertes. Die Close-Werte werden außerdem mit dem zugehörigen Datum in eine Datenbank abgespeichert.

Installation:
  Hierfür geben Sie in die Git Bash Command Line erst ihren gewünsten Speicherort ein und anschließend den Befehl "git clone github.com/DavidSimma/SWP_Java"
  Hierbei können SIe das Programm "Binärsuche" löschen oder ignorieren!
  
Wie führe ich es aus?
  Für die Ausführung ist das Programm MySQL erforderlich, sowie einen Benutzer "user" (kann im Programm beliebig geändert werden; das Password des Benutzers wird extern             ausgelesen) und eine Datenbank "aktiendaten".
  
Ergebnis:

<img src="https://user-images.githubusercontent.com/56593280/112457337-f365af80-8d5b-11eb-9058-6518a13520cb.png">

  Als Ergebnis erscheint ein Graph, welcher einerseits die Close-Werte, als auch den gleitenden Mittelwert der letzten 200 Werte anzeigt. Der Hintergrund der Graphen färbt sich,
  wenn die Close-Werte höher als der Durchschnitt ist, grün und ansonsten rot.
  
Was brauche ich dafür?
  - IntelliJ
  - MySQL Workbench
  - einen API-Key von "alphavantage"

Wie führe ich das Program aus? 

  Nach dem Download des Programms finden Sie eine CMD-Befehl.cmd Datei, welche entweder manuell oder automatisch ausgeführt werden kann. Anschließend speichert das Programm die     verschiedenne Aktienkurse in einen Ordner ab, welcher im Programmcode geändert werden kann!
