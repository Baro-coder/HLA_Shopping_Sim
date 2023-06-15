package msk.proj.hla.storesim.cashes;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.NullFederateAmbassador;
import msk.proj.hla.storesim.cashes.som.Cash;
import msk.proj.hla.storesim.clients.ClientsFederate;
import msk.proj.hla.storesim.clients.som.Client;
import msk.proj.hla.storesim.store.StoreFederate;
import org.portico.impl.hla13.types.DoubleTime;

public class CashesAmbassador  extends NullFederateAmbassador {
    private final static String COMPONENT_NAME = "CashesAmbassador";
    protected int NEW_CASH_REGISTER_HANDLE = 0;
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
    protected CashesFederate fedObject    = null;

    private static void log(String message)
    {
        System.out.println( COMPONENT_NAME + " : " + message );
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
        StringBuilder builder = new StringBuilder("Received Interaction: ");

        if (interactionClass == CLIENT_QUEUE_GET) {
            try {
                int cashId = EncodingHelpers.decodeInt(theInteraction.getValue(0));
                int clientId = EncodingHelpers.decodeInt(theInteraction.getValue(1));
                int clientGoodsAmount = EncodingHelpers.decodeInt(theInteraction.getValue(2));

                builder.append("Client Queue Get : cashId(").append(cashId).append(") : clientId(").append(clientId).append(") : clientGoodsAmount(").append(clientGoodsAmount).append(")");

                fedObject.enqueueClient(cashId, new Client(clientId, clientGoodsAmount));

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
