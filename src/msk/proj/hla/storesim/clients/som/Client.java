package msk.proj.hla.storesim.clients.som;

import java.util.Random;

public class Client {
    private static int COUNTER = 0;
    private final static int MAX_GOODS_AMOUNT = 20;
    private final int id;
    private final int goodsAmount;
    private double serviceStartTime = -1;
    private double serviceEndTime = -1;

    // Constructors
    public Client() {
        id = COUNTER++;
        Random rand = new Random();
        this.goodsAmount = rand.nextInt(MAX_GOODS_AMOUNT);
    }

    public Client(int id, int goodsAmount) {
        this.id = id;
        this.goodsAmount = goodsAmount;
    }


    // Getters
    public int getId() {
        return id;
    }

    public int getGoodsAmount() { return goodsAmount; }

    public double getServiceStartTime() { return serviceStartTime; }
    public double getServiceEndTime() { return serviceEndTime; }

    // Setters
    public void setServiceStartTime(double serviceStartTime) { this.serviceStartTime = serviceStartTime; }
    public void setServiceEndTime(double serviceEndTime) { this.serviceEndTime = serviceEndTime; }

}
