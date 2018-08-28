import java.io.IOException;
import java.net.Socket;

/**
 * Created by sagar on 11/3/17.
 */
public class SendTransferMessage implements Runnable {

    public SendTransferMessage(int amountToBeTransfer) {
        this.amountToBeTransfer = amountToBeTransfer;
    }

    @Override
    public void run() {
            try {
                String ip;
                int port;
                Socket clientSocket;
                for (int i = 0; i < SingleBranch.branchList.size(); i++) {
                    ip = SingleBranch.branchList.get(i).getIp();
                    port = SingleBranch.branchList.get(i).getPort();
                    if (!(BranchServer.currentPort == port && BranchServer.currentIp.equals(ip))) {
                        clientSocket = new Socket(ip, port);
                        Bank.Transfer transfer = Bank.Transfer.newBuilder()
                                .setMoney(amountToBeTransfer)
                                .build();
                        Bank.BranchMessage branchMessage = Bank.BranchMessage.newBuilder()
                                .setTransfer(transfer)
                                .build();
                        int updatedBalance = SingleBranch.SubtractBalance(amountToBeTransfer);
                        branchMessage.writeDelimitedTo(clientSocket.getOutputStream());
                        System.out.println("\nTransfer Message Sent with money: " + amountToBeTransfer + "\tBalance: " + updatedBalance);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private int amountToBeTransfer;
}
