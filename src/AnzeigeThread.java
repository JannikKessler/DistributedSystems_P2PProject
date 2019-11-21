import java.util.ArrayList;

public class AnzeigeThread extends Thread {

    private Gui gui;
    private ArrayList<PeerObject> peerList;

    public AnzeigeThread(Gui gui, ArrayList<PeerObject> peerList) {
        this.gui = gui;
        this.peerList = peerList;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Utilities.printPeerList(gui, peerList, false);
                Thread.sleep(Utilities.getScreenUpdateTime());
            }
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }
}
