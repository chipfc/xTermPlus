package org.chipfc.core;

import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages serial port communication, including connection, data transmission,
 * and event listening.
 */
@Slf4j
public class SerialEngine {

    private SerialPort serialPort;
    private SerialDataListener dataListener;

    public void setListener(SerialDataListener listener) {
        this.dataListener = listener;
    }

    /**
     * Retrieves a list of available serial port names on the system.
     */
    public List<String> getAvailablePorts() {
        List<String> portNames = new ArrayList<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            portNames.add(port.getSystemPortName());
        }
        return portNames;
    }

    /**
     * Opens a serial connection with the specified port and baud rate.
     */
    public boolean connect(String portName, int baudRate) {
        if (serialPort != null && serialPort.isOpen()) {
            log.warn("The port is already open; please close the old port before opening the new one.");
            return false;
        }

        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baudRate);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

        if (serialPort.openPort()) {
            log.info("Successfully connected to {} with BaudRate {}", portName, baudRate);
            setupDataListener();
            return true;
        } else {
            // Treat as successful if the port is already open
            if (serialPort.isOpen()) {
                return true;
            }
            log.error("Unable to open port {}. The port may be occupied by another app.", portName);
            return false;
        }
    }

    /**
     * Configures the serial port data listener to handle incoming bytes and
     * disconnections.
     */
    private void setupDataListener() {
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                    log.error("The connection was suddenly cut off!");
                    if (dataListener != null) {
                        dataListener.onConnectionError("The device has been disconnected.");
                    }
                    disconnect();
                    return;
                }

                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    int bytesAvailable = serialPort.bytesAvailable();
                    if (bytesAvailable > 0) {
                        byte[] buffer = new byte[bytesAvailable];
                        int bytesRead = serialPort.readBytes(buffer, buffer.length);
                        log.debug("Received {} bytes ({})", bytesRead, String.format("%02X", buffer[0]));

                        if (dataListener != null) {
                            dataListener.onDataReceived(buffer);
                        }
                    }
                }
            }
        });
    }

    /**
     * Removes the active data listener and clears the reference.
     */
    public void removeListener() {
        if (serialPort != null && dataListener != null) {
            serialPort.removeDataListener();
            dataListener = null;
            log.info("Data listener removed from the COM port.");
        }
    }

    public boolean isOpen() {
        return serialPort != null && serialPort.isOpen();
    }

    /**
     * Closes the serial port and releases resources.
     */
    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.removeDataListener();
            serialPort.closePort();
            log.info("Port {} is closed.", serialPort.getSystemPortName());
        }
        serialPort = null;
    }

    /**
     * Sends raw bytes to the connected serial port.
     */
    public boolean sendData(byte[] data) {
        if (serialPort != null && serialPort.isOpen()) {
            int bytesWritten = serialPort.writeBytes(data, data.length);
            return bytesWritten > 0;
        }
        log.warn("Unable to send data; COM port is not connected.");
        return false;
    }
}
