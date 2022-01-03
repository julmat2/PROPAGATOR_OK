import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

public class Zblizenia_min {
    public static String[] kolumny(String input){
        int ilosc_kolumn_w_pliku=5; //Do modyfikacji ile jest kolumn w pliku wejściowym
        String[] info = input.split(" ");
        int x=0;
        int y=0;
        String[] kolumny = new String[ilosc_kolumn_w_pliku];
        while(x!=info.length)
        {
            if(info[x].length()!=0)
            {
                kolumny[y]=info[x];
                y++;
            }
            x++;
        }
        return kolumny;
    }
    public static class Dane{
        String kol1; //data
        String kol2; //czas
        double kol3; //odleglosc

        public Dane(String kol1, String kol2, double kol3){
            this.kol1 = kol1;
            this.kol2 = kol2;
            this.kol3 = kol3;
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Hello world!");
        PrintWriter output = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/zblizenia_min.txt");
        File plik_in = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/distances.txt"); //pobranie pliku
        System.out.println("Jest plik: "+plik_in.exists()); //info czy istnieje plik
        Scanner plik_in_scan1 = new Scanner(plik_in);
        int il_linii=0; //rozmiar tablicy
        while(plik_in_scan1.hasNextLine()){
            plik_in_scan1.nextLine();
            //System.out.println(il_linii);
            il_linii++;

        }
        System.out.println("ilosc linii "+il_linii);//licznik ile jest linii w pliku wejssciowym
        Dane[] linia = new Dane[il_linii];
        double[] wartosci = new double[il_linii];
        plik_in_scan1.close(); //nie dawało mi to spokoju
        Scanner plik_in_scan2 = new Scanner(plik_in);
        int akt_linia=0;
        while(plik_in_scan2.hasNextLine()){
            String line = plik_in_scan2.nextLine(); //pobranie linii
            String[] elementy = kolumny(line); //podzial lini na 3 kolumny
//            System.out.println("1:"+elementy[0]);
//            System.out.println("2:"+elementy[1]);
//            System.out.println("3:"+elementy[2]);
            //Parsowanie danych w prostsze zmienne
            String kol1 = elementy[1];
            String kol2 = elementy[2];
            double kol3 = Double.parseDouble(elementy[4]);
//            System.out.println(akt_linia);
            linia[akt_linia]= new Dane(kol1,kol2,kol3);
            wartosci[akt_linia] = kol3;

            akt_linia++;
        }
        plik_in_scan2.close();

        Arrays.sort(wartosci);
        int ilosc = 20; // DO MODYFIKACJI
        int x=0;
        while(x!=ilosc)
        {
            int i=0;
            while(i!=il_linii)
            {
                if(linia[i].kol3==wartosci[x])
                {
                    System.out.println(x+": "+linia[i].kol1+" "+linia[i].kol2+" "+linia[i].kol3);
                    output.write(linia[i].kol1+"\t"+linia[i].kol2+"\t"+linia[i].kol3+"\n");

                    break;
                }
                i++;
            }
            x++;
        }

        System.out.println("Najniższe wartości to:"+wartosci[0]+"\n"+wartosci[1]+"\n"+wartosci[2]);


        System.out.println("Koniec");
        output.close();
    }
}
