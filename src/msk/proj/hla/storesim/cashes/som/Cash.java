package msk.proj.hla.storesim.cashes.som;

import msk.proj.hla.storesim.clients.som.Client;

import java.util.ArrayList;

public class Cash {
    private static int COUNTER = 0;
    private final int id;
    private boolean available;
    private ArrayList<Client> queue;

    // Constructors
    public Cash() {
        id = COUNTER++;
        available = true;
        queue = new ArrayList<>();
    }

    public Cash(int id) {
        this.id = id;
        available = true;
    }

    // Getters
    public int getId() {
        return id;
    }
    public boolean isAvailable() {
        return available;
    }
    public int getQueueLen() { return queue.size(); }


    // Setters
    public void takeTheCash() {
        available = false;
    }
    public void releaseTheCash() {
        available = true;
    }

    public void enqueueClient(Client client) {
        queue.add(client);
    }
}
