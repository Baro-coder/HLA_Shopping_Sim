package msk.proj.hla.storesim.cashes.som;

public class Cash {
    private static int COUNTER = 0;
    private final int id;
    private boolean available;

    // Constructors
    public Cash() {
        id = COUNTER++;
        available = true;
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


    // Setters
    public void takeTheCash() {
        available = false;
    }
    public void releaseTheCash() {
        available = true;
    }
}
