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
import org.orekit.propagation.analytical.tle.SGP4;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.numerical.NumericalPropagator;
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


public class PropagatorNumeryczny_Zblizenia_TLE {
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
        PrintWriter output8 = new PrintWriter("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/distances.txt");

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
        String line1_1 = "not declared";
        String line2_1 = "not declared";
        String line1_2 = "not declared";
        String line2_2 = "not declared";

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
                String linia1 = elements.nextLine(); //pobiera linię
                if (linia1.length() != 0 && linia1.charAt(0) != '#') {
                    //pierwsza część warunku sprawdza czy linia jest pusta
                    //druga część warunku sprawdza czy linia nie zaczyna się od '#'

                    //System.out.println(linia1); // sprawdzenie jak wygląda linia w pliku

                    String[] dane1 = Pliki.separate_string(linia1, ": "); //rozdzielenie linii na tablicę stringów

                    //System.out.println("1: '"+dane1[0]+"' 2: '"+dane1[1]+"'"); //sprawdzenie jak wygląda tablica Stringów

                    if (!dane1[0].equals("date") && !dane1[0].equals("initialDate") && !dane1[0].equals("date1") && !dane1[0].equals("line1_1") && !dane1[0].equals("line2_1") && !dane1[0].equals("line1_2") && !dane1[0].equals("line2_2")) {
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
                        } else if (dane1[0].equals("line1_1")) {
                            line1_1 = dane1[1];
                        } else if (dane1[0].equals("line2_1")) {
                            line2_1 = dane1[1];
                        } else if (dane1[0].equals("line1_2")) {
                            line1_2 = dane1[1];
                        } else if (dane1[0].equals("line2_2")) {
                            line2_2 = dane1[1];
                        }
                    }


                }
            }


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

            LocalDateTime iniDate = LocalDateTime.parse(iniData, formatter);


//            //data początku propagacji
            int iniyear = iniDate.getYear();
            int inimonth = iniDate.getMonthValue();
            int iniday = iniDate.getDayOfMonth();
            int inihour = iniDate.getHour();
            int inimin = iniDate.getMinute();
            int inisec = iniDate.getSecond();
            double inimiliSec = iniDate.getNano() / 1000000;
            double inisekPlusMili = inisec + inimiliSec / 1000.0;


            double type = elements_map.get("type");

            //Przypisanie wartości sił

            int gravity_force = forces_map.get("gravity");
            int gravity_model = forces_map.get("gravity_model");
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

            AbsoluteDate initialDate = new AbsoluteDate(iniyear, inimonth, iniday, inihour, inimin, inisekPlusMili, utc);


            //definiuję ramkę odniesienia
            Frame inertialFrame = FramesFactory.getEME2000();
            double mu = Constants.EGM96_EARTH_MU; //m3/s2


            // IERS conventions
            final IERSConventions IERSconventions = IERSConventions.IERS_2010;

            // central body
            final OneAxisEllipsoid body = new OneAxisEllipsoid(Constants.IERS2010_EARTH_EQUATORIAL_RADIUS, Constants.IERS2010_EARTH_FLATTENING, FramesFactory.getITRF(IERSconventions, true));

            AbsoluteDate date1ob = null;
            AbsoluteDate date2ob = null;
            double area = 0.0;
            double mass = 0.0;
            double area1 = 0.0;
            double mass1 = 0.0;
            double cr = 0.0;
            double cr1 = 0.0;
            double cd = 0.0;
            double cd1 = 0.0;

//ELEMENTY OBIEKTU GłÓWNEGO
//definiuję początkową orbitę - Keplerian Orbit
            Orbit initialOrbit = null;
            SpacecraftState initialState = null;
            area = 0;

            //definiuję początkową orbitę - TLE

                TLE tle1 = new TLE(line1_1, line2_1);
                TLEPropagator propagatorTle = TLEPropagator.selectExtrapolator(tle1);
                mass = elements_map.get("mass"); //satellite mass
                area = elements_map.get("area");
                cr = elements_map.get("pressureCr");
                cd = elements_map.get("dragCd");

                date1ob = tle1.getDate();
                initialState =  SGP4.selectExtrapolator(tle1).getInitialState();;

                initialOrbit = initialState.getOrbit();
            double date1obToIniDate = initialDate.durationFrom(date1ob);

            System.out.println(date1obToIniDate);

            //DEFINIUJĘ ORBITĘ KANDYDATA
            Orbit initialOrbit1 = null;
            SpacecraftState initialState1 = null;

                TLE tle2 = new TLE(line1_2, line2_2);

                TLEPropagator propagatorTle1 = TLEPropagator.selectExtrapolator(tle2);
                mass1 = elements_map.get("mass1"); //satellite mass
                area1 = elements_map.get("area1");
                cr1 = elements_map.get("pressureCr1");
                cd1 = elements_map.get("dragCd1");

                date2ob = tle2.getDate();
                initialState1 =  SGP4.selectExtrapolator(tle2).getInitialState();;

                initialOrbit1 = initialState1.getOrbit();
            double date2obToIniDate = initialDate.durationFrom(date2ob);

            System.out.println(date2obToIniDate);


            //Definiuję propagator jako Numeric Propagator
            // Adaptive step integrator
// with a minimum step of 0.001 and a maximum step of 1000
            double minStep = propagator_map.get("minStep");
            double maxstep = propagator_map.get("maxStep");
            double positionTolerance = propagator_map.get("positionTolerance");
            double durTime = propagator_map.get("durationTime"); //sekundy
            double propagationTime = propagator_map.get("propagationTime"); //sekundy
            //OrbitType propagationType = OrbitType.KEPLERIAN;
            OrbitType propagationType = OrbitType.CARTESIAN;


            double[][] tolerances =
                    NumericalPropagator.tolerances(positionTolerance, initialOrbit, propagationType);
            double[][] tolerances1 =
                    NumericalPropagator.tolerances(positionTolerance, initialOrbit1, propagationType);

            AdaptiveStepsizeIntegrator integrator =
                    new DormandPrince853Integrator(minStep, maxstep, tolerances[0], tolerances[1]);
            AdaptiveStepsizeIntegrator integrator1 =
                    new DormandPrince853Integrator(minStep, maxstep, tolerances1[0], tolerances1[1]);

            TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle1);
            TLEPropagator propagator1 = TLEPropagator.selectExtrapolator(tle2);
//            NumericalPropagator propagator1 = new NumericalPropagator(integrator1);
//            propagator.setOrbitType(propagationType);
//            propagator1.setOrbitType(propagationType);
//
//            propagator.setInitialState(initialState);
//            propagator1.setInitialState(initialState1);

//dodaję modele sił
            //GRAWITACJA
            NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(degree, order);

            ForceModel holmesFeatherstone =
                    new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(IERSConventions.IERS_2010, true), provider);

//            if (gravity_force == 1) {
//                propagator.addForceModel(holmesFeatherstone);
//                propagator1.addForceModel(holmesFeatherstone);
//            }


            //TRZECIE CIALO
            ForceModel sun = new ThirdBodyAttraction(CelestialBodyFactory.getSun());
//            if (sun_force == 1) {
//                propagator.addForceModel(sun);
//                propagator1.addForceModel(sun);
//            }
            ForceModel moon = new ThirdBodyAttraction(CelestialBodyFactory.getMoon());
//            if (moon_force == 1) {
//                propagator.addForceModel(moon);
//                propagator1.addForceModel(moon);
//            }

            //CISNIENIE PROMIENIOWANIA SLONECZNEGO

            SolarRadiationPressure solarRadiationPressure = new SolarRadiationPressure(CelestialBodyFactory.getSun(),
                    body.getEquatorialRadius(), new IsotropicRadiationSingleCoefficient(area, cr));

            SolarRadiationPressure solarRadiationPressure1 = new SolarRadiationPressure(CelestialBodyFactory.getSun(),
                    body.getEquatorialRadius(), new IsotropicRadiationSingleCoefficient(area1, cr1));


//            if (SRP_force == 1) {
//                propagator.addForceModel(solarRadiationPressure);
//                propagator1.addForceModel(solarRadiationPressure1);
//            }


            //PLYWY OCEANICZNE

            OceanTides oceanTides = new OceanTides(FramesFactory.getITRF(IERSconventions, true), Constants.IERS2010_EARTH_EQUATORIAL_RADIUS,
                    Constants.IERS2010_EARTH_MU,
                    degree,
                    order,
                    IERSconventions,
                    TimeScalesFactory.getUT1(IERSconventions, true));

//            if (OT_force == 1) {
//                propagator.addForceModel(oceanTides);
//                propagator1.addForceModel(oceanTides);
//            }


            //SILY RELATYWISTYCZNE

            //ATMOSFERA

            int atmosphereConfiguration = ATMmodel;
            CssiSpaceWeatherData spaceWeatherData = new CssiSpaceWeatherData("SpaceWeather-All-v1.2.txt");
            //CssiSpaceWeatherData spaceWeatherData = new CssiSpaceWeatherData("C:/Users/matys/Desktop/PROGRAMY_STUDIA/INNE/test_kompilatora/orekit-data/CSSI-Space-Weather-Data/SpaceWeather-All-v1.2.txt");


            if (ATMmodel == 1) {
                DTM2000 atmosphere;
                DTM2000InputParameters parameters = new MarshallSolarActivityFutureEstimation(
                        MarshallSolarActivityFutureEstimation.DEFAULT_SUPPORTED_NAMES,
                        MarshallSolarActivityFutureEstimation.StrengthLevel.AVERAGE);

                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();
                //atmosphere = new DTM2000(parameters, SUN != null ? SUN : SUN, body);
                atmosphere = new DTM2000(spaceWeatherData, SUN != null ? SUN : SUN, body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
                //propagator.addForceModel(dragForce);

                DragForce dragForce1 = new DragForce(atmosphere, new IsotropicDrag(area1, cd1));
                //propagator1.addForceModel(dragForce1);


            }
            if (ATMmodel == 2) {
                HarrisPriester atmosphere;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();
                atmosphere = new HarrisPriester(sun != null ? SUN : SUN, body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
               // propagator.addForceModel(dragForce);

                DragForce dragForce1 = new DragForce(atmosphere, new IsotropicDrag(area1, cd1));
                //propagator1.addForceModel(dragForce1);
            }

            if (ATMmodel == 3) {
                JB2008 atmosphere;

                JB2008InputParameters parameters = null;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();

                atmosphere = new JB2008(parameters, sun != null ? SUN : SUN, body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
               // propagator.addForceModel(dragForce);
                DragForce dragForce1 = new DragForce(atmosphere, new IsotropicDrag(area1, cd1));
                //propagator1.addForceModel(dragForce1);

            }

            if (ATMmodel == 4) {
                NRLMSISE00 atmosphere;
                NRLMSISE00InputParameters parameters = null;
                PVCoordinatesProvider SUN = CelestialBodyFactory.getSun();

                atmosphere = new NRLMSISE00(spaceWeatherData, CelestialBodyFactory.getSun(), body);
                DragForce dragForce = new DragForce(atmosphere, new IsotropicDrag(area, cd));
               // propagator.addForceModel(dragForce);
                DragForce dragForce1 = new DragForce(atmosphere, new IsotropicDrag(area1, cd1));
                //propagator1.addForceModel(dragForce1);


            }

            System.out.println(" step[s]         date         timeFromEpoch_obj1[days]     a           e" +
                    "           i         \u03c9          \u03a9" +
                    "          M");

            double durationTime = date1obToIniDate;
            SpacecraftState finalState = propagator.propagate(new AbsoluteDate(date1ob, durationTime));
            System.out.println(finalState.getDate());
            System.out.println(finalState);
            initialOrbit = finalState.getOrbit();
            System.out.println(initialOrbit.getDate());
            System.out.println(initialOrbit);

            double durationTime1 = date2obToIniDate;
            SpacecraftState finalState1 = propagator1.propagate(new AbsoluteDate(date2ob, durationTime1));
            System.out.println(finalState1.getDate());
            System.out.println(finalState1);
            initialOrbit1 = finalState1.getOrbit();
            System.out.println(initialOrbit1.getDate());
            System.out.println(initialOrbit1);

            AbsoluteDate finalDate = initialDate.shiftedBy(propagationTime);
            long iter = 0;
            double stepT = 60.0;
            System.out.format(Locale.US, "%s %s%n", initialDate, initialDate.shiftedBy(-180.0).getDate());
            boolean start = false;
            for (AbsoluteDate extrapDate = initialDate;
                 extrapDate.compareTo(finalDate) <= 0;
                 extrapDate = extrapDate.shiftedBy(stepT)) {

                if (extrapDate.compareTo(initialDate.shiftedBy(-60.0)) >= 0) {
                    stepT = 0.001;
                }
                if (extrapDate.compareTo(initialDate) >= 0) {
                    stepT = durTime;
                    start = true;
                }
                SpacecraftState currentState = propagator.propagate(extrapDate);
                SpacecraftState currentState1 = propagator1.propagate(extrapDate);
//                if(iter%1000==0) {
                KeplerianOrbit o = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(currentState.getOrbit());
                KeplerianOrbit o1 = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(currentState1.getOrbit());


                if (start) {
                    output.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(initialDate) / 86400.0,
                            o.getA(),
                            o.getE(),
                            FastMath.toDegrees(o.getI()),
                            FastMath.toDegrees(o.getPerigeeArgument()),
                            FastMath.toDegrees(o.getRightAscensionOfAscendingNode()),
                            FastMath.toDegrees(MathUtils.normalizeAngle(o.getMeanAnomaly(), FastMath.PI)));


                    output1.format(Locale.US, "%s %12.8f %23.16e %23.16e %23.16e %23.16e %23.16e%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(initialDate) / 86400.0,
                            o.getEquinoctialEy(), // h
                            o.getEquinoctialEx(), // k
                            o.getHy(),            // p
                            o.getHx(),            // q
                            FastMath.toDegrees(MathUtils.normalizeAngle(o.getLM(), FastMath.PI)));


                    output2.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(initialDate) / 86400.0,
                            currentState.getOrbit().getA(),
                            currentState.getOrbit().getEquinoctialEy(), // h
                            currentState.getOrbit().getEquinoctialEx(), // k
                            currentState.getOrbit().getHy(),            // p
                            currentState.getOrbit().getHx(),            // q
                            FastMath.toDegrees(MathUtils.normalizeAngle(currentState.getOrbit().getLM(), FastMath.PI)));


                    final PVCoordinates pv = currentState.getPVCoordinates();
                    output3.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState.getDate(),
                            currentState.getDate().durationFrom(initialDate) / 86400.0,
                            pv.getPosition().getX() * 0.001, //"position along X (km)"
                            pv.getPosition().getY() * 0.001, //"position along Y (km)"
                            pv.getPosition().getZ() * 0.001, //"position along Y (km)"
                            pv.getVelocity().getX() * 0.001, //"velocity along X (km/s)"
                            pv.getVelocity().getY() * 0.001, //"velocity along Y (km/s)"
                            pv.getVelocity().getZ() * 0.001); //"velocity along Z (km/s)"


                    output4.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState1.getDate(),
                            currentState1.getDate().durationFrom(initialDate) / 86400.0,
                            o1.getA(),
                            o1.getE(),
                            FastMath.toDegrees(o1.getI()),
                            FastMath.toDegrees(o1.getPerigeeArgument()),
                            FastMath.toDegrees(o1.getRightAscensionOfAscendingNode()),
                            FastMath.toDegrees(MathUtils.normalizeAngle(o1.getMeanAnomaly(), FastMath.PI)));


                    output5.format(Locale.US, "%s %12.8f %23.16e %23.16e %23.16e %23.16e %23.16e%n",
                            currentState1.getDate(),
                            currentState1.getDate().durationFrom(initialDate) / 86400.0,
                            o1.getEquinoctialEy(), // h
                            o1.getEquinoctialEx(), // k
                            o1.getHy(),            // p
                            o1.getHx(),            // q
                            FastMath.toDegrees(MathUtils.normalizeAngle(o1.getLM(), FastMath.PI)));


                    output6.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState1.getDate(),
                            currentState1.getDate().durationFrom(initialDate) / 86400.0,
                            currentState1.getOrbit().getA(),
                            currentState1.getOrbit().getEquinoctialEy(), // h
                            currentState1.getOrbit().getEquinoctialEx(), // k
                            currentState1.getOrbit().getHy(),            // p
                            currentState1.getOrbit().getHx(),            // q
                            FastMath.toDegrees(MathUtils.normalizeAngle(currentState1.getOrbit().getLM(), FastMath.PI)));



                    final PVCoordinates pv1 = currentState1.getPVCoordinates();
                    output7.format(Locale.US, "%s %12.8f %12.3f %10.8f %10.6f %10.6f %10.6f %10.6f%n",
                            currentState1.getDate(),
                            currentState1.getDate().durationFrom(initialDate) / 86400.0,
                            pv1.getPosition().getX() * 0.001, //"position along X (km)"
                            pv1.getPosition().getY() * 0.001, //"position along Y (km)"
                            pv1.getPosition().getZ() * 0.001, //"position along Y (km)"
                            pv1.getVelocity().getX() * 0.001, //"velocity along X (km/s)"
                            pv1.getVelocity().getY() * 0.001, //"velocity along Y (km/s)"
                            pv1.getVelocity().getZ() * 0.001); //"velocity along Z (km/s)"

                    AbsoluteDate date = currentState.getDate();
                    Vector3D P1 = currentState.getPVCoordinates().getPosition();
                    AbsoluteDate date1 = currentState1.getDate();
                    Vector3D P2 = currentState1.getPVCoordinates().getPosition();
                    double distance_m = Vector3D.distance(P1, P2); //[m]
                    double distance_km = Vector3D.distance(P1, P2) * 0.001; //[km]
//                    if (distance_km <= 1000.0) {
//                        stepT = durTime;
//                        stepT = durTime / 4;
//                        if (distance_km <= 500.0) {
//                            stepT = stepT / 4;
//                            if (distance_km <= 200.0) {
//                                stepT = stepT / 2;
//                                if (distance_km <= 100.0) {
//                                    stepT = stepT / 4;
//                                    if (distance_km <= 50.0) {
//                                        stepT = stepT / 4;
//                                        if (distance_km <= 40.0) {
//                                            stepT = stepT / 2;
//                                            if (distance_km <= 30.0) {
//                                                stepT = 0.1;
//                                                if (distance_km <= 25.0) {
//                                                    stepT = 0.05;
//                                                    if (distance_km <= 10.0) {
//                                                        stepT = 0.001;
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                }
//
//                            }
//
//                        }
//
//                    }
                    System.out.format(Locale.US, "%s %s %12.8f %12.8f %12.8f%n",
                            stepT, date, date.durationFrom(initialDate) / 86400.0, distance_m, distance_km);
                    output8.format(Locale.US, "%s %s %12.8f %12.8f %12.8f%n",
                            stepT, date, date.durationFrom(initialDate) / 86400.0, distance_m, distance_km);
                }
            }


            output.close();
            output1.close();
            output2.close();
            output3.close();
            output4.close();
            output5.close();
            output6.close();
            output7.close();
            output8.close();

        }
    }
}




