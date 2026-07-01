package org.chipfc.view.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.chipfc.model.SessionConfig;
import org.chipfc.model.SessionGroup;
import org.chipfc.model.SessionManager;

/**
 * Panel component displaying the session structure in a tree view.
 */
public class SessionTreePanel extends JPanel {
    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private TreeActionListener listener;

    /**
     * Interface for handling tree interaction events.
     */
    public interface TreeActionListener {
        void onConnectSession(SessionConfig config);

        void onNewSession();

        void onEditSession(SessionConfig config);

        void onDeleteSession(SessionConfig config);
    }

    public void setTreeActionListener(TreeActionListener listener) {
        this.listener = listener;
    }

    public SessionTreePanel() {
        setLayout(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sessions");
        loadDataIntoTree(root);

        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setCellRenderer(new SessionTreeRenderer());
        tree.setRootVisible(false);

        add(new JScrollPane(tree), BorderLayout.CENTER);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                tree.setSelectionRow(row);

                Object node = tree.getLastSelectedPathComponent();
                if (node == null)
                    return;

                Object userObj = ((DefaultMutableTreeNode) node).getUserObject();

                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e, userObj);
                } else if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    if (userObj instanceof SessionConfig && listener != null) {
                        listener.onConnectSession((SessionConfig) userObj);
                    }
                }
            }
        });
    }

    /**
     * Refreshes the tree while attempting to maintain the expanded state of nodes.
     */
    public void refreshTree() {
        List<String> expandedPaths = new ArrayList<>();
        for (int i = 0; i < tree.getRowCount(); i++) {
            TreePath path = tree.getPathForRow(i);
            if (tree.isExpanded(path)) {
                expandedPaths.add(pathToString(path));
            }
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();
        loadDataIntoTree(root);
        treeModel.reload();

        for (int i = 0; i < tree.getRowCount(); i++) {
            TreePath path = tree.getPathForRow(i);
            if (expandedPaths.contains(pathToString(path))) {
                tree.expandPath(path);
            }
        }
    }

    private String pathToString(TreePath path) {
        StringBuilder sb = new StringBuilder();
        for (Object node : path.getPath()) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            Object userObj = treeNode.getUserObject();
            if (userObj instanceof SessionGroup) {
                sb.append(((SessionGroup) userObj).getId()).append("/");
            } else if (userObj instanceof SessionConfig) {
                sb.append(((SessionConfig) userObj).getId()).append("/");
            } else {
                sb.append(userObj.toString()).append("/");
            }
        }
        return sb.toString();
    }

    /**
     * Custom renderer to display session icons and labels correctly.
     */
    private class SessionTreeRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof SessionGroup) {
                setText(((SessionGroup) userObject).getGroupName());
                setIcon(UIManager.getIcon("FileView.directoryIcon"));
            } else if (userObject instanceof SessionConfig) {
                setText(((SessionConfig) userObject).getDisplayName());
                setIcon(UIManager.getIcon("FileView.fileIcon"));
            }
            return this;
        }
    }

    private void loadDataIntoTree(DefaultMutableTreeNode root) {
        for (SessionGroup group : SessionManager.getInstance().getRootGroups()) {
            DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
            for (SessionConfig config : group.getItems()) {
                groupNode.add(new DefaultMutableTreeNode(config));
            }
            root.add(groupNode);
        }
    }

    private void showContextMenu(MouseEvent e, Object userObj) {
        JPopupMenu menu = new JPopupMenu();

        if (userObj instanceof SessionConfig) {
            SessionConfig config = (SessionConfig) userObj;

            JMenuItem connectItem = new JMenuItem("Connect");
            connectItem.addActionListener(evt -> {
                if (listener != null)
                    listener.onConnectSession(config);
            });

            JMenuItem editItem = new JMenuItem("Edit");
            editItem.addActionListener(evt -> {
                if (listener != null)
                    listener.onEditSession(config);
            });

            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener(evt -> {
                if (listener != null)
                    listener.onDeleteSession(config);
            });

            menu.add(connectItem);
            menu.add(editItem);
            menu.addSeparator();
            menu.add(deleteItem);
        } else {
            JMenuItem newItem = new JMenuItem("New Session in Group");
            newItem.addActionListener(evt -> {
                if (listener != null)
                    listener.onNewSession();
            });
            menu.add(newItem);
        }

        menu.show(tree, e.getX(), e.getY());
    }
}
