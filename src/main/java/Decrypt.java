import java.io.IOException;
import java.util.Base64;
import java.util.Set;

public class Decrypt implements Command{
    String id = null;
    String userEmail = null;
    Storage mem = null;

    Decrypt(String userEmail) {
        this.userEmail = userEmail;
        this.mem = Storage.getInstance(userEmail);
    }

    Decrypt(String id, String userEmail) {
        this.id = id;
        this.userEmail = userEmail;
        this.mem = Storage.getInstance(userEmail);
    }

    public void execute() {
        if (id == null) {
            Set<String> ids = mem.listOfMessageIds();
            for (String id : ids) {
                byte[] decryptedData = decrypt(id);
                mem.writeToFile(decryptedData, id + "-decrypted");
            }
        } else {
            byte[] decryptedData = decrypt(id);
            mem.writeToFile(decryptedData, id + "-decrypted");
        }
    }

    private byte[] decrypt(String id) {
        byte[] decryptedData = null;
        try {
            byte[] encryptedData = mem.getBytesFromFile(id);
            decryptedData = Encryption.decrypt(userEmail, encryptedData, id);
        } catch (IOException e) {
            System.out.println("Id does not exist.");
        } catch (Exception e) {
            System.out.println("Error in decryption. Error: " + e.getMessage());
        }
        return decryptedData;
    }
}
