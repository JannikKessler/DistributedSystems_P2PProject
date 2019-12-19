import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static boolean exitOnWindowClose = false;

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            //Utilities.setShowGui(false);
            //startPeerWithVariablesServer(3334); //int Port
            //startLocalServerAndOnePeer();
            startManyLocalServer(true); //boolean withServer

            //startOnConsole(args);

        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    public static boolean isExitOnWindowClose() {
        return exitOnWindowClose;
    }

    private static void startOnConsole(String[] args) {
        exitOnWindowClose = true;
        Utilities.setServerIp(args[0]);
        startPeerWithVariablesServer(Integer.parseInt(args[1]));
        Variables.putObject("show_gui", Boolean.parseBoolean(args[2]));
    }

    private static void startLocalServerAndOnePeer() {

        startLocalServer();
        startPeerWithVariablesServer(3334);
    }

    private static void startPeerWithVariablesServer(int port) {
        Peer p = new Peer(port);
        p.startPeer();
    }

    private static void startLocalServer() {

        try {
            Variables.putObject("server_ip", "localhost");
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

    private static void startManyLocalServer(boolean withServer) throws Exception {

        if (withServer)
            startLocalServer();

        int maxNebeneinander, maxUntereinander;

        maxNebeneinander = Utilities.getScreenDimension().width / (Utilities.getGuiSize().width + 20);
        maxUntereinander = Utilities.getScreenDimension().height / (Utilities.getGuiSize().height + 20);

        AtomicInteger port = new AtomicInteger(3334);

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
                Thread.sleep(1000);
            }
        }

        while (true) Thread.sleep(Long.MAX_VALUE);
    }
}
