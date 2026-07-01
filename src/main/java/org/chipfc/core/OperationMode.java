package org.chipfc.core;

/**
 * Defines the operational modes for the serial session.
 */
public enum OperationMode {
    /**
     * * Terminal mode: processes character strings and handles ANSI color codes.
     */
    TERMINAL,

    /**
     * * UART mode: reads raw bytes and displays them in HEX format.
     */
    UART
}
