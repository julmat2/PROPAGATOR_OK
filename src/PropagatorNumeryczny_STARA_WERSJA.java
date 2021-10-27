import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
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


public class PropagatorNumeryczny_STARA_WERSJA {
    public static void main(String[] args) throws FileNotFoundException {

//wczytuję dane z orekit-data
        File orData = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora");

        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orData));


        PrintWriter output = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_kepler_1ob.txt");
        PrintWriter output1 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_eq5_1ob.txt");
        PrintWriter output2 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_eq6_1ob.txt");
        PrintWriter output3 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_1ob.txt");

        PrintWriter output4 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_kepler_2ob.txt");
        PrintWriter output5 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_eq5_2ob.txt");
        PrintWriter output6 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_eq6_2ob.txt");
        PrintWriter output7 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/wynik_pv_2ob.txt");


//Ustawiam stan początkowy
        //wczytywanie plików
        File forces_file = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/forces_input.txt");
        File elements_file = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/elements_input.txt");
        File propagator_file = new File("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/propagator_input.txt");
        Map<String, Integer> forces_map = new HashMap<String, Integer>();
        Map<String, Double> elements_map = new HashMap<String, Double>();
        Map<String, Double> propagator_map = new HashMap<String, Double>();
        String iniData = "not declared";
        String data = "not declared";
        String data1 = "not declared";


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
            System.out.println(data);
            System.out.println(data1);
            System.out.println(iniData);
            //wypisywanie daty i mapy elementy
            System.out.println("Elementy " + elements_map);
            System.out.println("Data: " + data);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            LocalDateTime date = LocalDateTime.parse(data, formatter);
            LocalDateTime iniDate = LocalDateTime.parse(iniData, formatter);
            LocalDateTime date1 = LocalDateTime.parse(data1, formatter);

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

//            //data elementów 2 obiektu
            int year1 = date1.getYear();
            int month1 = date1.getMonthValue();
            int day1 = date1.getDayOfMonth();
            int hour1 = date1.getHour();
            int min1 = date1.getMinute();
            int sec1 = date1.getSecond();
            double miliSec1 = date1.getNano() / 1000000;
            double sekPlusMili1 = sec1 + miliSec1 / 1000.0;
//
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
            double a = elements_map.get("a");                 // semi major axis in meters
            double e = elements_map.get("e");               // eccentricity
            double i = Math.toRadians(elements_map.get("i"));        // inclination
            double omega = Math.toRadians(elements_map.get("omega"));  // perigee argument
            double raan = Math.toRadians(elements_map.get("raan"));   // right ascension of ascending node
            double lM = elements_map.get("lM");  // mean anomaly

            double mass = elements_map.get("mass"); //satellite mass
            double area = elements_map.get("area"); // satellite area

//ELEMENTY SATELITY ZBLIŻAJĄCEGO SIĘ
            double a1 = elements_map.get("a1");                 // semi major axis in meters
            double e1 = elements_map.get("e1");               // eccentricity
            double i1 = Math.toRadians(elements_map.get("i1"));        // inclination
            double omega1 = Math.toRadians(elements_map.get("omega1"));  // perigee argument
            double raan1 = Math.toRadians(elements_map.get("raan1"));   // right ascension of ascending node
            double lM1 = elements_map.get("lM1");  // mean anomaly

            double mass1 = elements_map.get("mass1"); //satellite mass
            double area1 = elements_map.get("area1"); // satellite area


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
            AbsoluteDate date1ob = new AbsoluteDate(year, month, day, hour, min, sekPlusMili1, utc);
            AbsoluteDate date2ob = new AbsoluteDate(year1, month1, day1, hour1, min1, sekPlusMili1, utc);

            double date1obToIniDate = initialDate.durationFrom(date1ob);
            double date2obToIniDate = initialDate.durationFrom(date2ob);
            System.out.println(date1obToIniDate);
            System.out.println(date2obToIniDate);

            //definiuję ramkę odniesienia
            Frame inertialFrame = FramesFactory.getEME2000();
            double mu = Constants.EGM96_EARTH_MU; //m3/s2

//            final NormalizedSphericalHarmonicsProvider gravityField = createGravityField(config.getCentralBody());


            // IERS conventions
            final IERSConventions IERSconventions = IERSConventions.IERS_2010;

            // central body
            final OneAxisEllipsoid body = new OneAxisEllipsoid(Constants.IERS2010_EARTH_EQUATORIAL_RADIUS, Constants.IERS2010_EARTH_FLATTENING, FramesFactory.getITRF(IERSconventions, true));

//definiuję początkową orbitę - Keplerian Orbit
            Orbit initialOrbit = new KeplerianOrbit(a, e, i, omega, raan, lM, PositionAngle.MEAN,
                    inertialFrame, date1ob, mu);

            //2obiekt
            Orbit initialOrbit1 = new KeplerianOrbit(a1, e1, i1, omega1, raan1, lM1, PositionAngle.MEAN,
                    inertialFrame, date2ob, mu);

            // Initial state definition
            SpacecraftState initialState = new SpacecraftState(initialOrbit);
            //2obiekt
            SpacecraftState initialState1 = new SpacecraftState(initialOrbit1);

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

            double[][] tolerances =
                    NumericalPropagator.tolerances(positionTolerance, initialOrbit, propagationType);
            AdaptiveStepsizeIntegrator integrator =
                    new DormandPrince853Integrator(minStep, maxstep, tolerances[0], tolerances[1]);

            NumericalPropagator propagator = new NumericalPropagator(integrator);
            propagator.setOrbitType(propagationType);

            propagator.setInitialState(initialState);


            //2 obiekt
            double[][] tolerances1 =
                    NumericalPropagator.tolerances(positionTolerance, initialOrbit1, propagationType);
            AdaptiveStepsizeIntegrator integrator1 =
                    new DormandPrince853Integrator(minStep, maxstep, tolerances1[0], tolerances1[1]);

            NumericalPropagator propagator1 = new NumericalPropagator(integrator1);
            propagator1.setOrbitType(propagationType);

            propagator1.setInitialState(initialState1);


//dodaję modele sił
            //GRAWITACJA

            NormalizedSphericalHarmonicsProvider provider =
                    GravityFieldFactory.getNormalizedProvider(degree, order);
            ForceModel holmesFeatherstone =
                    new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(IERSConventions.IERS_2010, true), provider);

            if (gravity_force == 1) {
                propagator.addForceModel(holmesFeatherstone);
                propagator1.addForceModel(holmesFeatherstone);
            }


            //TRZECIE CIALO
            ForceModel sun = new ThirdBodyAttraction(CelestialBodyFactory.getSun());
            if (sun_force == 1) {
                propagator.addForceModel(sun);
                propagator1.addForceModel(sun);
            }
            ForceModel moon = new ThirdBodyAttraction(CelestialBodyFactory.getMoon());
            if (moon_force == 1) {
                propagator.addForceModel(moon);
                propagator1.addForceModel(moon);
            }

            //CISNIENIE PROMIENIOWANIA SLONECZNEGO
            double cr = elements_map.get("pressureCr");
            //2obiekt
            double cr1 = elements_map.get("pressureCr1");


            SolarRadiationPressure solarRadiationPressure = new SolarRadiationPressure(CelestialBodyFactory.getSun(),
                    body.getEquatorialRadius(),
                    new IsotropicRadiationSingleCoefficient(area, cr));

            //2obiekt
            SolarRadiationPressure solarRadiationPressure1 = new SolarRadiationPressure(CelestialBodyFactory.getSun(),
                    body.getEquatorialRadius(),
                    new IsotropicRadiationSingleCoefficient(area1, cr1));

            if (SRP_force == 1) {
                propagator.addForceModel(solarRadiationPressure);
                propagator1.addForceModel(solarRadiationPressure1);
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
                propagator1.addForceModel(oceanTides);
            }


            //SILY RELATYWISTYCZNE

            //ATMOSFERA
            double cd = elements_map.get("dragCd");
            double cd1 = elements_map.get("dragCd1");

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

                //2obiekt
                DragForce dragForce1 = new DragForce(atmosphere, new IsotropicDrag(area1, cd1));
                propagator1.addForceModel(dragForce1);

            }
            if (ATMmodel == 2) {
                HarrisPriester atmosphere;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();
                atmosphere = new HarrisPriester(sun != null ? SUN : SUN, body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
                propagator.addForceModel(dragForce);

                //2obiekt
                DragForce dragForce1 = new DragForce(atmosphere, new IsotropicDrag(area1, cd1));
                propagator1.addForceModel(dragForce1);
            }

            if (ATMmodel == 3) {
                JB2008 atmosphere;

                JB2008InputParameters parameters = null;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();

                atmosphere = new JB2008(parameters, sun != null ? SUN : SUN, body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
                propagator.addForceModel(dragForce);

                //2obiekt
                DragForce dragForce1 = new DragForce(atmosphere, new IsotropicDrag(area1, cd1));
                propagator1.addForceModel(dragForce1);
            }

            if (ATMmodel == 4) {
                NRLMSISE00 atmosphere;
                NRLMSISE00InputParameters parameters = null;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();

                atmosphere = new NRLMSISE00(spaceWeatherData, CelestialBodyFactory.getSun(), body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
                propagator.addForceModel(dragForce);

                //2obiekt
                DragForce dragForce1 = new DragForce(atmosphere, new IsotropicDrag(area1, cd1));
                propagator1.addForceModel(dragForce1);
            }


            System.out.println("          date         timeFromStart     a           e" +
                    "           i         \u03c9          \u03a9" +
                    "          M");
            class TutorialStepHandler implements OrekitFixedStepHandler {

//            public void init(final SpacecraftState s0, final AbsoluteDate t) {
//                System.out.println("          date                a           e" +
//                        "           i         \u03c9          \u03a9" +
//                        "          \u03bd");
//            }

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
                            o.getA(), o.getE(),
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


//                    if (isLast) {
////                        System.out.println("that was the last step ");
////                        System.out.println();
////                        output.append("that was the last step ");
//                    }

                }

            }
            double durationTime1 = durationTime + date1obToIniDate;
            propagator.setMasterMode(durationTime1, new TutorialStepHandler());
            SpacecraftState finalState = propagator.propagate(new AbsoluteDate(initialDate, propagationTime));

            //2obiekt

            System.out.println("          date         timeFromStart     a           e" +
                    "           i         \u03c9          \u03a9" +
                    "          M");
            class TutorialStepHandler1 implements OrekitFixedStepHandler {

//            public void init(final SpacecraftState s0, final AbsoluteDate t) {
//                System.out.println("          date                a           e" +
//                        "           i         \u03c9          \u03a9" +
//                        "          \u03bd");
//            }

                public void handleStep(SpacecraftState currentState, boolean isLast) {

                    KeplerianOrbit o = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(currentState.getOrbit());

                    System.out.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date2ob),
                            o.getA(), o.getE(),
                            FastMath.toDegrees(o.getI()),
                            FastMath.toDegrees(o.getPerigeeArgument()),
                            FastMath.toDegrees(o.getRightAscensionOfAscendingNode()),
                            FastMath.toDegrees(MathUtils.normalizeAngle(o.getMeanAnomaly(), FastMath.PI)));


                    output4.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date2ob),
                            o.getA(), o.getE(),
                            FastMath.toDegrees(o.getI()),
                            FastMath.toDegrees(o.getPerigeeArgument()),
                            FastMath.toDegrees(o.getRightAscensionOfAscendingNode()),
                            FastMath.toDegrees(MathUtils.normalizeAngle(o.getMeanAnomaly(), FastMath.PI)));

                    output5.format(Locale.US, "%s %12.8f %23.16e %23.16e %23.16e %23.16e %23.16e%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date2ob),
                            o.getEquinoctialEy(), // h
                            o.getEquinoctialEx(), // k
                            o.getHy(),            // p
                            o.getHx(),            // q
                            FastMath.toDegrees(MathUtils.normalizeAngle(o.getLM(), FastMath.PI)));

                    output6.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date2ob),
                            currentState.getOrbit().getA(),
                            currentState.getOrbit().getEquinoctialEy(), // h
                            currentState.getOrbit().getEquinoctialEx(), // k
                            currentState.getOrbit().getHy(),            // p
                            currentState.getOrbit().getHx(),            // q
                            FastMath.toDegrees(MathUtils.normalizeAngle(currentState.getOrbit().getLM(), FastMath.PI)));

                    final PVCoordinates pv = currentState.getPVCoordinates();
                    output7.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(date2ob),
                            pv.getPosition().getX() * 0.001, //"position along X (km)"
                            pv.getPosition().getY() * 0.001, //"position along Y (km)"
                            pv.getPosition().getZ() * 0.001, //"position along Y (km)"
                            pv.getVelocity().getX() * 0.001, //"velocity along X (km/s)"
                            pv.getVelocity().getY() * 0.001, //"velocity along Y (km/s)"
                            pv.getVelocity().getZ() * 0.001); //"velocity along Z (km/s)"


                }

            }
            double durationTime2 = durationTime + date2obToIniDate;
            propagator1.setMasterMode(durationTime2, new TutorialStepHandler());
            SpacecraftState finalState1 = propagator1.propagate(new AbsoluteDate(initialDate, propagationTime));


            output.close();
            output1.close();
            output2.close();
            output3.close();
            output4.close();
            output5.close();
            output6.close();
            output7.close();

        }


    }
}


