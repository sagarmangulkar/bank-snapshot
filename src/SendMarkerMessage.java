import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by sagar on 11/5/17.
 */
public class SendMarkerMessage implements Runnable {

    public SendMarkerMessage(int snapShotId) {
        this.snapShotId = snapShotId;
    }

    public static void SendPortNumber(Socket socket) {
        //Send the message to the server            
        OutputStream os = null;
        try {
            os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            String portNumber = Integer.toString(BranchServer.currentPort);
            bw.write(portNumber);
            bw.flush();
            System.out.println("Port in Text Message sent: " + portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    Bank.Marker marker = Bank.Marker.newBuilder()
                            .setSnapshotId(snapShotId)
                            .build();
                    Bank.BranchMessage branchMessage = Bank.BranchMessage.newBuilder()
                            .setMarker(marker)
                            .build();
                    branchMessage.writeDelimitedTo(clientSocket.getOutputStream());
                    System.out.println("\nMarker Message Sent with SnapShot ID: " + snapShotId);
                    SendPortNumber(clientSocket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int snapShotId;
}
