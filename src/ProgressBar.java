import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressBar extends JFrame {

    private JProgressBar progressBar;

    public ProgressBar(JFrame parent) {

        setTitle("Status");

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                setProgressBar(100);
            }
        });

        JLabel lblUeberschirft = new JLabel("Fortschritt", JLabel.CENTER);
        lblUeberschirft.setFont(Utilities.getHeadlineFont());
        contentPane.add(lblUeberschirft, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setFont(Utilities.getNormalFont());
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        contentPane.add(progressBar, BorderLayout.CENTER);

        setProgressBar(0);

        pack();
        setLocationRelativeTo(parent);
    }

    public int getWert() {
        return progressBar.getValue();
    }

    public void setProgressBar(int wert) {
        progressBar.setValue(wert);
        repaint();
        revalidate();
    }
}