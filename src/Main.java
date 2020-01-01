import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            Utilities.setServerIp("localhost");
            Utilities.setShowGui(true);

            switch (0) {
                case 0:startWithGui();break;
                case 1:startOnConsole(args); break;
                case 2:startPeer(3334);break;
                case 3:startServerAndOnePeer();break;
                case 4:startManyPeers(true);
            }

        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    public static void startWithGui() {

        String[] optionen = {"Lokaler Server", "Lokaler Server + ein Peer", "Lokaler Server + viele Peers"};
        switch (JOptionPane.showOptionDialog(null, "Bitte Startoption auswählen", "Startoption wählen", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, optionen, 2)) {
            case 0:
                startServer();
                break;
            case 1:
                startServerAndOnePeer();
                break;
            case 2:
                startManyPeers(true);
                break;
            default:
                Utilities.fehlermeldungBenutzerdefiniert("Fehler in Startauswahl");
        }
    }

    private static void startOnConsole(String[] args) {
        Utilities.setServerIp(args[0]);
        startPeer(Integer.parseInt(args[1]));
        Variables.putObject("show_gui", Boolean.parseBoolean(args[2]));
    }

    private static void startServerAndOnePeer() {

        startServer();
        startPeer(3334);
    }

    private static void startPeer(int port) {
        Peer p = new Peer(port);
        p.startPeer();
    }

    private static void startServer() {

        try {
            Thread st = new Thread(() -> {
                Peer server = new Peer();
                server.startPeer();
            });
            st.start();
            Thread.sleep(1000);
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    private static void startManyPeers(boolean withServer) {

        try {

            int maxNebeneinander, maxUntereinander;

            maxNebeneinander = Utilities.getScreenDimension().width / (Utilities.getGuiSize().width + 20);
            maxUntereinander = Utilities.getScreenDimension().height / (Utilities.getGuiSize().height + 20);

            AtomicInteger port = new AtomicInteger(3333);

            if (!withServer)
                port.getAndIncrement();

            for (int i = 0; i < maxNebeneinander; i++) {
                for (int j = 0; j < maxUntereinander; j++) {

                    AtomicInteger stelleX = new AtomicInteger(i);
                    AtomicInteger stelleY = new AtomicInteger(j);

                    Thread t = new Thread(() -> {

                        int x, y;

                        x = 10 + stelleX.get() * (Utilities.getGuiSize().width + 10);
                        y = 10 + stelleY.get() * (Utilities.getGuiSize().height + 10);

                        Point location = new Point(x, y);
                        Peer p = new Peer(port.getAndIncrement(), location);
                        p.startPeer();
                    });
                    t.start();
                    Thread.sleep(500);
                }
            }

            while (true) Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }
}
