import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by sagar on 11/3/17.
 */
public class ReceiveTransferMessage implements Runnable {

    private ServerSocket serverSocket;

    public ReceiveTransferMessage(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    //send Marker message to all other branches
    public static void SendMarkerMessage(int snapShotId) {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        Runnable runnable = new SendMarkerMessage(snapShotId);
        executorService.execute(runnable);
    }

    public static int ReceivePortNumber(Socket socketClient) {
        //Reading the message from the client
        InputStream is = null;
        int portNumber = 0;
        try {
            is = socketClient.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String portNumberString = br.readLine();
            portNumber = Integer.parseInt(portNumberString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return portNumber;
    }

    @Override
    public void run() {
        try {
            HashMap<Integer, BranchState> stateHashMap = new HashMap<>();
            List<Integer> channelMoney = new ArrayList<>();
            boolean isFirstMarker = true;

            //initial state of all channels
            HashMap<Integer, Boolean> recordingStateOfChannelHashMap = new HashMap<>();
            String ip;
            int port;
            boolean isRecording = false;
            for (int i = 0; i < SingleBranch.branchList.size(); i++) {
                ip = SingleBranch.branchList.get(i).getIp();
                port = SingleBranch.branchList.get(i).getPort();
                if (!(BranchServer.currentPort == port && BranchServer.currentIp.equals(ip))) {
                    recordingStateOfChannelHashMap.put(port, isRecording);
                }
            }
            int incomingChannelPort;
            boolean isInitSnapshotBranch = false;

            while (true) {
                System.out.println("\nReceiver waiting for other messages...");
                Socket clientSocket = serverSocket.accept();
                Bank.BranchMessage branchMessage = Bank.BranchMessage.parseDelimitedFrom(clientSocket.getInputStream());
                System.out.println("\nBranch Message Type: " + branchMessage.getBranchMessageCase());
                int snapShotId;

                switch (branchMessage.getBranchMessageCase()) {
                    case INIT_BRANCH:
                        break;
                    case TRANSFER:
                        int receivedMoney = branchMessage.getTransfer().getMoney();
                        int updatedBalance = SingleBranch.AddBalance(receivedMoney);
                        System.out.println("Transfer Message Received with money:" + receivedMoney + "\tBalance: " + updatedBalance);

                        //store the sequence of incoming money to new hashMap
                        channelMoney.add(receivedMoney);

                        break;
                    case INIT_SNAPSHOT:
                        snapShotId = branchMessage.getInitSnapshot().getSnapshotId();
                        System.out.println("Init Snapshot Message Received...! SnapShoot ID: "
                                + snapShotId);
                        //record own state
                        stateHashMap.put(snapShotId, new BranchState(SingleBranch.getBalance()));
                        //send marker messages to other branches
                        SendMarkerMessage(snapShotId);
                        isFirstMarker = false;
                        isInitSnapshotBranch = true;
                        recordingStateOfChannelHashMap.replaceAll((k, v) -> Boolean.TRUE);
                        break;
                    case MARKER:
                        snapShotId = branchMessage.getMarker().getSnapshotId();
                        if (isFirstMarker) {
                            System.out.println("Marker Message Received for the first time...!");
                            //record own state and mark incoming channel state as empty
                            stateHashMap.put(snapShotId, new BranchState(SingleBranch.getBalance()));
                            //send marker messages to other branches
                            SendMarkerMessage(snapShotId);
                            isFirstMarker = false;
                            isRecording = true;
                            //if its branch on which InitSnapShot is been called then stop recording
                            if (isInitSnapshotBranch){
                                isRecording = false;
                            }
                            incomingChannelPort = ReceivePortNumber(clientSocket);
                            if (recordingStateOfChannelHashMap.containsKey(incomingChannelPort)) {
                                recordingStateOfChannelHashMap.replace(incomingChannelPort, isRecording);
                            }
                        } else {
                            System.out.println("Marker Message Received second or later time...!");
                            int snapShotBalance = stateHashMap.get(snapShotId).getBranchBalance();
                            //clear the channel money
                            channelMoney = new ArrayList<>();
                            isRecording = false;
                            TimeUnit.SECONDS.sleep(2);
                            incomingChannelPort = ReceivePortNumber(clientSocket);
                            if (recordingStateOfChannelHashMap.containsKey(incomingChannelPort)) {
                                recordingStateOfChannelHashMap.replace(incomingChannelPort, isRecording);
                            }
                        }
                        break;
                    case RETRIEVE_SNAPSHOT:
                        break;
                    case RETURN_SNAPSHOT:
                        break;
                    case BRANCHMESSAGE_NOT_SET:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
