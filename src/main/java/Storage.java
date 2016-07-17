import java.io.FileReader;
import java.util.HashMap;

public class Storage {
    private static Storage instance = null;
    private HashMap<String, byte[]> msgIdToIVMap = null;
    private Storage() {
        msgIdToIVMap = new HashMap<String, byte[]>();
    }
    public static Storage getInstance() {
        if(instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    public byte[] getIV(String msgId) {
        return msgIdToIVMap.get(msgId);
    }

    public void updateIdToIVMap(String msgId, byte[] IV) {
        msgIdToIVMap.put(msgId, IV);
    }

    private boolean fileDoesNotExists(String fileName) {

        boolean fileDoesNotExists = true;

        try {
            FileReader reader = new FileReader(fileName);
            reader.close();

            fileDoesNotExists = false;
        } catch (Exception e) {

        }

        return fileDoesNotExists;
    }
}
