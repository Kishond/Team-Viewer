package viewer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.event.*;

public class ViewerUI extends JFrame implements RemoteUpdateListener {
    private final ViewerUIListener networkSender;
    
    private final ImagePanel canvas;

    public ViewerUI(ViewerUIListener networkSender) {
        this.networkSender = networkSender;
        
        this.setTitle("Remote Desktop Viewer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1280, 720);
        this.setLayout(new BorderLayout());

        this.canvas = new ImagePanel();
        this.add(canvas, BorderLayout.CENTER);

        setupEventListeners();
    }

    private void setupEventListeners() {
        // mouse movement and clicks 
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                networkSender.sendMouseButton(e.getButton(), true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                networkSender.sendMouseButton(e.getButton(), false);
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                networkSender.sendMouseMove(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                networkSender.sendMouseMove(e.getX(), e.getY());
            }
        });

        // mouse wheel 
        canvas.addMouseWheelListener(e -> {
            networkSender.sendMouseWheel(e.getWheelRotation());
        });

        // keyboard events 
        this.addKeyListener(new KeyAdapter() {
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
        SwingUtilities.invokeLater(() -> {
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
        SwingUtilities.invokeLater(() -> this.setVisible(true));
    }

    @Override
    public void closeUI() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(false);
            this.dispose();
        });
    }

    // Inner class to handle the actual drawing of the screen shots
    private static class ImagePanel extends JPanel {
        private BufferedImage currentImage;

        public void updateImage(BufferedImage img) {
            this.currentImage = img;
            this.repaint(); // Triggers paintComponent
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentImage != null) {
                // Draws the received screen to fill the panel
                g.drawImage(currentImage, 0, 0, this.getWidth(), this.getHeight(), null);
            }
        }
    }
}