package org.chipfc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/**
 * Represents a group container for session configurations.
 * Used to organize sessions into logical categories in the tree view.
 */
@Data
public class SessionGroup {
    private String id;
    private String groupName;
    private List<SessionConfig> items;

    /**
     * Initializes a new group with a default name.
     */
    public SessionGroup() {
        this.id = UUID.randomUUID().toString();
        this.groupName = "New Group";
        this.items = new ArrayList<>();
    }

    /**
     * Initializes a new group with a specific name.
     * 
     * @param groupName The display name for this group.
     */
    public SessionGroup(String groupName) {
        this.id = UUID.randomUUID().toString();
        this.groupName = groupName;
        this.items = new ArrayList<>();
    }
}
