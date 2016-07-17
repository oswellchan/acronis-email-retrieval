import java.util.ArrayList;

public class CommandQueue {
    private ArrayList<Command> history = new ArrayList<Command>();
    private static CommandQueue instance = null;

    public static CommandQueue getInstance() {
        if(instance == null) {
            instance = new CommandQueue();
        }
        return instance;
    }

    public void execute(Command cmd) {
        cmd.execute();
    }
}
