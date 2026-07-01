package org.chipfc.model;

import java.util.UUID;

import lombok.Data;

/**
 * Represents the configuration settings for a serial connection session.
 */
@Data
public class SessionConfig {
    private String id;
    private String displayName;
    private String portName;
    private int baudRate;

    // Number of data bits (typically 8 or 7)
    private int dataBits;

    // Number of stop bits (maps to jSerialComm constants: 1, 2)
    private int stopBits;

    // Parity setting (maps to jSerialComm constants: 0=None, 1=Odd, 2=Even)
    private int parity;

    // Operation mode: "TERMINAL" or "UART"
    private String mode;

    /**
     * Initializes a new session configuration with default values.
     */
    public SessionConfig() {
        this.id = UUID.randomUUID().toString();
        this.displayName = "New Connection";
        this.baudRate = 115200;
        this.dataBits = 8;
        this.stopBits = 1;
        this.parity = 0;
        this.mode = "TERMINAL";
    }
}
