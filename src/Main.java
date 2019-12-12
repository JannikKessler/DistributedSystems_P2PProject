import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static boolean startFromConsole = false;

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());


            //startFew();
            //startMany();
            //startPeer(3333, true);
            startOnConsole(args);


        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    public static boolean isStartFromConsole() {
        return startFromConsole;
    }

    private static void startOnConsole(String[] args) {

        Variables.putObject("server_ip", args[0]);
        startPeer(Integer.parseInt(args[1]), Boolean.parseBoolean(args[2]));
        startFromConsole = true;
    }

    private static void startFew() throws Exception {
        Thread t = new Thread(() -> startServer());
        t.start();

        Thread.sleep(1000);

        startPeer(3334, true);
    }

    private static void startPeer(int port, boolean showGui) {
        Peer p = new Peer(port);
        p.startPeer(showGui);
    }

    private static void startServer() {
        Variables.putObject("server_ip", "localhost");
        Peer s = new Peer();
        s.startPeer(true);
    }

    private static void startMany() throws Exception {

        Thread st = new Thread(() -> {
            Peer server = new Peer();
            server.startPeer(true);
        });
        st.start();

        Thread.sleep(1500);

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
                    p.startPeer(true);
                });
                t.start();
                Thread.sleep(1000);
            }
        }

        while (true) Thread.sleep(1000);

    }
}
