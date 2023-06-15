package msk.proj.hla.storesim.store;

import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import msk.proj.hla.storesim.store.som.Store;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

public class StoreFederate {
    private final static String COMPONENT_NAME = "StoreFederate";
    private final static String FEDERATION_NAME = "StoreSimFederation";
    public static final String READY_TO_RUN = "READY_TO_RUN";
    private static final String FED_FILEPATH = "storesim.fed";
    private RTIambassador rtiAmbassador;
    private StoreAmbassador fedAmbassador;
    private Store store;
    private final double TIME_STEP = 10.0;

    public StoreFederate() {

    }

    private static void log(String message)
    {
        System.out.println(COMPONENT_NAME + " : " + message);
    }

    public void runFederate() throws RTIexception{
        /* LOGIC/DATA MODEL CONSTRUCTION*/
        store = new Store();

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
        fedAmbassador = new StoreAmbassador();
        rtiAmbassador.joinFederationExecution(COMPONENT_NAME, FEDERATION_NAME, fedAmbassador);
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
//            if(fedAmbassador.federateTime >= 2000) {
//                break;
//            }
            advanceTime(TIME_STEP);
            rtiAmbassador.tick();
        }
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
            new StoreFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
        log("End");
    }
}
