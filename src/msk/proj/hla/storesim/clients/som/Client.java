package msk.proj.hla.storesim.clients.som;

public class Client {
    private static int COUNTER = 0;
    private final int id;

    // Constructors
    public Client() {
        id = COUNTER++;
    }

    public Client(int id) {
        this.id = id;
    }


    // Getters
    public int getId() {
        return id;
    }

    // Setters
}
