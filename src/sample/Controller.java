package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sun.misc.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    ObservableList listSupp = FXCollections.observableArrayList();
    ObservableList listClie = FXCollections.observableArrayList();

    @FXML
    private ChoiceBox<String> seriesSupp, seriesClie;
    @FXML
    private TextField iloscDostawcow,iloscOdbiorcow,podaz,kosztyJednostkoweZakupu,popyt,cenaSprzedazy;
    @FXML
    private TextArea kosztyJednostkoweTransportu, results, log;

    private void loadData() {
        for(int i=0;i<10;i++){
            listClie.add(""+(i+1));
            listSupp.add(""+(i+1));
        }
        seriesClie.getItems().addAll(listClie);
        seriesSupp.getItems().addAll(listSupp);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
    }

    private void printResults(String result, String log){
        this.results.setText(result);
        this.log.setText(log);
    }

    public void onStartClicked(ActionEvent e) {
        String iloscDostawcow, iloscOdbiorcow, kosztyJednostkoweTransportu, podaz, kosztyJednostkoweZakupu, blokadaDostawcy, popyt, cenaSprzedazy, blokadaOdbiorcy;


        iloscDostawcow=this.iloscDostawcow.getText();
        iloscOdbiorcow=this.iloscOdbiorcow.getText();
        kosztyJednostkoweTransportu=this.kosztyJednostkoweTransportu.getText();
        podaz=this.podaz.getText();
        kosztyJednostkoweZakupu=this.kosztyJednostkoweZakupu.getText();
        blokadaDostawcy=this.seriesSupp.getValue();
        popyt=this.popyt.getText();
        cenaSprzedazy=this.cenaSprzedazy.getText();
        blokadaOdbiorcy=this.seriesClie.getValue();


        StringBuilder sb = new StringBuilder()
                .append(iloscDostawcow+" "+iloscOdbiorcow)
                .append("\n"+podaz)
                .append("\n"+popyt)
                .append("\n"+kosztyJednostkoweTransportu)
                //nowe
                .append("\n"+kosztyJednostkoweZakupu)
                .append("\n"+cenaSprzedazy)
                .append("\n"+blokadaDostawcy)
                .append("\n"+blokadaOdbiorcy)
                ;


        //USUWANIE UTWORZONYCH PLIKOW
        File file = new File("input1.txt");

        if(file.delete())
        {
            System.out.println("File deleted successfully");
        }
        else
        {
            System.out.println("Failed to delete the file");
        }
        File file2 = new File("finalsolutioninput1.txt.txt");

        if(file2.delete())
        {
            System.out.println("File deleted successfully");
        }
        else
        {
            System.out.println("Failed to delete the file");
        }
        File file3 = new File("solutioninput1.txt.txt");

        if(file3.delete())
        {
            System.out.println("File deleted successfully");
        }
        else
        {
            System.out.println("Failed to delete the file");
        }File file4 = new File("logs.txt");

        if(file4.delete())
        {
            System.out.println("File deleted successfully");
        }
        else
        {
            System.out.println("Failed to delete the file");
        }



        //ZAPiS DANYCH Z GUI DO PLIKU
        try (PrintWriter out = new PrintWriter("input1.txt")) {
            out.println(sb);
        } catch (Exception ec){}

        //WLACZENIE PROGRAMU I WYPISANIE WYNIKOW
        try {
            for (String filename : new String[]{"input1.txt"/*, "input2.txt"*/})
            {
                Logger.getInstance().log("Wczytanie danych wejściowych");
                TransportationProblem1.init(filename);
                TransportationProblem1.leastCostRule();
                Logger.getInstance().log("Macierz zysków po metodzie leastCostRule");
                TransportationProblem1.logCurrnetCosts();

                //TransportationProblem1.northWestCornerRule();
                TransportationProblem1.printResult(filename);
                TransportationProblem1.saveFile("solution" + filename);
                TransportationProblem1.steppingStone();
                TransportationProblem1.printResult(filename);
                TransportationProblem1.saveFile("finalsolution" + filename);
                Logger.getInstance().log("Koniec pracy programu");
            }






            String logs = new String(Files.readAllBytes(Paths.get("logs.txt")));
            String content = new String(Files.readAllBytes(Paths.get("finalsolutioninput1.txt.txt")));
            printResults(content, logs);

        }
        catch (Exception ec){}



}


}
