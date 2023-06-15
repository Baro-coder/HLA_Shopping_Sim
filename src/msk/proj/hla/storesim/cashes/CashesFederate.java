package msk.proj.hla.storesim.cashes;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import msk.proj.hla.storesim.cashes.som.Cash;
import msk.proj.hla.storesim.cashes.som.CashesManager;
import msk.proj.hla.storesim.clients.som.Client;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CashesFederate {
    private final static double SIMULATION_TIME = 2000.0;
    private final static String COMPONENT_NAME = "CashesFederate";
    private final static String FEDERATION_NAME = "StoreSimFederation";
    public static final String READY_TO_RUN = "READY_TO_RUN";
    private static final String FED_FILEPATH = "storesim.fed";
    private RTIambassador rtiAmbassador;
    private CashesAmbassador fedAmbassador;
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
        fedAmbassador = new CashesAmbassador();
        /* LOGIC MODEL CONSTRUCTION*/
        fedAmbassador.cashesManager = new CashesManager();
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

            // Interaction :: Publish :: New Cash Register
            Cash cash = fedAmbassador.cashesManager.registerNewCash();
            if(cash != null) {
                publishRegisterNewCash(cash.getId());
            }

            // Interaction :: Publish :: Client Service Start / End
            examineCashQueues();

            rtiAmbassador.tick();
        }
    }

    private void examineCashQueues() throws RTIexception {
        // Check for cashes ready to release for next client
        List<Integer> cashesIdList = fedAmbassador.cashesManager.getCashesIdToEndService(fedAmbassador.federateTime);
        if (cashesIdList.size() > 0) {
            for (int cashId : cashesIdList) {
                publishClientServiceEnd(cashId, fedAmbassador.cashesManager.getCashById(cashId).getId());
            }
        }

        // Check for available cashes to service the clients
        cashesIdList = fedAmbassador.cashesManager.getCashesIdToStartService(fedAmbassador.federateTime);
        if (cashesIdList.size() > 0) {
            for (int cashId : cashesIdList) {
                publishClientServiceStart(cashId, fedAmbassador.cashesManager.getCashById(cashId).getId());
            }
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
        // New Cash Register - Publish
        int newCashRegister = rtiAmbassador.getInteractionClassHandle("InteractionRoot.NewCashRegister");
        fedAmbassador.NEW_CASH_REGISTER_HANDLE = newCashRegister;
        rtiAmbassador.publishInteractionClass(newCashRegister);

        // Client Queue Get - Subscribe
        int clientQueueGet = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientQueueGet");
        fedAmbassador.CLIENT_QUEUE_GET = clientQueueGet;
        rtiAmbassador.subscribeInteractionClass(clientQueueGet);

        // Client Service Start - Publish
        int clientServiceStart = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientServiceStart");
        fedAmbassador.CLIENT_SERVICE_START = clientServiceStart;
        rtiAmbassador.publishInteractionClass(clientServiceStart);

        // Client Service End - Publish
        int clientServiceEnd = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientServiceEnd");
        fedAmbassador.CLIENT_SERVICE_END = clientServiceEnd;
        rtiAmbassador.publishInteractionClass(clientServiceEnd);
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

    /* === INTERACTION HANDLERS === */
    // Interaction :: Publish :: NewCashRegister
    private void publishRegisterNewCash(int cashId) throws RTIexception {
        // Interaction handler
        int newCashRegisterHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.NewCashRegister");

        // Params handler
        SuppliedParameters params = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        // Params dump
        int cashIdHandle = rtiAmbassador.getParameterHandle("cashId", newCashRegisterHandle);
        byte[] cashIdValue = EncodingHelpers.encodeInt(cashId);
        params.add(cashIdHandle, cashIdValue);

        // Interaction time set
        LogicalTime time = new DoubleTime(fedAmbassador.federateTime + fedAmbassador.federateLookahead);

        // Send interaction
        rtiAmbassador.sendInteraction(newCashRegisterHandle, params, "tag".getBytes(), time);

        // Log
        log("New Cash Register : " + "cashId(" + cashId + ") :: time : " + time);
    }

    // Interaction :: Publish :: ClientServiceStart
    private void publishClientServiceStart(int cashId, int clientId) throws RTIexception{
        // Interaction handler
        int clientServiceStartHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientServiceStart");

        // Params handler
        SuppliedParameters params = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        // Params dump
        int cashIdHandle = rtiAmbassador.getParameterHandle("cashId", clientServiceStartHandle);
        byte[] cashIdValue = EncodingHelpers.encodeInt(cashId);
        params.add(cashIdHandle, cashIdValue);

        int clientIdHandle = rtiAmbassador.getParameterHandle("clientId", clientServiceStartHandle);
        byte[] clientIdValue = EncodingHelpers.encodeInt(clientId);
        params.add(clientIdHandle, clientIdValue);

        // Interaction time set
        LogicalTime time = new DoubleTime(fedAmbassador.federateTime + fedAmbassador.federateLookahead);

        // Send interaction
        rtiAmbassador.sendInteraction(clientServiceStartHandle, params, "tag".getBytes(), time);

        // Log
        log("Client Service Start : " + "cashId(" + cashId + ") :: time : " + time);
    }

    // Interaction :: Publish :: ClientServiceEnd
    private void publishClientServiceEnd(int cashId, int clientId) throws RTIexception{
        // Interaction handler
        int clientServiceEndHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientServiceEnd");

        // Params handler
        SuppliedParameters params = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        // Params dump
        int cashIdHandle = rtiAmbassador.getParameterHandle("cashId", clientServiceEndHandle);
        byte[] cashIdValue = EncodingHelpers.encodeInt(cashId);
        params.add(cashIdHandle, cashIdValue);

        int clientIdHandle = rtiAmbassador.getParameterHandle("clientId", clientServiceEndHandle);
        byte[] clientIdValue = EncodingHelpers.encodeInt(clientId);
        params.add(clientIdHandle, clientIdValue);

        // Interaction time set
        LogicalTime time = new DoubleTime(fedAmbassador.federateTime + fedAmbassador.federateLookahead);

        // Send interaction
        rtiAmbassador.sendInteraction(clientServiceEndHandle, params, "tag".getBytes(), time);

        // Log
        log("Client Service End : " + "cashId(" + cashId + ") :: time : " + time);
    }

    public static void main(String[] args) {
        try {
            new CashesFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
