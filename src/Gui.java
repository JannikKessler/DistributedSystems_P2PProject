import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

public class Gui extends JFrame {

    private JLabel lblHeadline;
    private JTable peerTable;
    private Peer peer;
    private boolean isServer;

    private JPanel contentPane;
    private JPanel mainPanel;
    private JPanel listPanel;
    private JScrollPane scrollPane;
    private JPanel searchOuterPanel;
    private JPanel searchPanel;
    private JSplitPane topPanel;
    private JPanel bottomPanel;
    private JSplitPane splitPanel;
    private JScrollPane scrollPaneChat;
    private JPanel msgPanel;
    private JPanel msgSendPanel;

    private JTextField searchField;
    private JButton searchButton;
    private JLabel searchText;
    private JTextArea chatArea;
    private JTextField msgField;
    private JLabel msgSendText;
    private JButton msgSendButton;

    private final String[] COLUMN_NAMES = {"ID", "IP", "Port"};

    public Gui(boolean isServer, Point location, Peer peer) {

        this.isServer = isServer;
        setTitle((isServer) ? "Server" : "Peer");
        setSize(Utilities.getGuiSize());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.peer = peer;

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                peer.exit();
                if (JOptionPane.showConfirmDialog(null, "Komplettes Programm beenden?", "Schließen", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
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
        lblHeadline = new JLabel(getTitle());
        lblHeadline.setFont(Utilities.getHeadlineFont());
        mainPanel.add(lblHeadline, BorderLayout.NORTH);

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
        peerTable.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                updateSelection();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                updateSelection();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                updateSelection();
            }
        });

        peerTable.setFont(Utilities.getNormalFont());
        peerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        peerTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        peerTable.getColumnModel().getColumn(2).setPreferredWidth(0);
        peerTable.setShowGrid(false);

        peerTable.setRowHeight(Utilities.getNormalFont().getSize() + 6);

        scrollPane = new JScrollPane(peerTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.setBorder(new EmptyBorder(0, 0, 0, 10));

        // Search
        searchOuterPanel = new JPanel();
        searchPanel = new JPanel();
        searchOuterPanel.setLayout(new BoxLayout(searchOuterPanel, BoxLayout.Y_AXIS));
        searchPanel.setLayout(new GridLayout(3, 1));
        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchField.getPreferredSize().height));
        searchField.setToolTipText("Geben Sie eine Peer-ID ein.");
        searchField.setFont(Utilities.getNormalFont());
        searchButton = new JButton("Nach ID suchen");
        searchButton.setToolTipText("Suchen Sie im P2P-Netzwerk nach der eingegebenen ID.");
        searchButton.setFont(Utilities.getNormalFont());
        searchText = new JLabel("Ungültige ID");
        searchText.setFont(Utilities.getNormalFont());
        searchText.setForeground(Color.red);
        searchText.setVisible(false);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchForID();
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(searchText);
        searchPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        searchOuterPanel.add(searchPanel);

        // TopPanel
        topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, searchOuterPanel);
        topPanel.setOneTouchExpandable(true);
        topPanel.setDividerLocation((int) (getWidth() * 0.55));
        listPanel.setMinimumSize(new Dimension(120, 0));
        searchOuterPanel.setMinimumSize(new Dimension(145, 0));
        topPanel.setBorder(null);
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // BottomPanel
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        chatArea = new JTextArea();
        chatArea.setFont(Utilities.getNormalFont());
        DefaultCaret caret = (DefaultCaret) chatArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);
        chatArea.append("Ausgabe");
        scrollPaneChat = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bottomPanel.add(scrollPaneChat, BorderLayout.CENTER);

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
        msgSendPanel.setLayout(new BorderLayout());
        msgSendText = new JLabel();
        msgSendText.setFont(Utilities.getNormalFont());
        msgSendPanel.add(msgSendText, BorderLayout.CENTER);
        msgPanel.add(msgSendPanel, BorderLayout.SOUTH);
        msgSendButton = new JButton("Senden");
        msgSendPanel.add(msgSendButton, BorderLayout.EAST);
        msgSendButton.setFont(Utilities.getNormalFont());
        msgSendButton.setPreferredSize(new Dimension(100, 30));

        msgSendButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg();
            }
        });

        // SplitLayout
        splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPanel.setOneTouchExpandable(true);
        splitPanel.setDividerLocation(130);
        topPanel.setMinimumSize(new Dimension(0, 85));
        bottomPanel.setMinimumSize(new Dimension(0, 150));
        splitPanel.setBorder(null);
        mainPanel.add(splitPanel, BorderLayout.CENTER);

        if (location == null)
            setLocationRelativeTo(null);
        else
            setLocation(location);
        setVisible(true);
    }

    public void setPeerList(ArrayList<PeerObject> peerListe) {

        int selectedNr = peerTable.getSelectedRow();
        DefaultTableModel tableModel = (DefaultTableModel) peerTable.getModel();
        if (tableModel.getRowCount() > 0) {
            for (int i = tableModel.getRowCount() - 1; i > -1; i--) {
                tableModel.removeRow(i);
            }
        }
        tableModel.setRowCount(0);

        for (int i = 0; i < peerListe.size(); i++) {
            String[] data = new String[3];

            data[0] = "" + peerListe.get(i).getIdAsInt();
            data[1] = peerListe.get(i).getIpAsString();
            data[2] = "" + peerListe.get(i).getPortAsInt();

            tableModel.addRow(data);
        }
        tableModel.fireTableDataChanged();

        if (selectedNr != -1 && selectedNr < peerTable.getRowCount()) {
            peerTable.setRowSelectionInterval(selectedNr, selectedNr);
        }
        updateSelection();

        repaint();
        revalidate();
    }

    private void searchForID() {
        int id = getSearchFieldID();
        if (id != -1) {
            searchText.setVisible(false);
            searchField.setText("");
            peer.startSearch(id);
        } else {
            searchText.setVisible(true);
        }
    }

    private int getSearchFieldID() {
        if (searchText.getText().equals(""))
            return -1;
        else {
            try {
                int id = Integer.parseInt(searchField.getText().trim());
                if (id <= 65535)
                    return id;
                else
                    return -1;
            } catch (Exception e) {
                return -1;
            }
        }
    }

    private void updateSelection() {
        int selectedNr = peerTable.getSelectedRow();
        if (selectedNr == -1) {
            msgSendText.setVisible(false);
        } else {
            msgSendText.setVisible(true);
            String id = (String) peerTable.getValueAt(selectedNr, 0);
            msgSendText.setText("An ID " + id + " schicken");
        }
        checkSendButton();
    }

    private void checkSendButton() {
        int selectedNr = peerTable.getSelectedRow();
        if (selectedNr != -1 && msgField.getText().equals("") == false) {
            msgSendButton.setEnabled(true);
        } else {
            msgSendButton.setEnabled(false);
        }
    }

    private void sendMsg() {
        int id = Integer.parseInt((String) peerTable.getValueAt(peerTable.getSelectedRow(), 0));
        String msg = msgField.getText();

        peer.sendMsg(msg, id);
        msgField.setText("");
    }

    public void addText(String txt) {

        chatArea.append("\n" + txt);
    }

    public void setHeadline(String title) {
        setTitle(title);
        lblHeadline.setText(title);
    }

}
