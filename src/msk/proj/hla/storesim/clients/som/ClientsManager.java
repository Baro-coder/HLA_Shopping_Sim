package msk.proj.hla.storesim.clients.som;

import java.util.ArrayList;
import java.util.Random;

public class ClientsManager {
    public static int MIN_SHOPPING_TIME = 50;
    public static int MAX_SHOPPING_TIME = 500;
    private ArrayList<Client> clients;
    public ClientsManager() {
        clients = new ArrayList<>();
    }

    public Client bringNewClient() {
        Client client = new Client();
        clients.add(client);
        return client;
    }

    public static int getClientRandomShoppingTime() {
        Random rand = new Random();
        return rand.nextInt(MAX_SHOPPING_TIME) + MIN_SHOPPING_TIME;
    }
}
