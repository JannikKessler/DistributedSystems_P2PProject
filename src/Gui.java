import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public class Gui extends JFrame {

	private JLabel lblHeadline;
	private JList peerList;
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
		String interessen[] = { "ID: 0\tIP: 192.168.0.2\tPort: 3333", "ID: 4\tIP: 192.168.0.7\tPort: 3333",
				"ID: 18\tIP: 192.168.0.9\tPort: 3333", "ID: 27\tIP: 192.168.0.13\tPort: 3333" }; // TODO
		peerList = new JList(interessen);
		peerList.setCellRenderer(new TabRenderer());
		peerList.setFont(Utilities.getNormalFont());
		peerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(peerList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listPanel.add(scrollPane, BorderLayout.CENTER);

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
		searchButton.setToolTipText("Suchen Sie nach der eingegebenen Peer-ID.");
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
		searchPanel.setBorder(new EmptyBorder(0, 10, 20, 0));
		searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		searchOuterPanel.add(searchPanel);

		// TopPanel
		topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, searchOuterPanel);
		topPanel.setOneTouchExpandable(true);
		topPanel.setDividerLocation(315);
		listPanel.setMinimumSize(new Dimension(120, 0));
		searchOuterPanel.setMinimumSize(new Dimension(145, 0));
		topPanel.setBorder(null);
		
		// BottomPanel
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		
		
		// SplitLayout
		splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
		splitPanel.setOneTouchExpandable(true);
		splitPanel.setDividerLocation(110);
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

	public void setPeerList(String ausgabe) {

		// TODO

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

	class TabRenderer extends DefaultListCellRenderer {
		JTextArea field = new JTextArea();

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			field.setText(super.getText());
			field.setFont(super.getFont());
			field.setBackground(super.getBackground());
			field.setForeground(super.getForeground());
			field.setBorder(super.getBorder());
			field.setTabSize(5);
			return field;
		}
	}
}
