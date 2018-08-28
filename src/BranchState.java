import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sagar on 11/5/17.
 */
public class BranchState {

    private int branchBalance;
    private HashMap<String, List<Integer>> channelBalance;

    public BranchState(int branchBalance) {
        this.branchBalance = branchBalance;
        this.channelBalance = new HashMap<>();
        for (String branch : BranchServer.connectionMaps.keySet()) {
            channelBalance.put(branch, Collections.synchronizedList(new ArrayList<Integer>()));
        }
    }

    public synchronized HashMap<String, List<Integer>> getChannelBalance() {
        return channelBalance;
    }

    public synchronized void setChannelBalance(HashMap<String, List<Integer>> channelBalance) {
        this.channelBalance = channelBalance;
    }

    public synchronized int getBranchBalance() {
        return branchBalance;
    }

    public synchronized void setBranchBalance(int branchBalance) {
        this.branchBalance = branchBalance;
    }
}
