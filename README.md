# xTerm+

## Project Overview

- *Project Name/Internal:* `xTermPlus`
- *Display Name/Product Name:* `xTerm+`

xTerm+ is a high-performance, cross-platform Serial Terminal application designed for developers and hardware engineers. Built with Java and FlatLaF, it provides a robust, intuitive, and responsive environment for debugging, monitoring, and analyzing serial communication across macOS, Windows, and Linux. Whether you are working with embedded systems, microcontrollers, or Linux devices, xTerm+ streamlines your workflow with advanced features like virtualized rendering, automated session management, and comprehensive data export capabilities.

## Expected features

### Serial Communication

- **Port Detection:** Auto-scan and Hot-plug. Supports USB CDC, FTDI, CH340, CP210x devices
- **Baud Rate:** 300 - 2,000,000 bps with custom values
- **Flow Control:** RTS-CTS, XON-XOFF, or none
- **Auto-Reconnect:** Intelligent reconnection on device unplug/plug
- **Multi-Window:** Independent serial connections (Cmd/Ctrl+N)
- **Session manager:** Supports naming and saving configuration information for frequently used connections.

### Terminal Display

- **Multi-Format:** ASCII, HEX, Binary, Decimal views
- **Timestamps:** Millisecond precision timestamps
- **Line Endings:** CR, LF, CRLF, None
- **Auto-Scroll:** Toggle with Space key
- **Search:** Regex-based text search (Cmd/Ctrl+F)
- **Automatic syntax highlighting:** Example: ERROR is automatically highlighted in red
- **ASCII Color Codes:** Supports displaying colors from ASCII color codes
- **High Performance:** Virtualized rendering for 100k+ messages
- **Operation Mode:**
    - **Terminal:** Like a standard terminal. Useful for communicating with Linux devices.
    - **UART:** Has separate receive and send areas. Useful for debugging MCU devices.

### Logging & Export

- **Auto-Logging:** Automatic session recording to file
- **Export Formats:** .txt, .csv, .log, .bin (raw bytes)
- **Selective Export:** Export filtered or search-result data

### User Interface

- **Dark/Light Theme:** Toggle with persistence
- **Settings Sidebar:** Collapsible quick-access panel
- **Keyboard Navigation:** Full shortcut support

### Advanced features

- Realtime plotting
- Protocol analyzers (Modbus, CAN)
- Session recording/playback
- Advanced filtering
- Remote monitoring
- Performance dashboards

### Others Requirements

- **Activity Log:** Supports exporting application activity logs to help troubleshoot the application in case of problems.
- **Cross-Platform:** Identical experience on macOS, Windows, Linux
    - **macOS**: dmg
    - **Windows**: exe or msi
    - **Linux**: deb

## Development Environment

- Project developed in Java
- Using FlatLaF for the UI
- Using jSerialComm
- Built with Gradle
- IDEs: vscode and IntelliJ IDEA
- [...]

## UI Layout & Functional Requirements

### Left Sidebar (Navigation Tree)

* **Structure:** The left sidebar shall feature a 2-level hierarchical tree view (**Group > Item**).
* **Item Management:** Each **Item** represents and manages a single connection to a specific serial port.
* **Create New Item:** * Clicking "New Item" shall trigger a configuration modal/popup.
* The modal must allow the user to configure the following parameters: `Display Name`, `Serial Port Name`, `Baud Rate`, `Stop Bits`, `Parity Bit`, and `Mode` (Selectable between **UART** and **TERMINAL**).


* **Context Menu:** Right-clicking an item shall display a context menu with options including, but not limited to: `Edit`, `Delete`, and `Connect/Disconnect`.

### Right MainFrame (Tabbed View)

* **Structure:** The right main panel shall utilize a tabbed interface, where each **Tab** corresponds to an active serial port connection created from the sidebar.
* **Mode-Specific Layouts:**
* **Terminal Mode Tab:** The terminal console shall occupy the full screen area of the tab.
* **UART Mode Tab:** The tab shall include the main console area along with a **bottom toolbar**. This toolbar must contain a text input field and a "Send" button for transmitting data.

