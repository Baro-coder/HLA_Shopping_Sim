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
        queue = new ArrayList<>();
    }

    // Getters
    public int getId() {
        return id;
    }
    public boolean isAvailable() {
        return available;
    }
    public int getQueueLen() { return queue.size(); }
    public Client getFirstClient() {
        if (queue.size() > 0) {
            return queue.get(0);
        }
        return null;
    }

    // Setters
    public void takeTheCash(double currentTime) {
        available = false;
        Client client = getFirstClient();
        if(client != null) {
            client.setServiceStartTime(currentTime);
        }
    }
    public void takeTheCash(double currentTime, double serviceEndTime) {
        available = false;
        Client client = getFirstClient();
        if (client != null) {
            client.setServiceStartTime(currentTime);
            client.setServiceEndTime(serviceEndTime);
        }
    }

    public void releaseTheCash() {
        available = true;
        dequeueFirstClient();
    }

    public void enqueueClient(Client client) {
        queue.add(client);
    }
    public void dequeueFirstClient() {
        if (queue.size() > 0) {
            queue.remove(0);
        }
    }
}
