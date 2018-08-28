import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by sagar on 11/10/17.
 */
public class SendMarkerMessageThread extends Thread {

    public SendMarkerMessageThread(int snapShotId) {
        this.snapShotId = snapShotId;
    }

    @Override
    public void run() {
        Random random = new Random();
        try {
            TimeUnit.MILLISECONDS.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(String branch: BranchServer.connectionMaps.keySet()) {
            Bank.Marker marker = Bank.Marker.newBuilder()
                    .setSnapshotId(snapShotId)
                    .build();
            Bank.BranchMessage branchMessage = Bank.BranchMessage.newBuilder()
                    .setMarker(marker)
                    .build();
            try {
                branchMessage.writeDelimitedTo(BranchServer.connectionMaps.get(branch).getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("\nMarker Message Sent with SnapShot ID: " + snapShotId);
        }
    }

    private int snapShotId;
}
