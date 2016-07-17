import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Storage {
    private static final String mapFileName = "data.ser";
    private static String fileStorePath = null;
    private static Storage instance = null;
    private HashMap<String, byte[]> msgIdToIVMap = null;

    private Storage(String email) {
        fileStorePath = "./" + email + "/";
        initialiseUserFolder(fileStorePath);
        msgIdToIVMap = initialiseMsgIdToIVMap();
    }

    public static Storage getInstance(String email) {
        if (instance == null) {
            instance = new Storage(email);
        }
        return instance;
    }

    private void initialiseUserFolder(String path) {
        try {
            Path userFolderPath = Paths.get(path);
            if (Files.notExists(userFolderPath)) {
                Files.createDirectories(userFolderPath);
            }
        } catch (Exception e) {
            System.out.println("Unable to create user directory.");
            System.exit(1);
        }
    }

    private HashMap<String, byte[]> initialiseMsgIdToIVMap() {
        HashMap<String, byte[]> map = null;
        try
        {
            FileInputStream fileIn = new FileInputStream(fileStorePath + mapFileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            map = (HashMap<String, byte[]>) in.readObject();
            in.close();
            fileIn.close();
        }catch(Exception e)
        {
            map = new HashMap<String, byte[]>();
        }
        return map;
    }

    public void saveMsgIdToIVMap() {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileStorePath + mapFileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(msgIdToIVMap);
            out.close();
            fileOut.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getIV(String msgId) {
        return msgIdToIVMap.get(msgId);
    }

    public void updateIdToIVMap(String msgId, byte[] IV) {
        msgIdToIVMap.put(msgId, IV);
    }

    public void removeFromIdToIVMap(String msgId) {
        msgIdToIVMap.remove(msgId);
    }

    public boolean hasMessage(String id) {
        return msgIdToIVMap.containsKey(id);
    }

    public void writeToFile(byte[] data, String fileName) {
        try {
            Path file = Paths.get(fileStorePath + fileName);
            Files.write(file, data);
        } catch (Exception e) {
            System.out.println("Unable to write file to disk.");
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path file = Paths.get(fileStorePath + fileName);
            Files.delete(file);
        } catch (Exception e) {
            System.out.println("Unable to delete file.");
        }
    }
}
