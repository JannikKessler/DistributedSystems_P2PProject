import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public class Gui extends JFrame {

	private JLabel lblHeadline;
	private JTable peerTable;
	private PeerObject peerObject; // TODO implementieren

	private JPanel contentPane;
	private JPanel mainPanel;
	private JPanel listPanel;
	private JScrollPane scrollPane;
	private JPanel searchOuterPanel;
	private JPanel searchPanel;
	private JSplitPane topPanel;
	private JPanel bottomPanel;
	private JSplitPane splitPanel;

	private JTextField searchField;
	private JButton searchButton;
	private JLabel searchText;
	
	private final String[] COLUMN_NAMES = { "ID", "IP", "Port"};

	public Gui(String headline, Point location, Peer application) {

		setTitle(headline);
		setSize(Utilities.getGuiSize());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				application.exit();
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
		lblHeadline = new JLabel(headline);
		lblHeadline.setFont(Utilities.getHeadlineFont());
		mainPanel.add(lblHeadline, BorderLayout.NORTH);

		// PeerList
		listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		
		String[][] data = { { "0", "192.168.0.2", "3333" },
				{ "0", "192.168.0.2", "3333" },
				{ "4", "192.168.0.5", "3336" },
				{ "7", "192.168.0.12", "3349" },
				{ "18", "192.168.0.25", "65501" } };

		peerTable = new JTable(data, COLUMN_NAMES);
		peerTable.setFont(Utilities.getNormalFont());
		peerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		peerTable.getColumnModel().getColumn(0).setPreferredWidth(0);
		peerTable.getColumnModel().getColumn(2).setPreferredWidth(0);
		peerTable.setShowGrid(false);
		
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
		topPanel.setDividerLocation(315);
		listPanel.setMinimumSize(new Dimension(120, 0));
		searchOuterPanel.setMinimumSize(new Dimension(145, 0));
		topPanel.setBorder(null);
		topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		// BottomPanel
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());

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

		String[][] data = new String [peerListe.size()][3];
		for (int i = 0; i < peerListe.size(); i++) {
			data[i][0] = "" + peerListe.get(i).getIdAsInt();
			data[i][1] = "" + peerListe.get(i).getIpAsString();
			data[i][2] = "" + peerListe.get(i).getPortAsInt();
		}
		peerTable = new JTable(data, COLUMN_NAMES);

		repaint();
		revalidate();
	}

	private void searchForID() {
		int id = getSearchFieldID();
		if (id != -1) {
			searchText.setVisible(false);
			searchField.setText("");
			System.out.println("Suche nach " + id);
			// TODO SUche ausführen
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

}
