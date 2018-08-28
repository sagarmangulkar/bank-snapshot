import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by sagar on 11/9/17.
 */
public class TransferMessageSendingThread extends Thread {
    private Map<String, Socket> connectionMaps;
    public TransferMessageSendingThread(Map<String, Socket> connectionMaps) {
        this.connectionMaps = connectionMaps;
    }

    @Override
    public void run() {
        Socket clientSocket = null;
        try {
            Random random = new Random();
            while (true) {
                int amountToBeTransfer = (int) ((SingleBranch.getBalance() * ((random.nextInt(5) + 1)/100.0)));
                for (String branchName: connectionMaps.keySet()) {
                    TimeUnit.SECONDS.sleep(random.nextInt(5) + 1);
                    clientSocket = connectionMaps.get(branchName);
                    Bank.Transfer transfer = Bank.Transfer.newBuilder()
                            .setMoney(amountToBeTransfer)
                            .build();
                    Bank.BranchMessage branchMessage = Bank.BranchMessage.newBuilder()
                            .setTransfer(transfer)
                            .build();
                    int updatedBalance = SingleBranch.SubtractBalance(amountToBeTransfer);
                    branchMessage.writeDelimitedTo(clientSocket.getOutputStream());
                    System.out.println("\nTransfer Message Sent with money: " + amountToBeTransfer
                            + "\tBalance: " + updatedBalance);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
