package msk.proj.hla.storesim.cashes.som;

import msk.proj.hla.storesim.clients.som.Client;

import java.util.ArrayList;

public class CashesManager {
    private int cashesMaxCount = 4;
    private ArrayList<Cash> cashes;

    public CashesManager() {
        cashes = new ArrayList<>();
    }
    public CashesManager(int cashesMaxCount) {
        cashes = new ArrayList<>();
        this.cashesMaxCount = cashesMaxCount;
    }

    public Cash registerNewCash(){
        if (cashes.size() < cashesMaxCount) {
            Cash cash = new Cash();
            cashes.add(cash);
            return cash;
        }
        return null;
    }

    public void enqueueClient(int cashId, Client client) {
        for(Cash cash : cashes) {
            if (cash.getId() == cashId) {
                cash.enqueueClient(client);
            }
        }
    }
}
