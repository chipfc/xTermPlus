package org.chipfc.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.chipfc.controller.SessionController;
import org.chipfc.view.panels.ClosableTabHeader;
import org.chipfc.view.panels.SessionTabPanel;
import org.chipfc.view.panels.SessionTreePanel;

/**
 * The main application window for xTerm+.
 * Provides a split-pane layout with a session tree on the left and a tabbed
 * workspace on the right.
 */
public class MainFrame extends JFrame {
    private final SessionTreePanel sessionTree;
    private final JTabbedPane workspaceTabs;

    public MainFrame() {
        setTitle("xTerm+");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize UI components
        sessionTree = new SessionTreePanel();
        workspaceTabs = new JTabbedPane();

        // Configure the split pane layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sessionTree, workspaceTabs);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(3);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Returns the session navigation tree.
     */
    public SessionTreePanel getSessionTree() {
        return sessionTree;
    }

    /**
     * Returns the tabbed workspace panel.
     */
    public JTabbedPane getWorkspaceTabs() {
        return workspaceTabs;
    }

    /**
     * Adds a new session tab to the workspace.
     * 
     * @param title        The title of the tab.
     * @param contentPanel The UI content panel for the tab.
     * @param controller   The controller associated with this session.
     */
    public void addNewTab(String title, SessionTabPanel contentPanel, SessionController controller) {
        workspaceTabs.addTab(title, contentPanel);
        int index = workspaceTabs.getTabCount() - 1;

        // Assign a custom header component with a close button
        workspaceTabs.setTabComponentAt(index, new ClosableTabHeader(title, workspaceTabs, contentPanel, controller));
        workspaceTabs.setSelectedIndex(index);
    }
}
