package msk.proj.hla.storesim.store.som;

import msk.proj.hla.storesim.cashes.som.Cash;
import msk.proj.hla.storesim.clients.som.Client;

import java.util.ArrayList;
import java.util.LinkedList;

public class Store {
    private ArrayList<Cash> cashes;
    private ArrayList<Client> clients;
    private LinkedList<Client> queues;

    // Constructor
    public Store() {
        cashes = new ArrayList<>();
        clients = new ArrayList<>();
        queues = new LinkedList<>();

    }

    public void addCashRegister(Cash cash) {
        cashes.add(cash);
    }
    public void addClient(Client client) {
        // Add to general list
        clients.add(client);

        // TODO: Add to queue
    }
}
