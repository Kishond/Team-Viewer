package host;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import resources.Packet;
import resources.ServerProtocol;

public class RobotController implements RemoteActionListener {
    private Robot robot;
    private final HostActionsListener senderListener;
    private final Rectangle screenRect;
    
    private Thread captureThread;
    private boolean isCapturing;

    public RobotController(HostActionsListener senderListener) {
        this.senderListener = senderListener;
        this.isCapturing = false;
        // Get the dimensions of the primary screen
        this.screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            System.err.println("Robot Error: " + e.getMessage());
        }
    }

    public void startLiveCapture() {
        if (isCapturing) return;
        
        this.isCapturing = true;
        this.captureThread = new Thread(() -> {
            while (isCapturing) {
                byte[] frame = captureScreen();
                if (frame != null) {
                    senderListener.sendImage(frame);
                }
                
                try { Thread.sleep(10); } catch (InterruptedException e) { break; }
            }
        });
        this.captureThread.start();
    }

    public void stopLiveCapture() {
        this.isCapturing = false;
        if (this.captureThread != null) {
            this.captureThread.interrupt();
        }
    }

    private byte[] captureScreen() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage screenshot = robot.createScreenCapture(screenRect);
            // Convert to JPG for significant size reduction
            ImageIO.write(screenshot, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onMouseMove(Packet packet) {
        int[] coords = ServerProtocol.getCordsFromPacket(packet);
        robot.mouseMove(coords[0], coords[1]);
    }

    @Override
    public void onMouseButton(Packet packet) {
        ServerProtocol.ButtonData data = ServerProtocol.getButtonFromPacket(packet);
        
        int mask;
        switch (data.button) {
            case 3:
                mask = InputEvent.BUTTON3_DOWN_MASK;
                break;
            case 2:
                mask = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case 1:
            default:
                mask = InputEvent.BUTTON1_DOWN_MASK;
                break;
        }

        if (data.isPressed) {
            robot.mousePress(mask);
        } else {
            robot.mouseRelease(mask);
        }
    }

    @Override
    public void onMouseWheel(Packet packet) {
        int rotation = ServerProtocol.getWheelRotationFromPacket(packet);
        robot.mouseWheel(rotation);
    }

    @Override
    public void onKeyPress(Packet packet) {
        ServerProtocol.KeyData data = ServerProtocol.getKeysFromPacket(packet);
        try {
            if (data.isPressed) {
                robot.keyPress(data.keyCode);
            } else {
                robot.keyRelease(data.keyCode);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}

interface RemoteActionListener {
    void onMouseMove(Packet packet);
    void onMouseButton(Packet packet);
    void onMouseWheel(Packet packet);
    void onKeyPress(Packet packet);
}