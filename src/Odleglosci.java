import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.regex.Matcher;

public class Odleglosci {
    public static void main(String[] args) throws FileNotFoundException {

        File plik1 = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_1ob.txt");
        File plik2 = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_2ob.txt");
        PrintWriter output1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_1ob.out");
        PrintWriter output2 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_2ob.out");
        //PrintWriter log = new PrintWriter("./log.rav");

        // ----------  Obliczanie interwalu  ------------
        Scanner tmp1Scan = new Scanner(plik1);
        String tmp1String = tmp1Scan.nextLine();
        String tmp2String = tmp1Scan.nextLine();
        String[] tmp1dane = tmp1String.split(" ");
        String[] tmp2dane = tmp2String.split(" ");
        int l = 1;
        int n = 1;
        while (tmp1dane[l].equals("")) {
            l++;
        }
        while (tmp2dane[n].equals("")) {
            n++;
        }
        double tmp1double = Double.parseDouble(tmp1dane[l]);
        double tmp2double = Double.parseDouble(tmp2dane[n]);
        //log.println(tmp1dane[l]+" "+tmp2dane[n]);
        //log.println(tmp1double+" "+tmp2double);
        double interval = tmp2double - tmp1double;
        //log.println(interval);

        //stan
        double int1 = 0.00000000;
        double int2 = 0.00000000;

//        if(plik1.exists())
//            System.out.println("1");
//        if(plik2.exists())
//            System.out.println("2");

        Scanner p1 = new Scanner(plik1);

        int x = 0; //liczba ogarnietych linijek
        while (p1.hasNextLine()) {
            Scanner p2 = new Scanner(plik2);
            String p1_string = p1.nextLine();
            String[] p1_dane = p1_string.split(" ");

            while (p2.hasNextLine()) {
                String p2_string = p2.nextLine();
                String[] p2_dane = p2_string.split(" ");
                //jezeli dane z pierwszych kolumn sie zgadzaja
                if (p2_dane[0].equals(p1_dane[0])) {
                    //data i godzina
                    output1.print(p1_dane[0] + "\t");
                    output2.print(p2_dane[0] + "\t");
                    //czarna magia
                    int i = 1;
                    int j = 1;
                    int l_kol = 1;
                    while (l_kol != 8) {
                        while (p1_dane[i].equals("")) {
                            i++;
                        }
                        while (p2_dane[j].equals("")) {
                            j++;
                        }
                        //druga kolumna w pliku wyjsciowym
                        if (l_kol == 1) {
                            output1.print(int1 + "\t");
                            int1 += interval;

                            output2.print(int2 + "\t");
                            int2 += interval;
                        }
                        //reszta
                        else {
                            output1.print(p1_dane[i] + "\t");
                            output2.print(p2_dane[j] + "\t");
                        }
                        l_kol++;
                        i++;
                        j++;
                    }
                    //koniec linii w plikach
                    output1.print("\n");
                    output2.print("\n");
                    x++;
                    //System.out.println(p2_dane[0] +"\t"+ p1_dane[0]);
                    break;
                }
            }
        }

        //System.out.println("X -> "+x);//ilosc linii
        //System.out.println("InterwaĹ‚ -> "+interval); //interwal
        output1.close();
        output2.close();
        //log.close();

//OBLICZANIE ODLEGLOSCI MIEDZY OBIEKTAMI
        File plik3 = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_1ob.out");
        File plik4 = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_2ob.out");
        PrintWriter output3 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/odleglosci.out");
        System.out.println("Wykonuję obliczenia...\n" + "Proszę czekać...");
        Scanner p3 = new Scanner(plik3);

        int x1 = 0; //liczba ogarnietych linijek
        while (p3.hasNextLine()) {
            Scanner p4 = new Scanner(plik4);
            String p3_string = p3.nextLine();
            String[] p3_dane = p3_string.split("\t");

            while (p4.hasNextLine()) {
                String p4_string = p4.nextLine();
                String[] p4_dane = p4_string.split("\t");
                if (p4_dane[0].equals(p3_dane[0])) {
                    //data i godzina
                    output3.print(p3_dane[0] + "\t");
                    output3.print(p3_dane[1]+"\t");
                    //obiekt glowny - obiekt zblizajacy sie
                    double X=Math.pow(Double.parseDouble(p3_dane[2])-Double.parseDouble(p4_dane[2]),2);
                    double Y=Math.pow(Double.parseDouble(p3_dane[3])-Double.parseDouble(p4_dane[3]),2);
                    double Z=Math.pow(Double.parseDouble(p3_dane[4])-Double.parseDouble(p4_dane[4]),2);
                    double odleglosci = Math.sqrt(X+Y+Z);
                    output3.print(odleglosci);
                    output3.print("\n");
                }
            }

        }
        output3.close();
    }

}