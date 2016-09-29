import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class CLI {
    //Add new comment
    private static final String ERROR_INVALIDCOMMAND = "Invalid Command. Please try again.";
    private static final String ERROR_INVALIDSTARTDATEFORMAT = "Start date given is in invalid format. Corrrect format is yyyy/mm/dd. Please try again.";
    private static final String ERROR_INVALIDENDDATEFORMAT = "End date given is in invalid format. Corrrect format is yyyy/mm/dd. Please try again.";
    private static final String ERROR_INVALIDSTARTDATE = "Start date given is not an actual date. Please try again.";
    private static final String ERROR_INVALIDENDDATE = "End date given is not an actual date. Please try again.";
    private static final String ERROR_INVALIDDATERANGE = "Start date is later than end date. Please try again.";
    private static final String dateRegex = "\\d{4}/\\d{2}/\\d{2}";

    private static EmailBackup service = null;
    private static CommandQueue commandQueue = null;
    private static String userEmail = null;
    private static boolean isPendingInput = true;


    private static void parseAndExecuteInstruction(String instruction) {
        String[] splitArray = instruction.trim().split(" ");
        String commandType = splitArray[0];

        switch(commandType) {
            case "get":
                if (splitArray.length == 3) {
                    String startDate = splitArray[1];
                    String endDate = splitArray[2];

                    if (isValid(startDate, endDate)) {
                        String newEndDate = getNewEndDate(endDate);
                        commandQueue.execute(new Get(startDate, newEndDate, userEmail));
                    }
                } else {
                    print(ERROR_INVALIDCOMMAND);
                }
                break;
            case "decrypt":
                if (splitArray.length == 1) {
                    commandQueue.execute(new Decrypt(userEmail));
                } else if (splitArray.length == 2) {
                    commandQueue.execute(new Decrypt(splitArray[1], userEmail));
                } else {
                    print(ERROR_INVALIDCOMMAND);
                }
                break;
            case "logout":
                commandQueue.execute(new Logout());
                isPendingInput = false;
                break;
            case "exit":
                isPendingInput = false;
                break;
            default:
                print(ERROR_INVALIDCOMMAND);
        }
    }

    private static boolean isValid(String startDate, String endDate) {
        if (!startDate.matches(dateRegex)) {
            print(ERROR_INVALIDSTARTDATEFORMAT);
            return false;
        }

        if (!endDate.matches(dateRegex)) {
            print(ERROR_INVALIDENDDATEFORMAT);
            return false;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        format.setLenient(false);
        Date start = null;
        try {
            start = format.parse(startDate);
        }
        catch(Exception e){
            print(ERROR_INVALIDSTARTDATE);
            return false;
        }
        Date end = null;
        try {
            end = format.parse(endDate);
        }
        catch(Exception e){
            print(ERROR_INVALIDENDDATE);
            return false;
        }

        if (start.after(end)) {
            print(ERROR_INVALIDDATERANGE);
            return false;
        }

        return true;
    }

    /*Add one day so that we can create a range
    * for Gmail's search query so that it is
    * inclusive on both bounds
    */
    private static String getNewEndDate(String endDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        format.setLenient(false);
        Calendar cal = Calendar.getInstance();
        Date end = null;
        try {
            end = format.parse(endDate);
        }
        catch(Exception e){
            print(ERROR_INVALIDENDDATE);
            return null;
        }
        cal.setTime(end);
        cal.add(Calendar.DATE, 1);

        return format.format(cal.getTime());
    }

    private static void print(String feedback) {
        System.out.println(feedback);
    }

    private static void initialiseEnvironment() {
        try {
            service = EmailBackup.getInstance();
            commandQueue = CommandQueue.getInstance();
            userEmail = service.getEmailAddress();
            print("Logged in as: " + userEmail);
            Storage.getInstance(userEmail);
        } catch (Exception e) {
            print("Unable to setup environment. Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main (String[] args) {
        initialiseEnvironment();
        Scanner sc = new Scanner(System.in);

        while (isPendingInput) {
            System.out.println("Please enter command:");
            String instruction = sc.nextLine();
            parseAndExecuteInstruction(instruction);
        }

        sc.close();
    }
}
