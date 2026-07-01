package org.chipfc.controller;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.chipfc.model.SessionConfig;
import org.chipfc.model.SessionManager;
import org.chipfc.view.MainFrame;
import org.chipfc.view.dialogs.SessionDialog;
import org.chipfc.view.panels.SessionTabPanel;
import org.chipfc.view.panels.SessionTreePanel;

import lombok.extern.slf4j.Slf4j;

/**
 * Main controller for the application.
 * Manages the interactions between the UI components and the session data
 * model.
 */
@Slf4j
public class MainController {

    private final MainFrame view;

    public MainController(MainFrame view) {
        this.view = view;
        initTreeListeners();
    }

    /**
     * Sets up event listeners for the session navigation tree.
     */
    private void initTreeListeners() {
        view.getSessionTree().setTreeActionListener(new SessionTreePanel.TreeActionListener() {
            @Override
            public void onConnectSession(SessionConfig config) {
                handleConnect(config);
            }

            @Override
            public void onNewSession() {
                handleNewSession();
            }

            @Override
            public void onEditSession(SessionConfig config) {
                handleEditSession(config);
            }

            @Override
            public void onDeleteSession(SessionConfig config) {
                handleDeleteSession(config);
            }
        });
    }

    private void handleNewSession() {
        SessionDialog dialog = new SessionDialog(view, null);
        dialog.setVisible(true);

        SessionConfig newConfig = dialog.getSessionConfig();
        if (newConfig != null) {
            if (!SessionManager.getInstance().getRootGroups().isEmpty()) {
                SessionManager.getInstance().getRootGroups().get(0).getItems().add(newConfig);
                SessionManager.getInstance().saveSessions();
                view.getSessionTree().refreshTree();
            }
        }
    }

    private void handleEditSession(SessionConfig config) {
        SessionDialog dialog = new SessionDialog(view, config);
        dialog.setVisible(true);

        SessionConfig updatedConfig = dialog.getSessionConfig();
        if (updatedConfig != null) {
            config.setDisplayName(updatedConfig.getDisplayName());
            config.setPortName(updatedConfig.getPortName());
            config.setBaudRate(updatedConfig.getBaudRate());
            config.setMode(updatedConfig.getMode());

            SessionManager.getInstance().saveSessions();
            view.getSessionTree().refreshTree();
        }
    }

    private void handleDeleteSession(SessionConfig config) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete session '" + config.getDisplayName() + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().getRootGroups().forEach(g -> g.getItems().remove(config));
            SessionManager.getInstance().saveSessions();
            view.getSessionTree().refreshTree();
        }
    }

    /**
     * Handles the connection logic and tab management.
     */
    private void handleConnect(SessionConfig config) {
        // Prevent opening duplicate tabs
        for (int i = 0; i < view.getWorkspaceTabs().getTabCount(); i++) {
            Component comp = view.getWorkspaceTabs().getComponentAt(i);
            if (comp instanceof SessionTabPanel) {
                if (((SessionTabPanel) comp).getConfig().getId().equals(config.getId())) {
                    view.getWorkspaceTabs().setSelectedIndex(i);
                    return;
                }
            }
        }

        // Create new tab and controller
        SessionTabPanel newTab = new SessionTabPanel(config);
        SessionController controller = new SessionController(newTab, config);
        view.addNewTab(config.getDisplayName(), newTab, controller);
    }
}
