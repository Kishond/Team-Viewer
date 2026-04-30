package host;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
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
            long targetFrameTime = 1000 / 30; // Target 30 FPS (~33ms per frame)
            while (isCapturing) {
                long startTime = System.currentTimeMillis();
                byte[] frame = captureScreen();
                if (frame != null) {
                    senderListener.queueImage(frame);
                }
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = targetFrameTime - elapsedTime;
                if (sleepTime > 0) {
                    try { Thread.sleep(sleepTime); } catch (InterruptedException e) { break; }
                }
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
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            BufferedImage screenshot = robot.createScreenCapture(screenRect);
            
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.3f); // 30% quality drastically reduces latency
                }
                writer.setOutput(ios);
                writer.write(null, new IIOImage(screenshot, null, null), param);
                writer.dispose();
            }
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