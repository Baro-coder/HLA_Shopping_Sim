package msk.proj.hla.storesim.store.som;

import msk.proj.hla.storesim.cashes.som.Cash;
import msk.proj.hla.storesim.clients.som.Client;

import java.util.ArrayList;
import java.util.LinkedList;

public class Store {
    private ArrayList<Cash> cashes;
    private ArrayList<Client> clients;

    // Constructor
    public Store() {
        cashes = new ArrayList<>();
        clients = new ArrayList<>();

    }

    public void addCashRegister(Cash cash) {
        cashes.add(cash);
    }
    public void addClient(Client client) {
        // Add to general list
        clients.add(client);
    }

    public Client getClientById(int clientId){
        for(Client client : clients) {
            if (client.getId() == clientId) {
                return client;
            }
        }
        return null;
    }

    public int sendClientToTheShortestQueue(Client client) {
        int cashId = -1;
        int minQueueLen = -1;

        for(Cash cash : cashes) {
            if(minQueueLen == -1 || minQueueLen > cash.getQueueLen()) {
                minQueueLen = cash.getQueueLen();
                cashId = cash.getId();
            }
        }

        return cashId;
    }
}
