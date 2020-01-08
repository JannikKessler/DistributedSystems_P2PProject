import java.util.ArrayList;

public class AnzeigeThread extends Thread {

    private Peer i;

    public AnzeigeThread(Peer i) {
        this.i = i;
    }

    @Override
    public void run() {
        try {
            while (true) {
                i.getPeerUtilities().printPeerList(i, false);
                Thread.sleep(Utilities.getScreenUpdateTime());
            }
        } catch (Exception e) {
            i.getPeerUtilities().errorMessage(e);
        }
    }
}
