package org.chipfc.core;

/**
 * Interface definition for handling serial data events.
 */
public interface SerialDataListener {

    /**
     * Triggered when new data is received from the serial port.
     * 
     * @param data The raw byte array received from the device.
     */
    void onDataReceived(byte[] data);

    /**
     * Triggered when a connection error occurs.
     * 
     * @param errorMessage A description of the error encountered.
     */
    void onConnectionError(String errorMessage);
}
