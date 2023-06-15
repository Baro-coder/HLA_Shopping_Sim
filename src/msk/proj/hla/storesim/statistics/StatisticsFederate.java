package msk.proj.hla.storesim.statistics;

import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

public class StatisticsFederate {
    private final static double SIMULATION_TIME = 2000.0;
    private final static String COMPONENT_NAME = "StatisticsFederate  ";
    private final static String FEDERATION_NAME = "StoreSimFederation";
    public static final String READY_TO_RUN = "READY_TO_RUN";
    private static final String FED_FILEPATH = "storesim.fed";
    private RTIambassador rtiAmbassador;
    private StatisticsAmbassador fedAmbassador;
    private final double TIME_STEP = 10.0;

    private static void log(String message)
    {
        System.out.println(COMPONENT_NAME + " : " + message);
    }

    public void runFederate() throws RTIexception {
        /* FEDERATION CREATION */
        rtiAmbassador = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        try
        {
            File fom = new File(FED_FILEPATH);
            rtiAmbassador.createFederationExecution( FEDERATION_NAME,
                    fom.toURI().toURL() );
            log( "Federation created." );
        }
        catch(FederationExecutionAlreadyExists exists)
        {
            log( "Cannot create federation :: already exists" );
        }
        catch(MalformedURLException urlEx) {
            log("Exception processing fom: " + urlEx.getMessage());
            urlEx.printStackTrace();
            return;
        }

        /* FEDERATE JOIN */
        fedAmbassador = new StatisticsAmbassador();
        /* LOGIC MODEL CONSTRUCTION*/
        fedAmbassador.statistics = new Statistics();
        rtiAmbassador.joinFederationExecution(COMPONENT_NAME, FEDERATION_NAME, fedAmbassador );
        log( "Joined Federation as " + COMPONENT_NAME);

        /* FEDERATION SYNC POINT REGISTER*/
        rtiAmbassador.registerFederationSynchronizationPoint( READY_TO_RUN, null );

        while(!fedAmbassador.isAnnounced)
        {
            rtiAmbassador.tick();
        }

        /* WAITING POINT */
        waitForUser();

        /* FEDERATE SYNC */
        rtiAmbassador.synchronizationPointAchieved(READY_TO_RUN);
        log( "Achieved sync point: " + READY_TO_RUN + ", waiting for federation..." );
        while(!fedAmbassador.isReadyToRun)
        {
            rtiAmbassador.tick();
        }

        /* FEDERATE TIME POLICY */
        enableTimePolicy();

        /* INTERACTIONS HANDLE INIT */
        publishAndSubscribe();

        /* FEDERATE MAIN LOOP */
        while (fedAmbassador.running) {
            advanceTime(TIME_STEP);

            // Simulation end condition
            if(fedAmbassador.federateTime > SIMULATION_TIME) {
                break;
            }

            rtiAmbassador.tick();
        }

        /* STATS SUMMARY PRINT */
        fedAmbassador.statistics.summaryPrint();
    }

    private void enableTimePolicy() throws RTIexception
    {
        LogicalTime currentTime = new DoubleTime(fedAmbassador.federateTime);
        LogicalTimeInterval lookahead = new DoubleTimeInterval(fedAmbassador.federateLookahead);

        this.rtiAmbassador.enableTimeRegulation(currentTime, lookahead);

        while(!fedAmbassador.isRegulating)
        {
            rtiAmbassador.tick();
        }

        this.rtiAmbassador.enableTimeConstrained();

        while(!fedAmbassador.isConstrained)
        {
            rtiAmbassador.tick();
        }
    }

    private void publishAndSubscribe() throws RTIexception {
        // New Cash Register - Subscribe
        int newCashRegister = rtiAmbassador.getInteractionClassHandle("InteractionRoot.NewCashRegister");
        fedAmbassador.NEW_CASH_REGISTER_HANDLE = newCashRegister;
        rtiAmbassador.subscribeInteractionClass(newCashRegister);

        // Client Queue Get - Subscribe
        int clientQueueGet = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientQueueGet");
        fedAmbassador.CLIENT_QUEUE_GET = clientQueueGet;
        rtiAmbassador.subscribeInteractionClass(clientQueueGet);

        // Client Service Start - Subscribe
        int clientServiceStart = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientServiceStart");
        fedAmbassador.CLIENT_SERVICE_START = clientServiceStart;
        rtiAmbassador.subscribeInteractionClass(clientServiceStart);

        // Client Service End - Subscribe
        int clientServiceEnd = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientServiceEnd");
        fedAmbassador.CLIENT_SERVICE_END = clientServiceEnd;
        rtiAmbassador.subscribeInteractionClass(clientServiceEnd);
    }

    private void advanceTime(double step) throws RTIexception {
        log("Time advance :: Request :: Step : " + step);

        fedAmbassador.isAdvancing = true;
        LogicalTime newTime = new DoubleTime(fedAmbassador.federateTime + step);
        rtiAmbassador.timeAdvanceRequest(newTime);
        while (fedAmbassador.isAdvancing) {
            rtiAmbassador.tick();
        }

        log("Time advance :: Granted :: Time : " + fedAmbassador.federateTime);
    }

    private void waitForUser()
    {
        log( " Waiting for user :: Press ENTER to continue..." );
        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
        try
        {
            reader.readLine();
        }
        catch( Exception e )
        {
            log( "Error while waiting for user input: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new StatisticsFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
