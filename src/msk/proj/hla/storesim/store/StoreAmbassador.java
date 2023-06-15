package msk.proj.hla.storesim.store;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import msk.proj.hla.storesim.cashes.som.Cash;
import msk.proj.hla.storesim.clients.som.Client;
import org.portico.impl.hla13.types.DoubleTime;

public class StoreAmbassador extends NullFederateAmbassador {
    private final static String COMPONENT_NAME = "StoreAmbassador";
    protected int NEW_CASH_REGISTER_HANDLE = 0;
    protected int NEW_CLIENT_ARRIVAL = 0;
    protected int CLIENT_SHOPPING_END = 0;
    protected int CLIENT_QUEUE_GET = 0;
    protected int CLIENT_SERVICE_START = 0;
    protected int CLIENT_SERVICE_END = 0;

    protected double federateTime        = 0.0;
    protected double federateLookahead   = 10.0;
    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;
    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;
    protected boolean running 			 = true;
    protected StoreFederate fedObject    = null;

    private static void log(String message)
    {
        System.out.println(COMPONENT_NAME + " : " + message);
    }

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag )
    {
        receiveInteraction(interactionClass, theInteraction, tag, null, null);
    }

    public void receiveInteraction( int interactionClass,
                                    ReceivedInteraction theInteraction,
                                    byte[] tag,
                                    LogicalTime theTime,
                                    EventRetractionHandle eventRetractionHandle )
    {
        StringBuilder builder = new StringBuilder( "Interaction Received:" );

        if (interactionClass == NEW_CASH_REGISTER_HANDLE) {
            try {
                int newCashId = EncodingHelpers.decodeInt(theInteraction.getValue(0));

                builder.append("New Cash Register : cashId(").append(newCashId).append(")");

                fedObject.registerNewCash(new Cash(newCashId));

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        } else if (interactionClass == NEW_CLIENT_ARRIVAL) {
            try {
                int newClientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                int newClientGoodsAmount = EncodingHelpers.decodeInt(theInteraction.getValue(1));

                builder.append("New Client Arrival : clientId(").append(newClientId).append(") : goodsAmount(").append(newClientGoodsAmount).append(")");

                fedObject.noticeNewClientArrival(new Client(newClientId, newClientGoodsAmount));

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        } else if (interactionClass == CLIENT_SHOPPING_END) {
            try {
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(0));

                builder.append("Client Shopping End : clientId(").append(clientId).append(")");

                fedObject.sendClientToQueue(clientId);

            } catch (RTIexception e) {
                throw new RuntimeException(e);
            }
        } else if (interactionClass == CLIENT_SERVICE_START) {
            int cashId = 0;
            try {
                cashId = EncodingHelpers.decodeInt(theInteraction.getValue(0));

                builder.append("Client Service Start : cashId(").append(cashId).append(")");

                fedObject.noticeClientServiceStart(cashId);

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        } else if (interactionClass == CLIENT_SERVICE_END) {
            int cashId = 0;
            try {
                cashId = EncodingHelpers.decodeInt(theInteraction.getValue(0));

                builder.append("Client Service End : cashId(").append(cashId).append(")");

                fedObject.noticeClientServiceEnd(cashId);

            } catch (ArrayIndexOutOfBounds e) {
                throw new RuntimeException(e);
            }
        }

        log(builder.toString());
    }

    public void synchronizationPointRegistrationFailed( String label )
    {
        log("Failed to register sync point: " + label);
    }

    public void synchronizationPointRegistrationSucceeded(String label)
    {
        log("Successfully registered sync point: " + label);
    }

    public void announceSynchronizationPoint(String label, byte[] tag)
    {
        log("Sync point announced: " + label);
        if( label.equals(StoreFederate.READY_TO_RUN) ) {
            this.isAnnounced = true;
        }
    }

    public void federationSynchronized(String label)
    {
        log("Federation Synchronized: " + label);
        if(label.equals(StoreFederate.READY_TO_RUN)) {
            this.isReadyToRun = true;
        }
    }

    public void timeRegulationEnabled(LogicalTime theFederateTime)
    {
        this.federateTime = ((DoubleTime)theFederateTime).getTime();
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled(LogicalTime theFederateTime)
    {
        this.federateTime = ((DoubleTime)theFederateTime).getTime();
        this.isConstrained = true;
    }

    public void timeAdvanceGrant(LogicalTime theTime)
    {
        this.federateTime = ((DoubleTime)theTime).getTime();
        this.isAdvancing = false;
    }
}
