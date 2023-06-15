package msk.proj.hla.storesim.cashes.som;

import msk.proj.hla.storesim.clients.som.Client;

import java.util.ArrayList;
import java.util.List;

public class CashesManager {
    private final static double CLIENT_SERVICE_TIME_FACTOR = 50.0;
    private int cashesMaxCount = 4;
    private ArrayList<Cash> cashes;

    public CashesManager() {
        cashes = new ArrayList<>();
    }
    public CashesManager(int cashesMaxCount) {
        cashes = new ArrayList<>();
        this.cashesMaxCount = cashesMaxCount;
    }

    public Cash getCashById(int cashId){
        for(Cash cash : cashes) {
            if (cash.getId() == cashId) {
                return cash;
            }
        }
        return null;
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

    public List<Integer> getCashesIdToStartService(double currentTime) {
        List<Integer> cashesIdList = new ArrayList<Integer>();

        for(Cash cash : cashes) {
            if (cash.isAvailable() && cash.getQueueLen() > 0) {
                cashesIdList.add(cash.getId());

                double serviceEndTime = cash.getFirstClient().getServiceStartTime() + getClientServiceTimeDelay(cash.getFirstClient().getGoodsAmount());
                cash.takeTheCash(currentTime, serviceEndTime);
            }
        }

        return cashesIdList;
    }

    public List<Integer> getCashesIdToEndService(double currentTime) {
        List<Integer> cashesIdList = new ArrayList<Integer>();

        for(Cash cash : cashes) {
            if (!cash.isAvailable() && cash.getQueueLen() > 0) {
                Client client = cash.getFirstClient();
                if (client.getServiceEndTime() <= currentTime) {
                    cashesIdList.add(cash.getId());
                }
                cash.releaseTheCash();
            }
        }

        return cashesIdList;
    }

    public static double getClientServiceTimeDelay(int goodsAmount) {
        return goodsAmount * CLIENT_SERVICE_TIME_FACTOR;
    }
}
