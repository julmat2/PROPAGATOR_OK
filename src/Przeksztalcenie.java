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
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Scanner;

public class Przeksztalcenie {
    public static void main(String[] args) throws FileNotFoundException {
        File orData = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora");

        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orData));


        File plik1 = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/kat_tle.txt");
        PrintWriter output = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/kepler_sr.out");
        PrintWriter output1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/PV_sr.out");
        PrintWriter output1osc = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/PV_osc.out");

        PrintWriter output2 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/HEO_kepler_sr.out");
        PrintWriter output3 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/HEO_PV_sr.out");

        PrintWriter output4 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/LEO_kepler_sr.out");
        PrintWriter output5 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/LEO_PV_sr.out");

        PrintWriter output6 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/MEO_kepler_sr.out");
        PrintWriter output7 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/MEO_PV_sr.out");

        PrintWriter output6a = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/lowMEO_kepler_sr.out");
        PrintWriter output7a = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/lowMEO_PV_sr.out");

        PrintWriter output6b = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/highMEO_kepler_sr.out");
        PrintWriter output7b = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/highMEO_PV_sr.out");

        PrintWriter output8 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/GEO_kepler_sr.out");
        PrintWriter output9 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/przeksztalcenie/GEO_PV_sr.out");

        Scanner scanner = new Scanner(plik1);

        int i = 0, j = 0, k = 0, p = 0, l = 0;
        int m = 0, o = 0;
        while (scanner.hasNextLine()) {

            String tle_1 = scanner.nextLine();
            String tle_2 = scanner.nextLine();
            TLE tle = new TLE(tle_1, tle_2);
            TLEPropagator propagatorTle = TLEPropagator.selectExtrapolator(tle);


            AbsoluteDate date = propagatorTle.getInitialState().getDate();
            Vector3D osc_pos = propagatorTle.getInitialState().getOrbit().getPVCoordinates().getPosition();
            Vector3D osc_vel = propagatorTle.getInitialState().getOrbit().getPVCoordinates().getVelocity();

            double mu = Constants.EGM96_EARTH_MU; //m3/s2
            Frame inertialFrame = FramesFactory.getEME2000();
            double J2 =-1.0* Constants.EGM96_EARTH_C20*Math.sqrt(5.0);
            double aE = Constants.EGM96_EARTH_EQUATORIAL_RADIUS;//m
            double k2=1.0/2.0*J2*Math.pow(aE,2.0);
            //double k2 = 5.413080E-4;

//wyliczam polos wielką
            double n = tle.getMeanMotion();
            double dziel = mu / (n * n);
            double a1 = Math.pow(dziel, (2.0/3.0)); //m

            double d_1= 3.0/2.0 * k2/Math.pow(a1,2.0);
            double d_2=3.0* Math.pow(Math.pow(Math.cos(Math.toRadians(tle.getI())),2.0),2.0) -1.0;
            double d_3=Math.pow((1.0-Math.pow(tle.getE(),2.0)),(3.0/2.0));
            double del1 =d_1*d_2/d_3;

            double a0= a1*(1.0-(1.0/3.0*del1)-(Math.pow(del1,2.0))-(134.0/81.0*Math.pow(del1,3.0)));
            double del0= 3.0/2.0 * k2/Math.pow(a0,2.0)* d_2/d_3;

            double n0= n/(1.0+del0);
            double a=a0/(1.0-del0);


            double ha = ((a * (1.0 + tle.getE())) / 1000.0) - Constants.IERS2010_EARTH_EQUATORIAL_RADIUS / 1000.0;
            double hp = ((a * (1.0 - tle.getE())) / 1000.0) - Constants.IERS2010_EARTH_EQUATORIAL_RADIUS / 1000.0;
            Orbit orbit = new KeplerianOrbit(a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                    FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()), PositionAngle.MEAN,
                    inertialFrame, tle.getDate(), mu);

            PVCoordinates pv = orbit.getPVCoordinates();

            Vector3D position = pv.getPosition();
            Vector3D vel = pv.getVelocity();
            i++;
            output.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                    tle.getSatelliteNumber(), tle.getDate(), a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                    FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

            output1.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());
            output1osc.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), date, osc_pos.getX(), osc_pos.getY(), osc_pos.getZ(), osc_vel.getX(), osc_vel.getY(), osc_vel.getZ());

            //System.out.println(ha);
            //HEO
            if (tle.getE() >= 0.1) {
                j++;
                output2.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                output3.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());

            }
            //MEO
            else if (ha >= 2000 && ha < 34000 && tle.getE() < 0.1) {
                p++;
                output6.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                output7.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());
                //low MEO
                if (ha >= 2000 && ha < 16000 && tle.getE() < 0.1) {
                    m++;
                    output6a.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            tle.getSatelliteNumber(), tle.getDate(), a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                            FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                    output7a.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());
                }
                //high MEO
                else if (ha >= 16000 && ha < 34000 && tle.getE() < 0.1) {
                    o++;
                    output6b.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            tle.getSatelliteNumber(), tle.getDate(), a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                            FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                    output7b.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());
                }

            }
            //GEO
            else if (ha >= 34000 && ha < 36000 && tle.getE() < 0.1) {
                l++;
                output8.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                output9.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());

            }

            //LEO
            else if (ha < 2000 && tle.getE() < 0.1) {
                k++;
                output4.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                        tle.getSatelliteNumber(), tle.getDate(), a, tle.getE(), FastMath.toDegrees(tle.getI()), FastMath.toDegrees(tle.getPerigeeArgument()),
                        FastMath.toDegrees(tle.getRaan()), FastMath.toDegrees(tle.getMeanAnomaly()));

                output5.format(Locale.US, "%s %s %10.8f %10.8f %10.6f %10.6f %10.6f %10.6f%n", tle.getSatelliteNumber(), tle.getDate(), position.getX(), position.getY(), position.getZ(), vel.getX(), vel.getY(), vel.getZ());

            }
        }
        System.out.println("All: " + i);
        System.out.println("HEO: " + j);
        System.out.println("LEO: " + k);
        System.out.println("MEO: " + p);
        System.out.println("low MEO: " + m);
        System.out.println("high MEO: " + o);
        System.out.println("GEO: " + l);

        output.close();
        output1.close();
        output2.close();
        output3.close();
        output4.close();
        output5.close();
        output6.close();
        output6a.close();
        output6b.close();
        output7.close();
        output7a.close();
        output7b.close();
        output8.close();
        output9.close();


    }
}
