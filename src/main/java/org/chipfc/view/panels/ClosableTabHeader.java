package org.chipfc.view.panels;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.chipfc.controller.SessionController;

/**
 * A custom tab header component that includes a title and a close button.
 */
public class ClosableTabHeader extends JPanel {

    /**
     * Initializes the custom tab header.
     * 
     * @param title      The title displayed on the tab.
     * @param pane       The tabbed pane containing this header.
     * @param content    The content panel associated with this tab.
     * @param controller The controller responsible for managing the session
     *                   lifecycle.
     */
    public ClosableTabHeader(String title, JTabbedPane pane, SessionTabPanel content, SessionController controller) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
        add(new JLabel(title));

        // Create the close button
        JButton btnClose = new JButton("x");
        btnClose.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        btnClose.setContentAreaFilled(false);

        // Add action listener to handle tab closure
        btnClose.addActionListener(e -> {
            // Terminate the controller logic first
            if (controller != null) {
                controller.close();
            }
            // Remove the content panel from the tabbed pane
            pane.remove(content);
        });

        add(btnClose);
    }
}
