package msk.proj.hla.storesim;

import java.util.HashMap;
import java.util.Map;

public class HandlersHelper {
    private static Map<String, Integer> interactionClassMapping;

    static {
        interactionClassMapping = new HashMap<String, Integer>();
    }

    public static void addInteractionClassHandler(String interactionName, Integer handle) {
        interactionClassMapping.put(interactionName, handle);
    }

    public static int getInteractionHandleByName(String name) {
        return interactionClassMapping.get(name).intValue();
    }
}
