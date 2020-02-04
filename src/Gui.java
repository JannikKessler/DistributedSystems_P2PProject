import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

@SuppressWarnings("FieldCanBeLocal")
public class Gui extends JFrame {

    private JTable peerTable;
    private Peer peer;

    private JPanel contentPane;
    private JPanel mainPanel;
    private JPanel listPanel;
    private JScrollPane scrollPane;
    private JPanel searchOuterPanel;
    private JPanel searchPanel;
    private JPanel searchPanelHor;
    private JSplitPane topPanel;
    private JPanel bottomPanel;
    private JSplitPane splitPanel;
    private JSplitPane bottomSplitPanel;
    private JPanel chatPanel;
    private JPanel consolePanel;
    private JScrollPane scrollPaneChat;
    private JScrollPane scrollPaneConsole;
    private JPanel msgPanel;
    private JPanel msgSendPanel;

    private JLabel labelBold;
    private JLabel labelThin;
    private JLabel timeLabel;
    private JLabel labelRight;
    private JTextField searchField;
    private JButton searchButton;
    private JTextArea chatArea;
    private JTextArea consoleArea;
    private JTextField msgField;
    private JButton msgSendButton;
    private JButton leaderButton;
    private JButton timeSynButton;
    private JLabel leaderLabel;
    private JTextField msgIDField;
    private JLabel msgIDLabel;

    private static String leaderText = "Derzeitiger Leader: ";

    public Gui(Peer peer) {

        setTitle((peer.isServer) ? "Server" : "Peer");
        setSize(Utilities.getGuiSize());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.peer = peer;

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                peer.exit();
                if (Startoptionen.getInstance().alwaysExitOnClose || JOptionPane.showConfirmDialog(null, "Komplettes Programm beenden?", "Schließen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        // ContentPane
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // MainPanel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);

        // Headline
        JPanel headlinePanel = new JPanel();
        headlinePanel.setLayout(new BoxLayout(headlinePanel, BoxLayout.X_AXIS));

        labelBold = new JLabel(getTitle() + "  ");
        labelBold.setFont(Utilities.getHeadlineFont());
        labelThin = new JLabel();
        labelThin.setFont(Utilities.getHeadlineFontThin());
        labelRight = new JLabel();
        labelRight.setFont(Utilities.getHeadlineFont());

        headlinePanel.add(labelBold);
        headlinePanel.add(labelThin);
        headlinePanel.add(Box.createHorizontalGlue());
        timeLabel = new JLabel();
        timeLabel.setFont(Utilities.getHeadlineFontThin());
        headlinePanel.add(timeLabel);


        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                long time = System.currentTimeMillis() + peer.getTimeOffset();
                Date date = new Date(time);
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                String dateFormatted = formatter.format(date);

                timeLabel.setText(dateFormatted + "  ");
            }
        });
        timer.start();

        headlinePanel.add(labelRight);
        mainPanel.add(headlinePanel, BorderLayout.NORTH);

        // PeerList
        listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableModel.addColumn("ID");
        tableModel.addColumn("IP");
        tableModel.addColumn("Port");

        peerTable = new JTable(tableModel) {

            public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    // label.setHorizontalAlignment(JLabel.CENTER);
                    label.setVerticalAlignment(JLabel.CENTER);
                }
                return c;
            }
        };

        peerTable.setFont(Utilities.getNormalFont());
        peerTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        peerTable.getColumnModel().getColumn(2).setPreferredWidth(0);
        peerTable.setShowGrid(false);
        peerTable.setFocusable(false);
        peerTable.setRowSelectionAllowed(false);
        peerTable.setRowHeight(Utilities.getNormalFont().getSize() + 6);

        scrollPane = new JScrollPane(peerTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.setBorder(new EmptyBorder(0, 0, 0, 10));

        // Search
        searchOuterPanel = new JPanel();
        searchPanel = new JPanel();
        searchOuterPanel.setLayout(new BoxLayout(searchOuterPanel, BoxLayout.Y_AXIS));
        searchPanel.setLayout(new GridLayout(4, 1));


        searchField = new JTextField();
        searchField.setToolTipText("Geben Sie eine Peer-ID ein.");
        searchField.setFont(Utilities.getNormalFont());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkSearchButton();
            }
        });


        searchButton = new JButton("Nach ID suchen");
        searchButton.setToolTipText("Suchen Sie im P2P-Netzwerk nach der eingegebenen ID.");
        searchButton.setFont(Utilities.getNormalFont());
        searchButton.setEnabled(false);

        searchButton.addActionListener(e -> searchForID());

        searchPanelHor = new JPanel();
        searchPanelHor.setLayout(new BoxLayout(searchPanelHor, BoxLayout.X_AXIS));

        leaderButton = new JButton("Leader-Election starten");
        leaderButton.setToolTipText("Führen Sie eine \"Leader-Election\" aus.");
        leaderButton.setFont(Utilities.getNormalFont());
        leaderLabel = new JLabel(leaderText + "-", SwingConstants.CENTER);
        leaderLabel.setFont(Utilities.getNormalFont());
        leaderButton.addActionListener(e -> startLeaderElection());

        timeSynButton = new JButton("TimeSyn starten");
        timeSynButton.setToolTipText("Führen Sie eine Zeit-Synchronisation aus (Nur möglich, wenn sie Leader sind).");
        timeSynButton.setFont(Utilities.getNormalFont());
        timeSynButton.addActionListener(e -> startTimeSyn());
        timeSynButton.setEnabled(false);

        searchPanelHor.add(searchField);
        searchPanelHor.add(searchButton);
        searchPanel.add(searchPanelHor);
        searchPanel.add(leaderLabel);
        searchPanel.add(leaderButton);
        searchPanel.add(timeSynButton);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        searchPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        searchOuterPanel.add(searchPanel);

        // TopPanel
        topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, searchOuterPanel);
        topPanel.setOneTouchExpandable(true);
        topPanel.setDividerLocation((int) (getWidth() * 0.55));
        listPanel.setMinimumSize(new Dimension(120, 0));
        searchOuterPanel.setMinimumSize(new Dimension(150, 0));
        topPanel.setBorder(null);
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // BottomPanel
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setFont(Utilities.getNormalFont());
        DefaultCaret caret = (DefaultCaret) chatArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);
        chatArea.append("Chat:");
        scrollPaneChat = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatPanel.add(scrollPaneChat, BorderLayout.CENTER);

        consolePanel = new JPanel();
        consolePanel.setLayout(new BorderLayout());
        consoleArea = new JTextArea();
        consoleArea.setFont(Utilities.getNormalFont());
        DefaultCaret caret2 = (DefaultCaret) consoleArea.getCaret();
        caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);
        consoleArea.setEditable(false);
        consoleArea.append("Konsole:");
        scrollPaneConsole = new JScrollPane(consoleArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        consolePanel.add(scrollPaneConsole, BorderLayout.CENTER);


        bottomSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, consolePanel);
        bottomSplitPanel.setOneTouchExpandable(true);
        bottomSplitPanel.setDividerLocation((int) (getWidth() * 0.55));
        chatPanel.setMinimumSize(new Dimension(120, 0));
        consolePanel.setMinimumSize(new Dimension(120, 0));
        bottomSplitPanel.setBorder(null);

        bottomPanel.add(bottomSplitPanel, BorderLayout.CENTER);


        // MsgPanel
        msgPanel = new JPanel();
        msgPanel.setLayout(new BorderLayout());
        bottomPanel.add(msgPanel, BorderLayout.SOUTH);
        msgField = new JTextField();
        msgField.setFont(Utilities.getNormalFont());
        msgPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        msgPanel.add(msgField, BorderLayout.CENTER);

        msgField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {

                if (msgSendButton.isEnabled() && e.getKeyCode() == KeyEvent.VK_ENTER)
                    sendMsg();

                checkSendButton();
            }
        });

        msgSendPanel = new JPanel();
        msgSendPanel.setLayout(new BoxLayout(msgSendPanel, BoxLayout.X_AXIS));
        msgPanel.add(msgSendPanel, BorderLayout.SOUTH);
        msgSendButton = new JButton("Senden");
        msgIDField = new JTextField();
        msgIDField.setFont(Utilities.getNormalFont());
        msgIDField.setMaximumSize(new Dimension(50, Integer.MAX_VALUE));
        msgIDField.setMinimumSize(new Dimension(50, Integer.MAX_VALUE));
        msgIDField.setPreferredSize(new Dimension(50, 10));
        msgIDLabel = new JLabel("An ID schicken:  ");
        msgIDLabel.setFont(Utilities.getNormalFont());

        msgIDField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkSendButton();
            }
        });

        msgSendPanel.add(msgIDLabel);
        msgSendPanel.add(msgIDField);
        msgSendPanel.add(Box.createHorizontalGlue());
        msgSendPanel.add(msgSendButton);
        msgSendButton.setFont(Utilities.getNormalFont());
        msgSendButton.setPreferredSize(new Dimension(100, 30));
        msgSendButton.setEnabled(false);

        msgSendButton.addActionListener(e -> sendMsg());

        // SplitLayout
        //noinspection SuspiciousNameCombination
        splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPanel.setOneTouchExpandable(true);
        splitPanel.setDividerLocation(130);
        topPanel.setMinimumSize(new Dimension(0, 110));
        bottomPanel.setMinimumSize(new Dimension(0, 150));
        splitPanel.setBorder(null);
        mainPanel.add(splitPanel, BorderLayout.CENTER);

        Point p = peer.getLocation();
        if (p == null)
            setLocationRelativeTo(null);
        else
            setLocation(p);

        setVisible(true);
    }

    public void setPeerList(ArrayList<PeerObject> peerListe) {

        DefaultTableModel tableModel = (DefaultTableModel) peerTable.getModel();
        if (tableModel.getRowCount() > 0) {
            for (int i = tableModel.getRowCount() - 1; i > -1; i--) {
                tableModel.removeRow(i);
            }
        }
        tableModel.setRowCount(0);

        for (PeerObject peerObject : peerListe) {
            String[] data = new String[3];

            data[0] = "" + peerObject.getIdAsInt();
            data[1] = peerObject.getIpAsString();
            data[2] = "" + peerObject.getPortAsInt();

            tableModel.addRow(data);
        }
        tableModel.fireTableDataChanged();

        repaint();
        revalidate();
    }

    private void searchForID() {
        int id = getFieldID(searchField);
        if (id != -1) {
            searchField.setText("");
            peer.startSearch(id);
        }
        checkSearchButton();
    }

    private int getFieldID(JTextField tf) {
        if (tf.getText().equals("")) {
            return -1;
        }
        try {
            int id = Integer.parseInt(tf.getText().trim());
            if (id <= 65535)
                return id;
            else
                return -1;
        } catch (Exception e) {
            return -1;
        }
    }


    private void checkSendButton() {
        if (getFieldID(msgIDField) != -1 && !msgField.getText().equals("")) {
            msgSendButton.setEnabled(true);
        } else {
            msgSendButton.setEnabled(false);
        }
    }

    private void checkSearchButton() {
        if (getFieldID(searchField) == -1) {
            searchButton.setEnabled(false);
        } else {
            searchButton.setEnabled(true);
        }
    }

    private void sendMsg() {

        int id = getFieldID(msgIDField);
        String msg = msgField.getText();

        peer.sendMsg(msg, id);
        msgField.setText("");
        checkSendButton();
    }

    private void startLeaderElection() {
        peer.startLeaderElection(true);
    }

    private void startTimeSyn(){
        peer.startTimeSyn();
    }

    public void addTextToChat(String txt) {
        chatArea.append("\n" + txt);
    }

    public void addTextToConsole(String txt) {
        consoleArea.append("\n" + txt);
    }

    public void setLeaderId(int id) {
        leaderLabel.setText(leaderText + id);
        if(getLeaderId() == peer.getMyPeer().getIdAsInt()){
            timeSynButton.setEnabled(true);
        } else {
            timeSynButton.setEnabled(false);
        }
    }

    public int getLeaderId() {
        try {
            return Integer.parseInt(leaderLabel.getText().replace(leaderText, ""));
        } catch (Exception e) {
            //Returnwert liefert Fehler
            return -1;
        }
    }

    public void setHeadline(String s, String ipAsString, int portAsInt, int idAsInt) {
        labelBold.setText(s + "  ");
        labelThin.setText(ipAsString + ":" + portAsInt);
        labelRight.setText("ID: " + idAsInt);
        setTitle(s + ", " + ipAsString + ":" + portAsInt + ", ID: " + idAsInt);
    }
}
