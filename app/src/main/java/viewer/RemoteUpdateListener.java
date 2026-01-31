package viewer;

public interface RemoteUpdateListener {
    // When viewer UI receives image data to be rendered
    public void onImageRecieved(byte[] imageData);
    
    // When host connection has been closed or lost
    public void onSessionClosed(String reason);

    // UI State Management
    public void hideUI();
    public void showUI();
    public void closeUI();
}