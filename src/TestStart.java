import javax.swing.*;

public class TestStart {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            Utilities.setServerIp("localhost");
            Utilities.setShowGui(true);

            Startoptionen s = Startoptionen.getInstance();

            switch (4) {
                case 0:
                    s.startWithGui();
                    break;
                case 1:
                    s.startOnConsole(args);
                    break;
                case 2:
                    s.startPeer(3334);
                    break;
                case 3:
                    s.startServerAndOnePeer();
                    break;
                case 4:
                    s.startManyPeers(true);
            }

        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }
}
