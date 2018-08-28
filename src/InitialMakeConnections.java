import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * Created by sagar on 11/9/17.
 */
public class InitialMakeConnections extends Thread {

    private ServerSocket serverSocket;
    private List<Bank.InitBranch.Branch> branchList;
    private int currentPort;
    private String branchName;
    private Map<String, Socket> connectionMaps;

    public InitialMakeConnections(ServerSocket serverSocket, List<Bank.InitBranch.Branch> branchList, int currentPort, String branchName, Map<String, Socket> connectionMaps) {
        this.serverSocket = serverSocket;
        this.branchList = branchList;
        this.currentPort = currentPort;
        this.branchName = branchName;
        this.connectionMaps = connectionMaps;
    }

    @Override
    public void run() {
        MakeConnections();
    }

    private void MakeConnections() {
        //identifying for current index
        int branchIndex = 0;
        for (int i = 0; i < branchList.size(); i++) {
            if (branchList.get(i).getName().equals(branchName)){
                branchIndex = i;
                System.out.println("Branch Index: " + branchIndex);
            }
        }
        // Loop from self index + 1
        /*
        Logic:
        2's perspective
        3, 4
         */
        for (int i = branchIndex + 1; i < branchList.size(); i++) {
            Socket clientSocket = null;
            try {
                clientSocket = new Socket(branchList.get(i).getIp(), branchList.get(i).getPort());
                OutputStream os = clientSocket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(BranchServer.branchName + "\n");
                bw.flush();
                connectionMaps.put(branchList.get(i).getName(), clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
