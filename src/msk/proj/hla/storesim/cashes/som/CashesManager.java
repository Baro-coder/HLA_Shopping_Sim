package msk.proj.hla.storesim.cashes.som;

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

    public int registerNewCash(){
//        if (cashes.size() < cashesMaxCount) {
            Cash cash = new Cash();
            cashes.add(cash);
            return cash.getId();
//        }
//        return -1;
    }
}
