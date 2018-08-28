import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by sagar on 10/31/17.
 */
public class Controller {
    public static void main(String[] args) {
        int totalAmount = Integer.parseInt(args[0]);
        String fileName = args[1];
        String fileContent = ReadFile(fileName);
        String[] branchesArray = fileContent.split("\\n");
        int numberOfBranches = branchesArray.length;
        int eachBranchAmount = totalAmount / numberOfBranches;
        Socket clientSocket = null;
        try {
            String[] singleBranch = null;
            String branchName, branchIP;
            int branchPort;

            HashMap<Integer, Boolean> blankChannels = new HashMap<>();
            //gathering all branches in one List
            List<Bank.InitBranch.Branch> branchList = new ArrayList<>();
            for (int i = 0; i < numberOfBranches; i++) {
                singleBranch = branchesArray[i].split(" ");
                branchName = singleBranch[0];
                branchIP = singleBranch[1];
                branchPort = Integer.parseInt(singleBranch[2]);

                Bank.InitBranch.Branch branch = Bank.InitBranch.Branch.newBuilder()
                        .setName(branchName)
                        .setIp(branchIP)
                        .setPort(branchPort)
                        .build();
                branchList.add(branch);
                blankChannels.put(i, false);
            }

            //Init_Branch message
            List<Socket> clientSocketList = new ArrayList<>();
            Bank.BranchMessage branchMessage;
            for (int i = 0; i < numberOfBranches; i++) {
                clientSocketList.add(new Socket(branchList.get(i).getIp(), branchList.get(i).getPort()));
                clientSocket = clientSocketList.get(i);
                Bank.InitBranch initBranch = Bank.InitBranch.newBuilder()
                        .setBalance(eachBranchAmount)
                        .addAllAllBranches(branchList)
                        .build();

                branchMessage = Bank.BranchMessage.newBuilder()
                        .setInitBranch(initBranch)
                        .build();
                branchMessage.writeDelimitedTo(clientSocket.getOutputStream());
            }

            Random random = new Random();
            int snapShotId = 0;
            while (true) {
                snapShotId++;
                TimeUnit.SECONDS.sleep(3);

                //Task: send initSnapshot
                int randomBranchInt = random.nextInt(branchList.size());
                clientSocket = clientSocketList.get(randomBranchInt);
                Bank.InitSnapshot initSnapshot = Bank.InitSnapshot.newBuilder()
                        .setSnapshotId(snapShotId)
                        .build();
                Bank.BranchMessage branchMessage1 = Bank.BranchMessage.newBuilder()
                        .setInitSnapshot(initSnapshot)
                        .build();
                branchMessage1.writeDelimitedTo(clientSocket.getOutputStream());

                //Task: send retrieveSnapshot
                TimeUnit.SECONDS.sleep(20);
                for (int i = 0; i < branchList.size(); i++) {
                    Bank.RetrieveSnapshot retrieveSnapshot = Bank.RetrieveSnapshot.newBuilder()
                            .setSnapshotId(snapShotId)
                            .build();
                    Bank.BranchMessage branchMessage2 = Bank.BranchMessage.newBuilder()
                            .setRetrieveSnapshot(retrieveSnapshot)
                            .build();
                    branchMessage2.writeDelimitedTo(clientSocketList.get(i).getOutputStream());
                }
                System.out.println("snapshot_Id: " + snapShotId);

                //Task: receiving Return_Snapshot message
                for (int i = 0; i < branchList.size(); i++) {
                    blankChannels.replaceAll((k, v) -> Boolean.FALSE);
                    Bank.BranchMessage branchMessage2 = Bank.BranchMessage.parseDelimitedFrom(clientSocketList.get(i).getInputStream());
                    Bank.ReturnSnapshot.LocalSnapshot localSnapshot = branchMessage2.getReturnSnapshot().getLocalSnapshot();
                    int balance = localSnapshot.getBalance();
                    List<Integer> channelStateList = localSnapshot.getChannelStateList();
                    System.out.print(branchList.get(i).getName()
                    + ": " + balance + ", ");
                    for (int j = 1; j <= (channelStateList.size()/2); j = j+2) {
                        System.out.print(/*"(" + (j-1) + ") " +*/ branchList.get(channelStateList.get(j-1)-1).getName()
                                + "->" + branchList.get(i).getName() + ": " + channelStateList.get(j) + ", ");
                        blankChannels.replace(channelStateList.get(j-1)-1, true);
                    }
                    for (int j = 0; j < branchList.size(); j++) {
                        if (!blankChannels.get(j) && i!=j){
                            System.out.print(branchList.get(j).getName()
                                    + "->" + branchList.get(i).getName() + ": 0" + ", ");
                        }
                    }
                    System.out.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String ReadFile(String fileName) {
        String fileContent = null;
        FileReader fileReader = null;
        BufferedReader bufferReader = null;
        try {
            fileReader = new FileReader("src/" + fileName);
            bufferReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            fileContent = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                bufferReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileContent;
    }
}
