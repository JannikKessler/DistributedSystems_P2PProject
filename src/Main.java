import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());


            //startFew();
            startMany();


        } catch (Exception e) {
            Utilities.errorMessage(e);
        }
    }

    private static void startFew() throws Exception {
        Thread t = new Thread(() -> startServer());
        t.start();

        Thread.sleep(1000);

        startPeer(3334);
    }

    private static void startPeer(int port) {
        Peer p = new Peer(port);
        p.startPeer();
    }

    private static void startServer() {
        Peer s = new Peer();
        s.startPeer();
    }

    private static void startMany() throws Exception {

        Thread st = new Thread(() -> {
            Peer server = new Peer();
            server.startPeer();
        });
        st.start();

        Thread.sleep(1500);

        int maxNebeneinander, maxUntereinander;

        maxNebeneinander = Utilities.getScreenDimension().width / (Utilities.getGuiSize().width + 20);
        maxUntereinander = Utilities.getScreenDimension().height / (Utilities.getGuiSize().height + 20);

        AtomicInteger port = new AtomicInteger(3334);

        for (int i = 0; i < 2/*maxNebeneinander*/; i++) {
            for (int j = 0; j < 3/*maxUntereinander*/; j++) {

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
                Thread.sleep(2000);
            }
        }

        while (true) Thread.sleep(1000);

    }
}
