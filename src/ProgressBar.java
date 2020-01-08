import javax.swing.*;
import javax.swing.text.Position;
import java.awt.*;

public class ProgressBar extends JFrame {

    private JProgressBar progressBar;

    public ProgressBar(JFrame parent) {

        setTitle("Status");

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);
        setAlwaysOnTop(true);

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