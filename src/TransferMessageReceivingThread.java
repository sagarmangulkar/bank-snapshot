import java.net.Socket;

/**
 * Created by sagar on 11/9/17.
 */
public class TransferMessageReceivingThread extends Thread {

    private Bank.BranchMessage branchMessage;
    private Socket socket;

    public TransferMessageReceivingThread(Bank.BranchMessage branchMessage, Socket socket) {
        this.branchMessage = branchMessage;
        this.socket = socket;
    }

    @Override
    public void run() {
        int receivedMoney = branchMessage.getTransfer().getMoney();
        SingleBranch.AddBalance(receivedMoney);
        if (BranchServer.recordingStateOfChannelHashMap.get(BranchServer.branchesSocketsMapping.get(socket))){
            String currentBranch = BranchServer.branchesSocketsMapping.get(socket);
            BranchServer.stateHashMap.get(BranchServer.snapShotId).getChannelBalance().get(currentBranch).add(receivedMoney);
        }
        System.out.println("Transfer Message Received with money:" + receivedMoney
                + "\tBalance: " + SingleBranch.getBalance());
    }
}
