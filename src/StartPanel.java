import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StartPanel extends JFrame {

    private Startoptionen so;
    private JTextField txtHeight;
    private JTextField txtWidth;
    private JTextField txtTimeBetweenPeerStarts;
    private JTextField txtMaxPeersInNetwork;
    private JTextField txtServerIp;

    public StartPanel(Startoptionen so) {

        this.so = so;

        setTitle("Startoptionen");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JPanel panelHeadline = new JPanel(new BorderLayout());
        contentPane.add(panelHeadline, BorderLayout.NORTH);

        JLabel lblHeadline = new JLabel("Startoptionen");
        lblHeadline.setFont(Utilities.getHeadlineFont());
        panelHeadline.add(lblHeadline, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        contentPane.add(mainPanel, BorderLayout.CENTER);

        JSeparator sp01 = new JSeparator();
        sp01.setVisible(false);
        mainPanel.add(sp01);

        JSeparator sp02 = new JSeparator();
        sp02.setVisible(false);
        mainPanel.add(sp02);

        JLabel lblServerIP = new JLabel("Server-IP");
        lblServerIP.setFont(Utilities.getNormalFont());
        mainPanel.add(lblServerIP);

        txtServerIp = new JTextField(Utilities.getServerIp());
        txtServerIp.setFont(Utilities.getNormalFont());
        mainPanel.add(txtServerIp);

        JLabel lblHeight = new JLabel("Höhe");
        lblHeight.setFont(Utilities.getNormalFont());
        mainPanel.add(lblHeight);

        txtHeight = new JTextField("" + Utilities.getGuiSize().height);
        txtHeight.setFont(Utilities.getNormalFont());
        mainPanel.add(txtHeight);

        JLabel lblWidth = new JLabel("Breite");
        lblWidth.setFont(Utilities.getNormalFont());
        mainPanel.add(lblWidth);

        txtWidth = new JTextField("" + Utilities.getGuiSize().width);
        txtWidth.setFont(Utilities.getNormalFont());
        mainPanel.add(txtWidth);

        JLabel lblTimeBetweenPeerStarts = new JLabel("Zeit zwischen Peerstarts");
        lblTimeBetweenPeerStarts.setFont(Utilities.getNormalFont());
        mainPanel.add(lblTimeBetweenPeerStarts);

        txtTimeBetweenPeerStarts = new JTextField("" + Variables.getIntValue("time_between_peer_starts"));
        txtTimeBetweenPeerStarts.setFont(Utilities.getNormalFont());
        mainPanel.add(txtTimeBetweenPeerStarts);

        JLabel lblMaxPeersInNetwork = new JLabel("Max. Peers in Network");
        lblMaxPeersInNetwork.setFont(Utilities.getNormalFont());
        mainPanel.add(lblMaxPeersInNetwork);

        txtMaxPeersInNetwork = new JTextField("" + Variables.getIntValue("max_peers_in_network"));
        txtMaxPeersInNetwork.setFont(Utilities.getNormalFont());
        mainPanel.add(txtMaxPeersInNetwork);

        JSeparator sp11 = new JSeparator();
        sp11.setVisible(false);
        mainPanel.add(sp11);

        JSeparator sp12 = new JSeparator();
        sp12.setVisible(false);
        mainPanel.add(sp12);

        JButton btnLocalServer = new JButton("Lokaler Server");
        btnLocalServer.setFont(Utilities.getNormalFont());
        btnLocalServer.addActionListener(e -> end(0));
        mainPanel.add(btnLocalServer);

        JButton btnOnePeer = new JButton("Ein Peer");
        btnOnePeer.setFont(Utilities.getNormalFont());
        btnOnePeer.addActionListener(e -> end(1));
        mainPanel.add(btnOnePeer);

        JButton btnManyPeers = new JButton("Viele Peers");
        btnManyPeers.setFont(Utilities.getNormalFont());
        btnManyPeers.addActionListener(e -> end(2));
        mainPanel.add(btnManyPeers);

        JButton btnOnePeerWithServer = new JButton("Lokaler Server + ein Peer");
        btnOnePeerWithServer.setFont(Utilities.getNormalFont());
        btnOnePeerWithServer.addActionListener(e -> end(3));
        mainPanel.add(btnOnePeerWithServer);

        JButton btnServerAndManyPeers = new JButton("Lokaler Server + viele Peers");
        btnServerAndManyPeers.setFont(Utilities.getNormalFont());
        btnServerAndManyPeers.addActionListener(e -> end(4));
        mainPanel.add(btnServerAndManyPeers);

        pack();
        setLocationRelativeTo(null);
    }

    private void end(int option) {

        dispose();

        try {
            Utilities.setServerIp(txtServerIp.getText());
            Variables.putObject("gui_size", new Dimension(Integer.parseInt(txtWidth.getText()), Integer.parseInt(txtHeight.getText())));
            Variables.putObject("time_between_peer_starts", Integer.parseInt(txtTimeBetweenPeerStarts.getText()));
            Variables.putObject("max_peers_in_network", Integer.parseInt(txtMaxPeersInNetwork.getText()));

            Thread t = new Thread(() -> {
                switch (option) {
                    case 0:
                        so.alwaysExitOnClose = true;
                        so.startServer();
                        break;
                    case 1:
                        so.alwaysExitOnClose = true;
                        so.startPeer(Integer.parseInt(JOptionPane.showInputDialog("Bitte Port eingeben")));
                        break;
                    case 2:
                        so.startManyPeers(false, Integer.parseInt(JOptionPane.showInputDialog("Anzahl Peers")));
                        break;
                    case 3:
                        so.startServerAndOnePeer();
                        break;
                    case 4:
                        so.startManyPeers(true, Integer.parseInt(JOptionPane.showInputDialog("Anzahl Peers")));
                        break;
                    default:
                        Utilities.staticErrorMessage(new Exception("Fehler in Startauswahl"));
                        break;
                }
            });
            t.start();
        } catch (Exception e) {
            Utilities.staticErrorMessage(e);
        }
    }
}
