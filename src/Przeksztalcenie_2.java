import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Scanner;

public class Przeksztalcenie_2 {
    public static void main(String[] args) throws FileNotFoundException {
        File orData = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora");

        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orData));


        File plik1 = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/kat_tle.txt");
        PrintWriter outputTLE = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/2_TLE.out");
        PrintWriter output = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/kepler_sr.out");
        PrintWriter output1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/PV_sr.out");

        PrintWriter outputLEO1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/LEO_kepler_sr.out");
        PrintWriter outputLEO2 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/LEO_PV_sr.out");

        PrintWriter outputMEO1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/MEO_kepler_sr.out");
        PrintWriter outputMEO2 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/MEO_PV_sr.out");

        PrintWriter outputGEO1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/GEO_kepler_sr.out");
        PrintWriter outputGEO2 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/GEO_PV_sr.out");

        PrintWriter outputHEO1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/HEO_kepler_sr.out");
        PrintWriter outputHEO2 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/HEO_PV_sr.out");

        Scanner scanner = new Scanner(plik1);

        int i = 0, j = 0, k = 0, l = 0, m = 0;

        while (scanner.hasNextLine()) {
            String tle_0 = scanner.nextLine();
            String tle_1 = scanner.nextLine();
            String tle_2 = scanner.nextLine();
            if (!tle_1.startsWith("1 T")) {
                TLE tle = new TLE(tle_1, tle_2);


                double mu = 398600.4415; //[km**3/s**2]
                double aE = 6378.1363; //[km]
                double pi = Math.atan(1.0) * 4.0;
                pi = Math.PI;
                double pi2 = 2.0 * pi;
                double rd = 180.0 / pi;


                Frame inertialFrame = FramesFactory.getEME2000();


                AbsoluteDate date = tle.getDate();

//wyliczam polos wielką
                double n = tle.getMeanMotion();
                double I = tle.getI() / rd;
                double e = tle.getE();

                //STOP
                double C20jgm = 0.48416955E-3;
//double k2=C20jgm;
                double C20egm96 = Constants.EGM96_EARTH_C20;
           double     k2=C20egm96;
//                double J2 = -1.0 * C20jgm * Math.sqrt(5.0);

//            double a1 = Math.pow((mu / (Math.pow(n, 2.0))), (1.0 / 3.0));
//            double d1 = (0.75 * J2 * Math.pow(aE / a1, 2.0) * (3.0 * (Math.cos(I) * Math.cos(I)) - 1.0)) / Math.pow((1.0 - (e * e)), (2.0 / 3.0));
//            double a = a1 * (1.0 - (d1 / 3.0) - (d1 * d1) - ((134.0 / 81.0) * d1 * d1 * d1));

                //SGP4 celestrack

                mu = Constants.IERS2010_EARTH_MU / Math.pow(10.0, 9); //[km3/s2]
                double ke = Math.sqrt(mu);
                double a1 = Math.pow((ke / n), (2.0 / 3.0));
// k2 to jest współczynnik C20 z modelu pola grawitacyjnego?
               // double k2 = 1.0 / 2.0 * J2 * (aE * aE);
 //               double k2 = 5.413080E-4;  //wziete z opisu SGP4

                double d1 = 3.0 / 2.0 * (k2 / (a1 * a1)) * ((3.0 * Math.cos(I) * Math.cos(I)) - 1.0) / Math.pow((1.0 - (e * e)), (3.0 / 2.0));

                double a0 = a1 * (1.0 - (1.0 / 3.0 * d1) - (d1 * d1) - (134.0 / 81.0 * d1 * d1 * d1));
                double d0 = 3.0 / 2.0 * (k2 / (a0 * a0)) * ((3.0 * Math.cos(I) * Math.cos(I)) - 1.0) / Math.pow((1.0 - (e * e)), (3.0 / 2.0));
                double n0 = n / (1.0 + d0);
                double a = a0 / (1.0 - d0);


                double ha = (a * (1.0 + e)) - aE;
                double hp = (a * (1.0 - e)) - aE;
                Orbit orbit = new KeplerianOrbit(a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()), PositionAngle.MEAN,
                        inertialFrame, tle.getDate(), mu);

                PVCoordinates pv = orbit.getPVCoordinates();

                Vector3D position = pv.getPosition();
                Vector3D vel = pv.getVelocity();
                i++;
                outputTLE.write(tle_1+"\n"+tle_2+"\n");
                output.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), ha, hp, a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                output1.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), ha, hp, position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());


            //LEO
            if (tle.getE() <= 0.25 && a <= (6371.0 + 2000.0)) {
                j++;
                outputLEO1.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), ha, hp, a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                outputLEO2.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), ha, hp, position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());

            }
            //MEO
            else if (tle.getE() <= 0.25 && a > (6371.0 + 2000.0) && a <= (42164.0 - 1000.0)) {
                k++;
                outputMEO1.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), ha, hp, a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                outputMEO2.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), ha, hp, position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());

            }
            //GEO
            else if (tle.getE() <= 0.25 && a > (42164.0 - 1000.0)) {
                l++;
                outputGEO1.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), ha, hp, a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                outputGEO2.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), ha, hp, position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());

            }
            //HEO
            else if (tle.getE() > 0.25) {
                m++;
                outputHEO1.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), ha, hp, a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                outputHEO2.format(Locale.US, "%s %s %10.8f %10.8f %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), ha, hp, position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());

            }

            } else break;
        }
        System.out.println("All: " + i);
        System.out.println("LEO: " + j);
        System.out.println("MEO: " + k);
        System.out.println("GEO: " + l);
        System.out.println("HEO: " + m);

        outputTLE.close();
        output.close();
        output1.close();
        outputLEO1.close();
        outputLEO2.close();
        outputMEO1.close();
        outputMEO2.close();
        outputGEO1.close();
        outputGEO2.close();
        outputHEO1.close();
        outputHEO2.close();

    }
}
