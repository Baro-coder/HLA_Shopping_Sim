package msk.proj.hla.storesim.clients;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import msk.proj.hla.storesim.cashes.som.Cash;
import msk.proj.hla.storesim.clients.som.Client;
import msk.proj.hla.storesim.clients.som.ClientsManager;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Random;

public class ClientsFederate {
    private final static double SIMULATION_TIME = 2000.0;
    private final static String COMPONENT_NAME = "ClientsFederate  ";
    private final static String FEDERATION_NAME = "StoreSimFederation";
    public static final String READY_TO_RUN = "READY_TO_RUN";
    private static final String FED_FILEPATH = "storesim.fed";
    private RTIambassador rtiAmbassador;
    private ClientsAmbassador fedAmbassador;
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
        fedAmbassador = new ClientsAmbassador();
        /* LOGIC MODEL CONSTRUCTION*/
        fedAmbassador.clientsManager = new ClientsManager();
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

            // Interaction :: Publish :: New Client Arrival
            Client client = fedAmbassador.clientsManager.bringNewClient();
            if(client != null) {
                publishNewClientArrival(client);
            }

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
        // New Client Arrival - Publish
        int newClientArrival = rtiAmbassador.getInteractionClassHandle("InteractionRoot.NewClientArrival");
        fedAmbassador.NEW_CLIENT_ARRIVAL = newClientArrival;
        rtiAmbassador.publishInteractionClass(newClientArrival);

        // Client Shopping End - Publish
        int clientShoppingEnd = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientShoppingEnd");
        fedAmbassador.CLIENT_SHOPPING_END = clientShoppingEnd;
        rtiAmbassador.publishInteractionClass(clientShoppingEnd);
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
    // Interaction :: Publish :: NewClientArrival
    public void publishNewClientArrival(Client client) throws RTIexception {
        // Interaction handler
        int newClientArrivalHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.NewClientArrival");

        // Params handler
        SuppliedParameters params = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        // Params dump
        int clientIdHandle = rtiAmbassador.getParameterHandle("clientId", newClientArrivalHandle);
        byte[] clientIdValue = EncodingHelpers.encodeInt(client.getId());
        params.add(clientIdHandle, clientIdValue);

        int goodsAmountHandle = rtiAmbassador.getParameterHandle("goodsAmount", newClientArrivalHandle);
        byte[] goodsAmountValue = EncodingHelpers.encodeInt(client.getGoodsAmount());
        params.add(goodsAmountHandle, goodsAmountValue);

        // Interaction time set
        LogicalTime time = new DoubleTime(fedAmbassador.federateTime + fedAmbassador.federateLookahead);

        // Send interaction
        rtiAmbassador.sendInteraction(newClientArrivalHandle, params, "tag".getBytes(), time);

        // Log
        log("Interaction Publish  :: New Client Arrival : " + "clientId(" + client.getId() + ") : goodsAmount(" + client.getGoodsAmount() + ") :: time : " + time);

        // Send interaction of client shopping end in advance
        publishClientShoppingEnd(client.getId());
    }

    // Interaction :: Publish :: ClientShoppingEnd
    public void publishClientShoppingEnd(int clientId) throws RTIexception{
        // Interaction handler
        int clientShoppingEndHandle = rtiAmbassador.getInteractionClassHandle("InteractionRoot.ClientShoppingEnd");

        // Params handler
        SuppliedParameters params = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        // Params dump
        int clientIdHandle = rtiAmbassador.getParameterHandle("clientId", clientShoppingEndHandle);
        byte[] clientIdValue = EncodingHelpers.encodeInt(clientId);
        params.add(clientIdHandle, clientIdValue);

        // Interaction time set
        int delay = ClientsManager.getClientRandomShoppingTime();
        LogicalTime time = new DoubleTime(fedAmbassador.federateTime + fedAmbassador.federateLookahead + delay);

        // Send interaction
        rtiAmbassador.sendInteraction(clientShoppingEndHandle, params, "tag".getBytes(), time);

        // Log
        log("Interaction Publish  :: Client Shopping End : " + "clientId(" + clientId + ") :: time : " + time);
    }

    public static void main(String[] args) {
        try {
            new ClientsFederate().runFederate();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }
}
