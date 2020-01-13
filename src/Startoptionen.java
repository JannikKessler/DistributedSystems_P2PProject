import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Startoptionen {

    private static Startoptionen instance;
    public boolean alwaysExitOnClose = false;

    private Startoptionen() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("OptionPane.cancelButtonText", "Abbrechen");
            UIManager.put("OptionPane.noButtonText", "Nein");
            UIManager.put("OptionPane.okButtonText", "Ok");
            UIManager.put("OptionPane.yesButtonText", "Ja");

            Utilities.setServerIp("localhost");
            Utilities.setShowGui(true);
        } catch (Exception e) {
            Utilities.staticErrorMessage(e);
        }
    }

    public static Startoptionen getInstance() {
        if (instance == null)
            instance = new Startoptionen();
        return instance;
    }

    public void startWithGui() {

        StartPanel sp = new StartPanel(this);
        sp.setVisible(true);
    }

    public void startOnConsole(String[] args) {
        alwaysExitOnClose = true;
        Utilities.setServerIp(args[0]);
        startPeer(Integer.parseInt(args[1]));
        Variables.putObject("show_gui", Boolean.parseBoolean(args[2]));
    }

    public void startServerAndOnePeer() {
        startLocalServer();
        startPeer(3334);
    }

    public void startPeer(int port) {
        Peer p = new Peer(port);
        p.startPeer();
    }

    public void startLocalServer() {
        try {
            Utilities.setServerIp("localhost");
            Thread st = new Thread(() -> {
                Peer server = new Peer();
                server.startPeer();
            });
            st.start();
            Thread.sleep(1000);
        } catch (Exception e) {
            Utilities.staticErrorMessage(e);
        }
    }

    public void startManyPeers(boolean withServer, int numberOfPeers) {

        try {

            int nebeneinander = Utilities.getScreenDimension().width / (Utilities.getGuiSize().width + 10);
            int untereinander = Utilities.getScreenDimension().height / (Utilities.getGuiSize().height + 10);

            while (nebeneinander * untereinander < numberOfPeers) {
                nebeneinander++;
                untereinander++;
            }

            AtomicInteger port = new AtomicInteger(3333);

            if (!withServer)
                port.getAndIncrement();

            int k = 0;

            for (int i = 0; i < nebeneinander; i++) {
                for (int j = 0; j < untereinander; j++) {

                    int w = Utilities.getScreenDimension().width / nebeneinander;
                    int h = Utilities.getScreenDimension().height / untereinander;

                    if (k < numberOfPeers) {

                        AtomicInteger stelleX = new AtomicInteger(i);
                        AtomicInteger stelleY = new AtomicInteger(j);

                        Thread t = new Thread(() -> {

                            int x, y;

                            x = stelleX.get() * (w + 10);
                            y = stelleY.get() * (h + 10);

                            Point location = new Point(x, y);
                            Peer p = new Peer(port.getAndIncrement(), location);
                            p.startPeer();
                        });
                        t.start();
                        Thread.sleep(Variables.getIntValue("time_between_peer_starts"));

                        k++;
                    }
                }
            }

            //noinspection InfiniteLoopStatement
            while (true) Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            Utilities.staticErrorMessage(e);
        }
    }
}
