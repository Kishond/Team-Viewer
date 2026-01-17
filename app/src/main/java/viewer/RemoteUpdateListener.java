package viewer;

public interface RemoteUpdateListener {
    // when viewer UI recieves image
    public void onImageRecieved(byte[] imageData);
    
    // when host connection has been closed
    public void onSessionClosed(String reason);
}
