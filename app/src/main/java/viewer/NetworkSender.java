package viewer;

import resources.*;

public class NetworkSender implements ViewerUIListener, Runnable {
    private ServerProtocolSender serverProtocol;

    public NetworkSender(ServerProtocolSender serverProtocol) {
        this.serverProtocol = serverProtocol;
    }

    @Override
    public void sendMouseMove(int x, int y) {
        throw new UnsupportedOperationException("Unimplemented method 'sendMouseMove'");
    }

    @Override
    public void sendMouseButton(int button, boolean isPressed) {
        throw new UnsupportedOperationException("Unimplemented method 'sendMouseButton'");
    }

    @Override
    public void sendMouseWheel(int rotation) {
        throw new UnsupportedOperationException("Unimplemented method 'sendMouseWheel'");
    }

    @Override
    public void sendKeyPress(int keyCode, boolean isPressed) {
        throw new UnsupportedOperationException("Unimplemented method 'sendKeyPress'");
    }

    @Override
    public void sendDisconnect() {
        throw new UnsupportedOperationException("Unimplemented method 'sendDisconnect'");
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
    
}

interface ViewerUIListener {
    // Mouse Events
    void sendMouseMove(int x, int y);
    void sendMouseButton(int button, boolean isPressed); // 1=Left, 2=Middle, 3=Right
    void sendMouseWheel(int rotation);

    // Keyboard Events
    void sendKeyPress(int keyCode, boolean isPressed);

    // Connection Events
    void sendDisconnect();
}
