public class Logout implements Command{
    public void execute() {
        EmailBackup.getInstance().revokeCredentials();
    }
}
