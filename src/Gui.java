import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public class Gui extends JFrame {

	private JLabel lblHeadline;
	private JList peerList;
	private PeerObject peerObject; // TODO implementieren

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
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);

		// MainPanel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		contentPane.add(mainPanel, BorderLayout.CENTER);

		// Headline
		lblHeadline = new JLabel(headline);
		lblHeadline.setFont(Utilities.getHeadlineFont());
		mainPanel.add(lblHeadline, BorderLayout.NORTH);

		// PeerList
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		String interessen[] = { "Politik", "Autos", "Mode", "Film- und Fernsehen", "Computer", "Tiere", "Sport" }; // TODO
		peerList = new JList(interessen);
		peerList.setFont(Utilities.getNormalFont());
		peerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(peerList);
		listPanel.add(scrollPane, BorderLayout.CENTER);

		// Suche
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		JTextField searchField = new JTextField();
		searchPanel.add(searchField);

		// TopPanel
		JSplitPane topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, searchPanel);
		topPanel.setOneTouchExpandable(true);
		topPanel.setDividerLocation(250);
		mainPanel.add(topPanel, BorderLayout.CENTER);

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
}
