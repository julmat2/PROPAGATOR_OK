import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.ForceModel;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.IsotropicDrag;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.OceanTides;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.radiation.IsotropicRadiationSingleCoefficient;
import org.orekit.forces.radiation.SolarRadiationPressure;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.models.earth.atmosphere.*;
import org.orekit.models.earth.atmosphere.data.CssiSpaceWeatherData;
import org.orekit.models.earth.atmosphere.data.MarshallSolarActivityFutureEstimation;
import org.orekit.orbits.*;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.AltitudeDetector;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.PVCoordinatesProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;


public class PropagatorNumeryczny_REENTRY {
    public static void main(String[] args) throws FileNotFoundException {

//wczytuję dane z orekit-data
        File orData = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora");

        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orData));


        PrintWriter output = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_kepler_RE.txt");
        PrintWriter output1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_eq5_RE.txt");
        PrintWriter output2 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_eq6_RE.txt");
        PrintWriter output3 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_RE.txt");


//Ustawiam stan początkowy
        //wczytywanie plików
        File forces_file = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/forces_input_RE.txt");
        File elements_file = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/elements_input_RE.txt");
        File propagator_file = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/propagator_input_RE.txt");
        Map<String, Integer> forces_map = new HashMap<String, Integer>();
        Map<String, Double> elements_map = new HashMap<String, Double>();
        Map<String, Double> propagator_map = new HashMap<String, Double>();
        String iniData = "not declared";
        String data = "not declared";
        String data1 = "not declared";

/////////////PROPAGACJA OBIEKTU GłÓWNEGO////////////////////////////////////////////////////////////
        if (forces_file.exists() && elements_file.exists() && propagator_file.exists()) {
            //forces
            Scanner forces = new Scanner(forces_file);
            while (forces.hasNextLine()) {
                //mapka string i int
                String linia = forces.nextLine();
                if (linia.charAt(0) != '#' && linia.charAt(0) != '\n') {
                    String[] dane = Pliki.separate_string(linia, ": ");
                    forces_map.put(dane[0], Integer.parseInt(dane[1]));
                }
            }
            System.out.println("Siły " + forces_map);

            Scanner propConfig = new Scanner(propagator_file);
            while (propConfig.hasNextLine()) {
                //mapka string i double
                String linia2 = propConfig.nextLine();
                if (linia2.charAt(0) != '#' && linia2.charAt(0) != '\n') {
                    String[] dane2 = Pliki.separate_string(linia2, ": ");
                    propagator_map.put(dane2[0], Double.parseDouble(dane2[1]));
                }
            }
            System.out.println("Propagator konfiguracja" + propagator_map);

            //elements tłumaczę i objaśniam co tu następuje
            Scanner elements = new Scanner(elements_file); //wiadomo
            while (elements.hasNextLine()) {
                String linia1 = elements.nextLine(); //ppobiera linię
                if (linia1.length() != 0 && linia1.charAt(0) != '#') {
                    //pierwsza część warunku sprawdza czy linia jest pusta
                    //druga część warunku sprawdza czy linia nie zaczyna się od '#'

                    //System.out.println(linia1); // sprawdzenie jak wygląda linia w pliku

                    String[] dane1 = Pliki.separate_string(linia1, ": "); //rozdzielenie linii na tablicę stringów

                    //System.out.println("1: '"+dane1[0]+"' 2: '"+dane1[1]+"'"); //sprawdzenie jak wygląda tablica Stringów

                    if (!dane1[0].equals("date") && !dane1[0].equals("initialDate") && !dane1[0].equals("date1")) {
                        //jeśli wartość pierwszego elementu stringa to nie jest date -> zapisuje do mapy
                        //System.out.println(dane1[0]);
                        elements_map.put(dane1[0], Double.parseDouble(dane1[1]));
                    } else {
                        //jeśli to jest 'date' -> zapisuje do zmiennej date
                        if (dane1[0].equals("date")) {
                            data = dane1[1];
                        } else if (dane1[0].equals("date1")) {
                            data1 = dane1[1];
                        } else if (dane1[0].equals("initialDate")) {
                            iniData = dane1[1];
                        }
                    }


                }
            }

            //wypisywanie daty i mapy elementy
            System.out.println("Elementy " + elements_map);
            System.out.println("Data: " + data);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            LocalDateTime date = LocalDateTime.parse(data, formatter);
            LocalDateTime iniDate = LocalDateTime.parse(iniData, formatter);


            //Przypisanie wartości elementom
            //data elementów 1 obiektu
            int year = date.getYear();
            int month = date.getMonthValue();
            int day = date.getDayOfMonth();
            int hour = date.getHour();
            int min = date.getMinute();
            int sec = date.getSecond();
            double miliSec = date.getNano() / 1000000;
            double sekPlusMili = sec + miliSec / 1000.0;


//            //data początku propagacji
            int iniyear = iniDate.getYear();
            int inimonth = iniDate.getMonthValue();
            int iniday = iniDate.getDayOfMonth();
            int inihour = iniDate.getHour();
            int inimin = iniDate.getMinute();
            int inisec = iniDate.getSecond();
            double inimiliSec = iniDate.getNano() / 1000000;
            double inisekPlusMili = inisec + inimiliSec / 1000.0;

//ELEMENTY OBIEKTU GłÓWNEGO
            double type = elements_map.get("type");


            //Przypisanie wartości sił

            int gravity_force = forces_map.get("gravity");
            int degree = forces_map.get("GMdegree");
            int order = forces_map.get("GMorder");
            int sun_force = forces_map.get("sun");
            int moon_force = forces_map.get("moon");
            int atmosphere_force = forces_map.get("atmosphere");
            int ATMmodel = forces_map.get("ATMmodel");
            int SRP_force = forces_map.get("SRP");
            int OT_force = forces_map.get("OT");
            int RAcc_force = forces_map.get("RAcc");


            TimeScale utc = TimeScalesFactory.getUTC();
            //AbsoluteDate initialDate = new AbsoluteDate(year, month, day, hour, min, sekPlusMili, utc);
            AbsoluteDate initialDate = new AbsoluteDate(iniyear, inimonth, iniday, inihour, inimin, inisekPlusMili, utc);
            AbsoluteDate date1ob = new AbsoluteDate(year, month, day, hour, min, sekPlusMili, utc);


            double date1obToIniDate = initialDate.durationFrom(date1ob);

            System.out.println(date1obToIniDate);


            //definiuję ramkę odniesienia
            Frame inertialFrame = FramesFactory.getEME2000();
            double mu = Constants.EGM96_EARTH_MU; //m3/s2

//            final NormalizedSphericalHarmonicsProvider gravityField = createGravityField(config.getCentralBody());


            // IERS conventions
            final IERSConventions IERSconventions = IERSConventions.IERS_2010;

            // central body
            final OneAxisEllipsoid body = new OneAxisEllipsoid(Constants.IERS2010_EARTH_EQUATORIAL_RADIUS, Constants.IERS2010_EARTH_FLATTENING, FramesFactory.getITRF(IERSconventions, true));


//definiuję początkową orbitę - Keplerian Orbit
            Orbit initialOrbit = null;
            SpacecraftState initialState = null;
            double area = 0;
            if (type == 1) {
                double a = elements_map.get("a");                 // semi major axis in meters
                double e = elements_map.get("e");               // eccentricity
                double i = Math.toRadians(elements_map.get("i"));        // inclination
                double omega = Math.toRadians(elements_map.get("omega"));  // perigee argument
                double raan = Math.toRadians(elements_map.get("raan"));   // right ascension of ascending node
                double lM = elements_map.get("lM");  // mean anomaly

                double mass = elements_map.get("mass"); //satellite mass
                area = elements_map.get("area");

                initialOrbit = new KeplerianOrbit(a, e, i, omega, raan, lM, PositionAngle.MEAN,
                        inertialFrame, date1ob, mu);


                // Initial state definition
                initialState = new SpacecraftState(initialOrbit);

            }
            //definiuję początkową orbitę - Cartesian Orbit
            else if (type == 2) {
                double x = elements_map.get("x");                 // semi major axis in meters
                double y = elements_map.get("y");               // eccentricity
                double z = elements_map.get("z");        // inclination
                double vx = elements_map.get("vx");  // perigee argument
                double vy = elements_map.get("vy");   // right ascension of ascending node
                double vz = elements_map.get("vz");  // mean anomaly

                double mass = elements_map.get("mass"); //satellite mass
                area = elements_map.get("area");

                //initialOrbit = new CartesianOrbit(x,y,z,vx,vy,vz,inertialFrame, date1ob, mu);
                Vector3D position = new Vector3D(x, y, z);
                Vector3D velocity = new Vector3D(vx, vy, vz);
                PVCoordinates pv = new PVCoordinates(position, velocity);
                initialOrbit = new CartesianOrbit(pv, inertialFrame, date1ob, mu);

                // Initial state definition
                initialState = new SpacecraftState(initialOrbit);
            }
//Definiuję propagator jako Numeric Propagator
            // Adaptive step integrator
// with a minimum step of 0.001 and a maximum step of 1000
            double minStep = propagator_map.get("minStep");
            double maxstep = propagator_map.get("maxStep");
            double positionTolerance = propagator_map.get("positionTolerance");
            double durationTime = propagator_map.get("durationTime"); //sekundy
            double propagationTime = propagator_map.get("propagationTime"); //sekundy
            //OrbitType propagationType = OrbitType.KEPLERIAN;
            OrbitType propagationType = OrbitType.CARTESIAN;
            propagationTime = propagationTime + date1obToIniDate;
            double[][] tolerances =
                    NumericalPropagator.tolerances(positionTolerance, initialOrbit, propagationType);
            AdaptiveStepsizeIntegrator integrator =
                    new DormandPrince853Integrator(minStep, maxstep, tolerances[0], tolerances[1]);

            NumericalPropagator propagator = new NumericalPropagator(integrator);
//            propagator.addEventDetector(new AltitudeDetector(0.0,body));
            propagator.setOrbitType(propagationType);

            propagator.setInitialState(initialState);




//dodaję modele sił
            //GRAWITACJA

            NormalizedSphericalHarmonicsProvider provider =
                    GravityFieldFactory.getNormalizedProvider(degree, order);
            ForceModel holmesFeatherstone =
                    new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(IERSConventions.IERS_2010, true), provider);

            if (gravity_force == 1) {
                propagator.addForceModel(holmesFeatherstone);

            }


            //TRZECIE CIALO
            ForceModel sun = new ThirdBodyAttraction(CelestialBodyFactory.getSun());
            if (sun_force == 1) {
                propagator.addForceModel(sun);

            }
            ForceModel moon = new ThirdBodyAttraction(CelestialBodyFactory.getMoon());
            if (moon_force == 1) {
                propagator.addForceModel(moon);

            }

            //CISNIENIE PROMIENIOWANIA SLONECZNEGO
            double cr = elements_map.get("pressureCr");

            SolarRadiationPressure solarRadiationPressure = new SolarRadiationPressure(CelestialBodyFactory.getSun(),
                    body.getEquatorialRadius(),
                    new IsotropicRadiationSingleCoefficient(area, cr));


            if (SRP_force == 1) {
                propagator.addForceModel(solarRadiationPressure);

            }


            //PLYWY OCEANICZNE

            OceanTides oceanTides = new OceanTides(FramesFactory.getITRF(IERSconventions, true), Constants.IERS2010_EARTH_EQUATORIAL_RADIUS,
                    Constants.IERS2010_EARTH_MU,
                    degree,
                    order,
                    IERSconventions,
                    TimeScalesFactory.getUT1(IERSconventions, true));

            if (OT_force == 1) {
                propagator.addForceModel(oceanTides);

            }


            //SILY RELATYWISTYCZNE

            //ATMOSFERA
            double cd = elements_map.get("dragCd");


            int atmosphereConfiguration = ATMmodel;
            CssiSpaceWeatherData spaceWeatherData = new CssiSpaceWeatherData("SpaceWeather-All-v1.2.txt");


            if (ATMmodel == 1) {
                DTM2000 atmosphere;
                DTM2000InputParameters parameters = new MarshallSolarActivityFutureEstimation(
                        MarshallSolarActivityFutureEstimation.DEFAULT_SUPPORTED_NAMES,
                        MarshallSolarActivityFutureEstimation.StrengthLevel.AVERAGE);

                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();
                //atmosphere = new DTM2000(parameters, SUN != null ? SUN : SUN, body);
                atmosphere = new DTM2000(spaceWeatherData, SUN != null ? SUN : SUN, body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
                propagator.addForceModel(dragForce);


            }
            if (ATMmodel == 2) {
                HarrisPriester atmosphere;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();
                atmosphere = new HarrisPriester(sun != null ? SUN : SUN, body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
                propagator.addForceModel(dragForce);


            }

            if (ATMmodel == 3) {
                JB2008 atmosphere;

                JB2008InputParameters parameters = null;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();

                atmosphere = new JB2008(parameters, sun != null ? SUN : SUN, body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
                propagator.addForceModel(dragForce);


            }

            if (ATMmodel == 4) {
                NRLMSISE00 atmosphere;
                NRLMSISE00InputParameters parameters = null;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();

                atmosphere = new NRLMSISE00(spaceWeatherData, CelestialBodyFactory.getSun(), body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
                propagator.addForceModel(dragForce);


            }

            System.out.println("          date         timeFromStart     a           e" +
                    "           i         \u03c9          \u03a9" +
                    "          M");
            class TutorialStepHandler implements OrekitFixedStepHandler {


                public void handleStep(SpacecraftState currentState, boolean isLast) {

                    KeplerianOrbit o = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(currentState.getOrbit());

                    System.out.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date1ob),
                            o.getA(), o.getE(),
                            FastMath.toDegrees(o.getI()),
                            FastMath.toDegrees(o.getPerigeeArgument()),
                            FastMath.toDegrees(o.getRightAscensionOfAscendingNode()),
                            FastMath.toDegrees(MathUtils.normalizeAngle(o.getMeanAnomaly(), FastMath.PI)));


                    output.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date1ob),
                            o.getA(),
                            o.getE(),
                            FastMath.toDegrees(o.getI()),
                            FastMath.toDegrees(o.getPerigeeArgument()),
                            FastMath.toDegrees(o.getRightAscensionOfAscendingNode()),
                            FastMath.toDegrees(MathUtils.normalizeAngle(o.getMeanAnomaly(), FastMath.PI)));


                    output1.format(Locale.US, "%s %12.8f %23.16e %23.16e %23.16e %23.16e %23.16e%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date1ob),
                            o.getEquinoctialEy(), // h
                            o.getEquinoctialEx(), // k
                            o.getHy(),            // p
                            o.getHx(),            // q
                            FastMath.toDegrees(MathUtils.normalizeAngle(o.getLM(), FastMath.PI)));


                    output2.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date1ob),
                            currentState.getOrbit().getA(),
                            currentState.getOrbit().getEquinoctialEy(), // h
                            currentState.getOrbit().getEquinoctialEx(), // k
                            currentState.getOrbit().getHy(),            // p
                            currentState.getOrbit().getHx(),            // q
                            FastMath.toDegrees(MathUtils.normalizeAngle(currentState.getOrbit().getLM(), FastMath.PI)));


                    final PVCoordinates pv = currentState.getPVCoordinates();
                    output3.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date1ob),
                            pv.getPosition().getX() * 0.001, //"position along X (km)"
                            pv.getPosition().getY() * 0.001, //"position along Y (km)"
                            pv.getPosition().getZ() * 0.001, //"position along Y (km)"
                            pv.getVelocity().getX() * 0.001, //"velocity along X (km/s)"
                            pv.getVelocity().getY() * 0.001, //"velocity along Y (km/s)"
                            pv.getVelocity().getZ() * 0.001); //"velocity along Z (km/s)"


                }

            }

//            AltitudeDetector altitudeDetector = new AltitudeDetector(0.0, body);
//            propagator.addEventDetector(altitudeDetector);


            propagator.setMasterMode(durationTime, new TutorialStepHandler());
            //propagator.addEventDetector(new AltitudeDetector(0.0,body));
            SpacecraftState finalState = propagator.propagate(new AbsoluteDate(date1ob, propagationTime));

            output.close();
            output1.close();
            output2.close();
            output3.close();


        }

    }

}



