import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by sagar on 10/30/17.
 */

public class BranchServer {
    public static int currentPort;
    public static String currentIp;
    public static String branchName;
    public static Map<String, Socket> connectionMaps;
    public static Map<Socket, String> branchesSocketsMapping;
    public static HashMap<Integer, BranchState> stateHashMap;
    public static boolean isFirstMarker;
    public static HashMap<String, Boolean> recordingStateOfChannelHashMap = new HashMap<>();
    public static int snapShotId;

    public static void main(String[] args) {
        branchName = args[0];
        currentPort = Integer.parseInt(args[1]);
        ServerSocket serverSocket = null;
        BranchServer branchServerObj = new BranchServer();
        connectionMaps = new Hashtable<>();
        branchesSocketsMapping = new Hashtable<>();
        try {
            System.out.println(recordingStateOfChannelHashMap.toString());
            serverSocket = new ServerSocket(currentPort);
            currentIp = serverSocket.getInetAddress().getHostName();
            int port = serverSocket.getLocalPort();
            System.out.println(branchName + " " + currentIp + " " + port);
            System.out.println("Waiting for Controller...");
            Socket clientSocket = serverSocket.accept();

            //Receiving Init_Branch message
            Bank.BranchMessage initialBranchMessage = Bank.BranchMessage.parseDelimitedFrom(clientSocket.getInputStream());
            int balance = initialBranchMessage.getInitBranch().getBalance();
            List<Bank.InitBranch.Branch> branchList = initialBranchMessage.getInitBranch().getAllBranchesList();

            List<Integer> portList = new ArrayList<>();
            List<String> ipList = new ArrayList<>();
            for (int i = 0; i < branchList.size(); i++) {
                if (branchList.get(i).getPort() != currentPort){
                    portList.add(branchList.get(i).getPort());
                    ipList.add(branchList.get(i).getIp());
                }
            }

            Thread r1 = new InitialStoreConnections(serverSocket, branchList, currentPort, branchName, connectionMaps);
            r1.start();
            Thread r2 = new InitialMakeConnections(serverSocket, branchList, currentPort, branchName, connectionMaps);
            r2.start();

            r1.join();
            r2.join();
            //initial state of recording flag
            for (String branch: BranchServer.connectionMaps.keySet()){
                BranchServer.recordingStateOfChannelHashMap.put(branch, false);
            }
            for (String branch: connectionMaps.keySet()){
                System.out.println("Branch : " + branch + " and connectionMap: " + connectionMaps.get(branch));
                branchesSocketsMapping.put(connectionMaps.get(branch), branch);
            }

            SingleBranch currentBranch = new SingleBranch(balance, branchName, currentIp, port, branchList);
            //sending transfer message
            Thread r3 = new TransferMessageSendingThread(connectionMaps);
            r3.start();

            Thread r4 = null;
            for (String branchName: connectionMaps.keySet()) {
                r4 = new ChannelThread(branchServerObj, branchName, connectionMaps.get(branchName));
                r4.start();
            }
            System.out.println("Waiting Init_SnapShot...");
            Thread r5 = new ChannelControllerThread(clientSocket, serverSocket);
            r5.start();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void SendMarkerMessageFunction(int snapShotId, HashMap<Integer, BranchState> stateHashMap) {
        Thread r4 = new SendMarkerMessageThread(snapShotId);
        r4.start();
    }
}