package viewer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.io.File;
import java.awt.event.*;

public class ViewerUI extends JFrame implements RemoteUpdateListener, ConnectionCallback {
    private final ViewerUIListener networkSender;
    private final ConnectableToHost viewerManager;
    
    private final ImagePanel canvas;

    public ViewerUI(ViewerUIListener networkSender, ConnectableToHost viewerManager) {
        this.networkSender = networkSender;
        this.viewerManager = viewerManager;

        this.setTitle("Remote Desktop Viewer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1280, 720);
        this.setLayout(new BorderLayout());

        this.canvas = new ImagePanel();
        this.canvas.setEnabled(false);
        this.canvas.setFocusable(true); 
        this.add(canvas, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        JButton sendFileBtn = new JButton("Send File");
        sendFileBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
                    networkSender.sendFile(file.getName(), data);
                } catch (Exception ex) {}
            }
        });
        topPanel.add(sendFileBtn);
        this.add(topPanel, BorderLayout.NORTH);

        setupEventListeners();
    }

    private void setupEventListeners() {
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                sendScaledMouseCoords(e.getX(), e.getY());
                networkSender.sendMouseButton(e.getButton(), true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                sendScaledMouseCoords(e.getX(), e.getY());
                networkSender.sendMouseButton(e.getButton(), false);
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                sendScaledMouseCoords(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                sendScaledMouseCoords(e.getX(), e.getY());
            }
        });

        canvas.addMouseWheelListener(e -> {
            networkSender.sendMouseWheel(e.getWheelRotation());
        });

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                networkSender.sendKeyPress(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                networkSender.sendKeyPress(e.getKeyCode(), false);
            }
        });
    }

    /**
     * maps local Canvas coordinates to Host screen coordinates 
     * based on the received image dimensions.
     */
    private void sendScaledMouseCoords(int x, int y) {
        BufferedImage img = canvas.getImage();
        if (img != null) {
            double scaleX = (double) img.getWidth() / canvas.getWidth();
            double scaleY = (double) img.getHeight() / canvas.getHeight();

            int scaledX = (int) (x * scaleX);
            int scaledY = (int) (y * scaleY);

            networkSender.sendMouseMove(scaledX, scaledY);
        }
    }

    @Override
    public void onImageRecieved(byte[] imageData) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(bais);

            if (image != null) {
                SwingUtilities.invokeLater(() -> {
                    canvas.updateImage(image);
                });
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void onSessionClosed(String reason) {
        System.out.println("session closed");
        SwingUtilities.invokeLater(() -> {
            this.canvas.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Session Closed: " + reason);
            this.dispose();
        });
    }

    @Override
    public void hideUI() {
        SwingUtilities.invokeLater(() -> this.setVisible(false));
    }

    @Override
    public void showUI() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            promptForConnection();
        });
    }

    private void promptForConnection() {
        viewerManager.connectToHost(() -> {
            return JOptionPane.showInputDialog(this, "Enter Session Code:");
        }, this);
    }

    @Override
    public void onConnectionSuccess() {
        SwingUtilities.invokeLater(() -> {
            this.setTitle("Connected to Host - Remote Desktop Viewer");
            this.canvas.setEnabled(true); 
            this.canvas.requestFocusInWindow(); // Give focus to canvas for keyboard input
        });
    }

    @Override
    public void onConnectionError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message);
        });
    }

    @Override
    public void closeUI() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(false);
            this.dispose();
        });
    }

    private static class ImagePanel extends JPanel {
        private BufferedImage currentImage;

        public void updateImage(BufferedImage img) {
            this.currentImage = img;
            this.repaint();
        }

        public BufferedImage getImage() {
            return currentImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentImage != null) {
                g.drawImage(currentImage, 0, 0, this.getWidth(), this.getHeight(), null);
            } else {
                g.drawString("Waiting for connection...", 10, 20);
            }
        }
    }
}

interface ConnectionCallback {
    void onConnectionSuccess();
    void onConnectionError(String message);
}