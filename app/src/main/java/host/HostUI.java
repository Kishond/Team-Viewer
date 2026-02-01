package host;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class HostUI extends JFrame implements HostRegistrationCallback {

    private final RegisterableToHost hostManager;
    private final JLabel statusLabel;
    private final JLabel keyLabel;
    private final JButton stopButton;

    public HostUI(RegisterableToHost hostManager) {
        this.hostManager = hostManager;

        this.setTitle("Remote Desktop Host");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 250);
        this.setLayout(new BorderLayout(10, 10));

        // UI Components
        this.statusLabel = new JLabel("Status: Disconnected", SwingConstants.CENTER);
        
        this.keyLabel = new JLabel("Session Key: ----", SwingConstants.CENTER);
        this.keyLabel.setFont(new Font("Monospaced", Font.BOLD, 24));

        this.stopButton = new JButton("Stop Sharing");
        this.stopButton.setEnabled(false);

        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(statusLabel, BorderLayout.NORTH);
        centerPanel.add(keyLabel, BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);
        this.add(stopButton, BorderLayout.SOUTH);

        setupEventListeners();
    }

    private void setupEventListeners() {
        stopButton.addActionListener(e -> {
            ((HandleDissconnection)hostManager).handleQuitRequest();
        });
    }

    public void showUI() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            // Prompt the user for a key and start registration
            promptForRegistration();
        });
    }

    private void promptForRegistration() {
        hostManager.registerHost(() -> {
            return JOptionPane.showInputDialog(this, "Set your Session Key:");
        }, this);
    }

    @Override
    public void onRegistrationSuccess(String sessionKey) {
        SwingUtilities.invokeLater(() -> {
            this.statusLabel.setText("Status: Waiting for Viewer...");
            this.keyLabel.setText(sessionKey);
            this.stopButton.setEnabled(true);
        });
    }

    @Override
    public void onRegistrationError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Registration Error: " + message);
            promptForRegistration(); // Retry
        });
    }

    public void onSessionClosed(String reason) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Session Ended: " + reason);
            this.dispose();
        });
    }

    @Override
    public void onConnection() {
    SwingUtilities.invokeLater(() -> {
        this.statusLabel.setText("Status: Streaming to Viewer");
        this.statusLabel.setForeground(java.awt.Color.GREEN);
    });
}
}

interface HostRegistrationCallback {
    void onRegistrationSuccess(String sessionKey);
    void onRegistrationError(String message);
    void onConnection();
}