package msk.proj.hla.storesim.cashes.som;

import msk.proj.hla.storesim.clients.som.Client;

import java.util.ArrayList;

public class Cash {
    private static int COUNTER = 0;

    private final int id;
    private boolean available;
    private ArrayList<Client> queue;
    private int servicedClientscounter;
    private int enqueuedClientsCounter = 0;
    private double meanQueueLen;
    private double meanWaitingTime;

    // Constructors
    public Cash() {
        id = COUNTER++;
        available = true;
        queue = new ArrayList<>();
        servicedClientscounter = 0;
        meanQueueLen = 0.0;
        meanWaitingTime = 0.0;
    }

    public Cash(int id) {
        this.id = id;
        available = true;
        queue = new ArrayList<>();
        servicedClientscounter = 0;
        meanQueueLen = 0.0;
        meanWaitingTime = 0.0;
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
    public double getMeanWaitingTime() { return meanWaitingTime; }
    public double getMeanQueueLen() { return meanQueueLen; }
    public int getServicedClientsCounter() {
        return servicedClientscounter;
    }

    // Setters
    public void takeTheCash() {
        available = false;
    }
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

    public void enqueueClient(Client client, double currentTime) {
        client.setEnqueueTime(currentTime);
        queue.add(client);
        meanQueueLen = (meanQueueLen * enqueuedClientsCounter + queue.size()) / (enqueuedClientsCounter + 1);
        enqueuedClientsCounter++;
    }
    private void dequeueFirstClient() {
        if (queue.size() > 0) {
            double actualWaitingTime = Math.abs(getFirstClient().getServiceEndTime() - getFirstClient().getEnqueueTime());
            meanWaitingTime = (meanWaitingTime * servicedClientscounter + actualWaitingTime) / (servicedClientscounter + 1);
            servicedClientscounter++;

            meanQueueLen = (meanQueueLen * enqueuedClientsCounter + queue.size()) / (enqueuedClientsCounter + 1);
            enqueuedClientsCounter++;

            queue.remove(0);
        }
    }
}
