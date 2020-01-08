import javax.swing.*;

public class TestStart {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            Startoptionen s = Startoptionen.getInstance();

            Utilities.setServerIp("localhost");
            Utilities.setShowGui(true);

            switch (4) {
                case 0:
                    s.startWithGui();
                    break;
                case 1:
                    s.startOnConsole(args);
                    break;
                case 2:
                    s.startPeer(3333);
                    break;
                case 3:
                    s.startServerAndOnePeer();
                    break;
                case 4:
                    s.startManyPeers(true, 30);
            }

        } catch (Exception e) {
            Utilities.staticErrorMessage(e);
        }
    }
}
