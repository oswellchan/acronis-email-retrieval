public class Get implements Command {
    private String startDate = null;
    private String endDate = null;

    Get(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public void execute() {

    }
}
