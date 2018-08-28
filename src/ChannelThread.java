import java.io.IOException;
import java.net.Socket;

/**
 * Created by sagar on 11/10/17.
 */
public class ChannelThread extends Thread {

    private BranchServer branchServerObj;
    private String branchName;
    private Socket socket;

    public ChannelThread(BranchServer branchServerObj, String branchName, Socket socket) {
        this.branchServerObj = branchServerObj;
        this.branchName = branchName;
        this.socket = socket;
    }

    @Override
    public void run() {
        BranchServer.isFirstMarker = true;
        while (true) {
            try {
                Bank.BranchMessage branchMessage = null;
                branchMessage = Bank.BranchMessage.parseDelimitedFrom(socket.getInputStream());
                System.out.println("\nBranch Message Type: " + branchMessage.getBranchMessageCase());

                switch (branchMessage.getBranchMessageCase()) {
                    case TRANSFER:
                        //receiving transfer message
                        Thread r4 = new TransferMessageReceivingThread(branchMessage, socket);
                        r4.start();
                        break;
                    case MARKER:
                        BranchServer.snapShotId = branchMessage.getMarker().getSnapshotId();

                        if (!BranchServer.stateHashMap.containsKey(BranchServer.snapShotId)) {
                            BranchServer.isFirstMarker = false;
                            System.out.println("1) Marker Message Received for the first time...! " + BranchServer.snapShotId);
                            //record own state and mark incoming channel state as empty
                            BranchServer.stateHashMap.put(BranchServer.snapShotId, new BranchState(SingleBranch.getBalance()));
                            //send marker messages to other branches
                            System.out.println("StateHashMap: " + BranchServer.stateHashMap.keySet().toString());
                            BranchServer.recordingStateOfChannelHashMap.replaceAll((k, v) -> Boolean.TRUE);
                            BranchServer.SendMarkerMessageFunction(BranchServer.snapShotId, BranchServer.stateHashMap);
                        } else {
                            System.out.println("2) Marker Message Received second or later time...! " + BranchServer.snapShotId);
                            //record/update incoming channel state only
                            BranchServer.recordingStateOfChannelHashMap.replace(BranchServer.branchesSocketsMapping.get(socket), false);
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
