import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

		// TopPanel
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		mainPanel.add(topPanel, BorderLayout.CENTER);
		
		// PeerList
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		String interessen[] = { "Politik", "Autos", "Mode", "Film- und Fernsehen", "Computer", "Tiere", "Sport" }; // TODO
		peerList = new JList(interessen);
		peerList.setFont(Utilities.getNormalFont());
		peerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(peerList);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		topPanel.add(listPanel, BorderLayout.CENTER);

		// Suche
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BorderLayout());
		searchPanel.setBackground(Color.GREEN);
		topPanel.add(listPanel, BorderLayout.EAST);
		
		
		
		
//        JPanel contentPane = new JPanel();
//        contentPane.setLayout(new BorderLayout());
//        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
//        setContentPane(contentPane);
//
//        JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new BorderLayout());
//        contentPane.add(mainPanel, BorderLayout.CENTER);
//
//        lblHeadline = new JLabel(headline);
//        lblHeadline.setFont(Utilities.getHeadlineFont());
//        mainPanel.add(lblHeadline, BorderLayout.NORTH);
//
//        peerList = new JTextArea();
//        peerList.setFont(Utilities.getNormalFont());
//        peerList.setEditable(false);
//
//        JScrollPane scrollPane = new JScrollPane(peerList);
//        mainPanel.add(scrollPane, BorderLayout.CENTER);

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
