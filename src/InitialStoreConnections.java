import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sagar on 11/9/17.
 */
public class InitialStoreConnections extends Thread {

//    private HashMap<Socket, Integer> incomingConnectionPortsHashMap;
//    private int branchListSize;

    private ServerSocket serverSocket;
    private List<Bank.InitBranch.Branch> branchList;
    private int currentPort;
    private String branchName;
    private Map<String, Socket> connectionMaps;

    public InitialStoreConnections(ServerSocket serverSocket, List<Bank.InitBranch.Branch> branchList, int currentPort, String branchName, Map<String, Socket> connectionMaps) {
        this.serverSocket = serverSocket;
        this.branchList = branchList;
        this.currentPort = currentPort;
        this.branchName = branchName;
        this.connectionMaps = connectionMaps;
    }

    @Override
    public void run() {
        StoreClientConnections();
    }

    private void StoreClientConnections() {
        Socket clientSocket = null;
        // Loop till self index
        for (int i = 0; i < branchList.size(); i++) {
            if (branchList.get(i).getName().equals(branchName)){
                break;
            }
            try {
                clientSocket = serverSocket.accept();
                InputStream is = clientSocket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                String branchNameReceived = br.readLine();
                //setting the connections
                connectionMaps.put(branchNameReceived, clientSocket);

                /*
                3's perspective
                1, 2
                map.put(bName, socket)
                 */
                System.out.println("Connections stored for branch: " + branchName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
