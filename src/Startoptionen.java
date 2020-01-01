import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Startoptionen {

    private static Startoptionen instance;

    private Startoptionen() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Utilities.setServerIp("localhost");
            Utilities.setShowGui(true);
        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    public static Startoptionen getInstance() {
        if (instance == null)
            instance = new Startoptionen();
        return instance;
    }

    public void startWithGui() {

        String[] optionen = {"Lokaler Server", "Ein Peer", "Lokaler Server + ein Peer", "Lokaler Server + viele Peers"};
        switch (JOptionPane.showOptionDialog(null, "Bitte Startoption auswählen", "Startoption wählen", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, optionen, 2)) {
            case 0:
                startServer();
                break;
            case 1:
                startPeer(Integer.parseInt(JOptionPane.showInputDialog("Bitte Port eingeben")));
                break;
            case 2:
                startServerAndOnePeer();
                break;
            case 3:
                startManyPeers(true);
                break;
            default:
                Utilities.fehlermeldungBenutzerdefiniert("Fehler in Startauswahl");
        }
    }

    public void startOnConsole(String[] args) {
        Utilities.setServerIp(args[0]);
        startPeer(Integer.parseInt(args[1]));
        Variables.putObject("show_gui", Boolean.parseBoolean(args[2]));
    }

    public void startServerAndOnePeer() {

        startServer();
        startPeer(3334);
    }

    public void startPeer(int port) {
        Peer p = new Peer(port);
        p.startPeer();
    }

    public void startServer() {

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

    public void startManyPeers(boolean withServer) {

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
