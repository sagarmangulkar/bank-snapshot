import java.util.List;

/**
 * Created by sagar on 11/4/17.
 */
public class SingleBranch {

    public static List<Bank.InitBranch.Branch> branchList;
    private static int balance;
    private String name;
    private String ip;
    private int port;

    public SingleBranch() {
    }

    public SingleBranch(int balance, String name, String ip, int port, List<Bank.InitBranch.Branch> branchList) {
        SingleBranch.balance = balance;
        this.name = name;
        this.ip = ip;
        this.port = port;
        SingleBranch.branchList = branchList;
    }

    public static synchronized int getBalance() {
        return balance;
    }

    public static void setBalance(int balance) {
        SingleBranch.balance = balance;
    }

    public static synchronized int AddBalance(int amount) {
        balance = balance + amount;
        return balance;
    }

    public static synchronized int SubtractBalance(int amount) {
        balance = balance - amount;
        return balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<Bank.InitBranch.Branch> getBranchList() {
        return branchList;
    }

    public void setBranchList(List<Bank.InitBranch.Branch> branchList) {
        this.branchList = branchList;
    }

    @Override
    public String toString() {
        return "SingleBranch{" +
                "balance=" + balance +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
