import com.google.api.services.gmail.model.Message;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Get implements Command {
    private String startDate = null;
    private String endDate = null;
    private String userEmail = null;
    private Storage mem = null;
    DecimalFormat df = new DecimalFormat("#.#");

    Get(String startDate, String endDate, String userEmail) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.userEmail = userEmail;
        mem = Storage.getInstance(userEmail);
    }
    public void execute() {
        String query = "after:" + startDate + "before:" + endDate;
        EmailBackup service = EmailBackup.getInstance();
        ArrayList<String> ids = new ArrayList<String>();
        try {
            List<Message> messages = service.listMessagesMatchingQuery("me", query);
            long totalMessages = messages.size();
            System.out.println(totalMessages + " email(s) found. Retrieving...");
            long count = 0;
            for (Message m : messages) {
                String msgId = m.getId();
                if (!mem.hasMessage(msgId)) {
                    byte[] messageInRaw = service.getMessageRaw(msgId);
                    byte[] encrypted = Encryption.encrypt(userEmail, messageInRaw, msgId);
                    mem.writeToFile(encrypted, msgId);
                    ids.add(msgId);
                }
                String progress = df.format((double)++count / totalMessages * 100);
                System.out.print(count + " out of " + totalMessages + " retrieved. | " + progress +"%       \r");
            }

            mem.saveMsgIdToIVMap();
            System.out.println("Completed.                                  ");
        } catch (Exception e) {
            System.out.println("Error in getting file: " + e.getMessage());
            rollback(ids, userEmail);
        }
    }

    public void rollback(List<String> ids, String userEmail) {
        for (String id : ids) {
            mem.deleteFile(id);
            mem.removeFromIdToIVMap(id);
        }
    }
}
