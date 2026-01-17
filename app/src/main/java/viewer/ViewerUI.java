package viewer;

import javax.swing.JFrame;

public class ViewerUI extends JFrame implements RemoteUpdateListener {
    private final ConnectableToHost viewerManager;
    private final ViewerUIListener networkSender;

    public ViewerUI(ConnectableToHost viewerManager, ViewerUIListener networkSender) {
        this.viewerManager = viewerManager;
        this.networkSender = networkSender;
    }

    @Override
    public void onImageRecieved(byte[] imageData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onImageRecieved'");
    }

    @Override
    public void onSessionClosed(String reason) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onSessionClosed'");
    }
}
