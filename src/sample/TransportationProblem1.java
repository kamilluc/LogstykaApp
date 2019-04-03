package sample;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toCollection;

public class TransportationProblem1
{
    private static int[] demand;
    private static int[] supply;
    private static double[][] costs; //u nas to bedzie Z czyli zysk
    private static Shipment[][] matrix;

    private static class Shipment
    {
        final double costPerUnit; // mozna usunac
        final int r, c;
        double quantity;

        public Shipment(double q, double cpu, int r, int c)
        {
            quantity = q;
            costPerUnit = cpu;
            this.r = r;
            this.c = c;
        }
    }

    static void init(String filename) throws Exception
    {

        try (Scanner sc = new Scanner(new File(filename)))
        {
            int numSources = sc.nextInt();
            int numDestinations = sc.nextInt();

            List<Integer> src = new ArrayList<>(); //lista z podaza
            List<Integer> dst = new ArrayList<>(); //lista popytem
            List<Integer> buy = new ArrayList<>(); //lista z podaza
            List<Integer> sell = new ArrayList<>(); //lista popytem

            for (int i = 0; i < numSources; i++)
                src.add(sc.nextInt());

            for (int i = 0; i < numDestinations; i++)
                dst.add(sc.nextInt());

            // Zrownowazenie popytu i podazy (jesli trzeba dodanie fikcyjnego dostawcy/odbiorcy)
            int totalSrc = src.stream().mapToInt(i -> i).sum();
            int totalDst = dst.stream().mapToInt(i -> i).sum();
            if (totalSrc > totalDst)
                dst.add(totalSrc - totalDst);
            else if (totalDst > totalSrc)
                src.add(totalDst - totalSrc);

            supply = src.stream().mapToInt(i -> i).toArray();
            demand = dst.stream().mapToInt(i -> i).toArray();

            costs = new double[supply.length][demand.length];
            matrix = new Shipment[supply.length][demand.length];

            for (int i = 0; i < numSources; i++)
                for (int j = 0; j < numDestinations; j++)
                    costs[i][j] = sc.nextDouble();

            //NOWE
                //wczytanie z pliku dodatkowych danych dla posrednika
            //ceny zakupu
            for (int i = 0; i < numSources; i++)
                buy.add(sc.nextInt());
            //cena sprzedazy
            for (int i = 0; i < numDestinations; i++)
                sell.add(sc.nextInt());

            //blokda
            int blokadaOdbiorcy, blokadaDostawcy;
            blokadaDostawcy=sc.nextInt();
            blokadaOdbiorcy=sc.nextInt();

            //aktualizacja zysku (macierz)
            for (int i = 0; i < numSources; i++)
                for (int j = 0; j < numDestinations; j++) { // z= c-kz-kt
                    costs[i][j] = sell.get(j) - buy.get(i) - costs[i][j];
                    System.out.println(costs[i][j]);
                }

            // /NOWE



            //Jesli pojawi sie fikcyjny dostawca/odbiorca ustawienie kosztow transportu na 9999
            if (supply.length > numSources)
            {
                for (int i = numSources; i < supply.length; i++)
                    for (int j = 0; j < demand.length; j++)
                    {
                        costs[i][j] = 0; //bylo 9999
                    }
            }
            else if (demand.length > numDestinations)
            {
                for (int i = 0; i < supply.length; i++)
                    for (int j = numDestinations; j < demand.length; j++)
                    {
                        costs[i][j] = 0; // bylo 9999
                    }
            }
        }
    }

    static void northWestCornerRule()
    {
        for (int r = 0, northwest = 0; r < supply.length; r++)
            for (int c = northwest; c < demand.length; c++)
            {
                int quantity = Math.min(supply[r], demand[c]);
                if (quantity > 0)
                {
                    matrix[r][c] = new Shipment(quantity, costs[r][c], r, c);

                    supply[r] -= quantity;
                    demand[c] -= quantity;

                    if (supply[r] == 0)
                    {
                        northwest = c;
                        break;
                    }
                }
            }
    }

    static void leastCostRule()
    {
        double min;
        int k = 0; //licznik mozliwych rozwiazan

        //isSet jest odpowiedzialny za opisywanie komorek juz przydzielonych
        boolean[][] isSet = new boolean[supply.length][demand.length];
        for (int j = 0; j < demand.length; j++)
            for (int i = 0; i < supply.length; i++)
                isSet[i][j] = false;

        int i = 0, j = 0;
        Variable minCost = new Variable();

        //petla ktora przeszukuje macierz i znajduje komorki,
        // ktore maja najmniejszy koszt transportu
        while (k < (supply.length + demand.length - 1))
        {

            minCost.setValue(Double.MAX_VALUE);
            //wybranie komorki o najmniejszym koszcie transportu
            for (int m = 0; m < supply.length; m++)
                for (int n = 0; n < demand.length; n++)
                    if (!isSet[m][n])
                        if (costs[m][n] < minCost.getValue())
                        {
                            minCost.setStock(m);
                            minCost.setRequired(n);
                            minCost.setValue(costs[m][n]);
                        }

            i = minCost.getStock();
            j = minCost.getRequired();

            //przydzielenie zapasow we wlasciwy sposob
            min = Math.min(demand[j], supply[i]);

            matrix[i][j] = new Shipment(min, costs[i][j], i, j);
            k++;

            demand[j] -= min;
            supply[i] -= min;

            //przydzielanie wartosci pustych w usunietym wierszu / kolumnie
            if (supply[i] == 0)
                for (int l = 0; l < demand.length; l++)
                    isSet[i][l] = true;
            else
                for (int l = 0; l < supply.length; l++)
                    isSet[l][j] = true;
        }
    }


    static void steppingStone()
    {
        double maxReduction = 0;
        Shipment[] move = null;
        Shipment leaving = null;

        fixDegenerateCase();

        for (int r = 0; r < supply.length; r++)
        {
            for (int c = 0; c < demand.length; c++)
            {

                if (matrix[r][c] != null)
                    continue;

                Shipment trial = new Shipment(0, costs[r][c], r, c);
                Shipment[] path = getClosedPath(trial);

                double reduction = 0;
                double lowestQuantity = Integer.MAX_VALUE;
                Shipment leavingCandidate = null;

                boolean plus = true;
                for (Shipment s : path)
                {
                    if (plus)
                    {
                        reduction += s.costPerUnit;
                    }
                    else
                    {
                        reduction -= s.costPerUnit;
                        if (s.quantity < lowestQuantity)
                        {
                            leavingCandidate = s;
                            lowestQuantity = s.quantity;
                        }
                    }
                    plus = !plus;
                }
                if (reduction < maxReduction)
                {
                    move = path;
                    leaving = leavingCandidate;
                    maxReduction = reduction;
                }
            }
        }

        if (move != null)
        {
            double q = leaving.quantity;
            boolean plus = true;
            for (Shipment s : move)
            {
                s.quantity += plus ? q : -q;
                matrix[s.r][s.c] = s.quantity == 0 ? null : s;
                plus = !plus;
            }
            steppingStone();
        }
    }

    static LinkedList<Shipment> matrixToList()
    {
        return stream(matrix)
                .flatMap(row -> stream(row))
                .filter(s -> s != null)
                .collect(toCollection(LinkedList::new));
    }

    static Shipment[] getClosedPath(Shipment s)
    {
        LinkedList<Shipment> path = matrixToList();
        path.addFirst(s);

        //petla, ktora sprawdza (a nastepnie usuwa) elementy,
        // ktore nie maja sasiadow w poziomie i w pionie
        while (path.removeIf(e -> {
            Shipment[] nbrs = getNeighbors(e, path);
            return nbrs[0] == null || nbrs[1] == null;
        })) ;

        // umiesc pozostale elementy w odpowiedniej kolejnosci plus-minus
        Shipment[] stones = path.toArray(new Shipment[path.size()]);
        Shipment prev = s;
        for (int i = 0; i < stones.length; i++)
        {
            stones[i] = prev;
            prev = getNeighbors(prev, path)[i % 2];
        }
        return stones;
    }

    static Shipment[] getNeighbors(Shipment s, LinkedList<Shipment> lst)
    {
        Shipment[] nbrs = new Shipment[2];
        for (Shipment o : lst)
        {
            if (o != s)
            {
                if (o.r == s.r && nbrs[0] == null)
                    nbrs[0] = o;
                else if (o.c == s.c && nbrs[1] == null)
                    nbrs[1] = o;
                if (nbrs[0] != null && nbrs[1] != null)
                    break;
            }
        }
        return nbrs;
    }

    static void fixDegenerateCase()
    {
        final double eps = Double.MIN_VALUE;

        if (supply.length + demand.length - 1 != matrixToList().size())
        {

            for (int r = 0; r < supply.length; r++)
                for (int c = 0; c < demand.length; c++)
                {
                    if (matrix[r][c] == null)
                    {
                        Shipment dummy = new Shipment(eps, costs[r][c], r, c);
                        if (getClosedPath(dummy).length == 0)
                        {
                            matrix[r][c] = dummy;
                            return;
                        }
                    }
                }
        }
    }

    static void printResult(String filename)
    {
        System.out.printf("Optymalne rozwiazanie %s%n%n", filename);
        double totalCosts = 0;

        for (int r = 0; r < supply.length; r++)
        {
            for (int c = 0; c < demand.length; c++)
            {

                Shipment s = matrix[r][c];
                if (s != null && s.r == r && s.c == c)
                {
                    System.out.printf(" %3s ", (int) s.quantity);
                    if (s.costPerUnit < 9999)
                        totalCosts += (s.quantity * s.costPerUnit);
                }
                else
                    System.out.printf("  -  ");
            }
            System.out.println();
        }
        System.out.printf("%nCalkowity koszt: %s%n%n", totalCosts);
    }

    static void saveFile(String outputfilename)
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(outputfilename + ".txt", "UTF-8");

            double totalCosts = 0;

            for (int r = 0; r < supply.length; r++)
            {
                for (int c = 0; c < demand.length; c++)
                {

                    Shipment s = matrix[r][c];
                    if (s != null && s.r == r && s.c == c)
                    {
                        writer.printf(" %3s ", (int) s.quantity);

                        if (s.costPerUnit < 9999)
                            totalCosts += (s.quantity * s.costPerUnit);
                    }
                    else
                        writer.printf("  -  ");
                }
                writer.println();
            }
            writer.printf("%nCalkowity koszt: %s%n%n", totalCosts);

            writer.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {

        for (String filename : new String[]{"input1.txt"/*, "input2.txt"*/})
        {
            init(filename);
            leastCostRule();
            //northWestCornerRule();
            printResult(filename);
            saveFile("solution" + filename);
            steppingStone();
            printResult(filename);
            saveFile("finalsolution" + filename);
        }
    }
}
