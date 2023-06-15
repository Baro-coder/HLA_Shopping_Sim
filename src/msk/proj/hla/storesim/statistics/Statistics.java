package msk.proj.hla.storesim.statistics;

import msk.proj.hla.storesim.cashes.som.Cash;
import msk.proj.hla.storesim.clients.som.Client;

import java.util.ArrayList;

public class Statistics {
    private ArrayList<Cash> cashes;

    public Statistics() {
        cashes = new ArrayList<>();
    }

    public void summaryPrint() {
        System.out.println("\n --- STATISTICS SUMMARY ---\n");

        int cashesCount = cashes.size();

        // Mean queue length
        double meanQueueLen = 0.0;
        System.out.println(" Mean queue len: ");
        for(Cash cash : cashes) {
            meanQueueLen += cash.getMeanQueueLen();
            System.out.println("  - " + cash.getId() + " :: " + String.format("%.2f", cash.getMeanQueueLen()));
        }
        meanQueueLen /= cashesCount;
        System.out.println(" * Average: " + String.format("%.2f", meanQueueLen) + "\n");

        // Mean waiting time
        double meanWaitingTime = 0.0;
        System.out.println(" Mean waiting time: ");
        for(Cash cash : cashes) {
            meanWaitingTime += cash.getMeanWaitingTime();
            System.out.println("  - " + cash.getId() + " :: " + String.format("%.2f", cash.getMeanWaitingTime()));
        }
        meanWaitingTime /= cashesCount;
        System.out.println(" * Average: " + String.format("%.2f", meanWaitingTime) + "\n");

        // Max serviced clients count
        int cashId = -1;
        int maxServicedClientsCount = -1;

        for(Cash cash : cashes) {
            if (maxServicedClientsCount == -1 || maxServicedClientsCount < cash.getServicedClientsCounter()) {
                maxServicedClientsCount = cash.getServicedClientsCounter();
                cashId = cash.getId();
            }
        }

        System.out.println(" Max serviced clients count: ");
        System.out.println("   * cash ID:           " + cashId);
        System.out.println("   * serviced clients:  " + maxServicedClientsCount);
    }

    public void addNewCash(int cashId) {
        cashes.add(new Cash(cashId));
    }

    public void addClientToQueue(int cashId, int clientId, int goodsAmount, double currentTime) {
        Client client = new Client(clientId, goodsAmount);
        for(Cash cash : cashes) {
            if(cash.getId() == cashId) {
                cash.enqueueClient(client, currentTime);
            }
        }
    }

    public void cashServiceStart(int cashId) {
        for(Cash cash : cashes) {
            if(cash.getId() == cashId) {
                cash.takeTheCash();
            }
        }
    }

    public void cashServiceEnd(int cashId) {
        for(Cash cash : cashes) {
            if(cash.getId() == cashId) {
                cash.releaseTheCash();
            }
        }
    }
}
