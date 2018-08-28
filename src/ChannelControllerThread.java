import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by sagar on 11/10/17.
 */
public class ChannelControllerThread extends Thread {
    public ChannelControllerThread(Socket socket, ServerSocket serverSocket) {
        this.socket = socket;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        BranchServer.stateHashMap = new HashMap<>();
        try {
            while(true) {
                System.out.println("Waiting to receive...");
                Bank.BranchMessage controllerBranchMessage = Bank.BranchMessage.parseDelimitedFrom(socket.getInputStream());
                System.out.println("\nBranch Message Type: " + controllerBranchMessage.getBranchMessageCase());

                switch (controllerBranchMessage.getBranchMessageCase()) {
                    case INIT_SNAPSHOT:
                        BranchServer.isFirstMarker = false;
                        System.out.println("Socket: " + socket);
                        System.out.println("Init SnapShot Id: " + controllerBranchMessage);
                        int snapShotId = controllerBranchMessage.getInitSnapshot().getSnapshotId();
                        BranchServer.snapShotId = snapShotId;
                        System.out.println("Saving this balance: " + SingleBranch.getBalance());
                        BranchServer.stateHashMap.put(snapShotId, new BranchState(SingleBranch.getBalance()));
                        System.out.println("StateHashMap: " + BranchServer.stateHashMap.keySet().toString());
                        //send Marker message
                        BranchServer.recordingStateOfChannelHashMap.replaceAll((k, v) -> Boolean.TRUE);
                        Thread r4 = new SendMarkerMessageThread(snapShotId);
                        r4.start();
                        break;
                    case RETRIEVE_SNAPSHOT:
                        System.out.println("RETRIEVE_SNAPSHOT received...!");
                        System.out.println("State HashMap: " + BranchServer.stateHashMap.keySet().toString());
                        int snapShotIdToBeRetrieved = controllerBranchMessage.getRetrieveSnapshot().getSnapshotId();
                        Bank.ReturnSnapshot.LocalSnapshot.Builder temp = Bank.ReturnSnapshot.LocalSnapshot.newBuilder()
                                .setSnapshotId(snapShotIdToBeRetrieved)
                                .setBalance(BranchServer.stateHashMap.get(snapShotIdToBeRetrieved).getBranchBalance());

                        int i = 0;
                        for (String branchName : BranchServer.stateHashMap.get(snapShotIdToBeRetrieved).getChannelBalance().keySet()) {
                            int branchNumber = Integer.parseInt(branchName.substring(6));
                            for (int channelMoney : BranchServer.stateHashMap.get(snapShotIdToBeRetrieved).getChannelBalance().get(branchName)) {
                                temp.addChannelState(branchNumber);
                                temp.addChannelState(channelMoney);

                            }
                        }
                        Bank.ReturnSnapshot.LocalSnapshot localSnapshot = temp.build();
                        Bank.ReturnSnapshot returnSnapshot = Bank.ReturnSnapshot.newBuilder()
                                .setLocalSnapshot(localSnapshot)
                                .build();
                        System.out.println("RETRIEVE_SNAPSHOT sending...");
                        Bank.BranchMessage branchMessage = Bank.BranchMessage.newBuilder()
                                .setReturnSnapshot(returnSnapshot)
                                .build();
                        branchMessage.writeDelimitedTo(socket.getOutputStream());
                        //returnSnapshot.writeDelimitedTo(socket.getOutputStream());
                        System.out.println("RETRIEVE_SNAPSHOT sent.");

                        break;
                    case BRANCHMESSAGE_NOT_SET:
                        System.out.println("BRANCHMESSAGE_NOT_SET Received...!");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket socket;
    private ServerSocket serverSocket;
}
