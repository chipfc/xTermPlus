package org.chipfc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

/**
 * Singleton manager responsible for loading and saving session configurations
 * to a local JSON file.
 */
@Slf4j
public class SessionManager {
    private static final String CONFIG_DIR = ".configs";
    private static final String CONFIG_FILE = "sessions.json";
    private static SessionManager instance;

    private List<SessionGroup> rootGroups;
    private final Gson gson;

    private SessionManager() {
        // Initialize Gson with pretty printing for readability
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.rootGroups = new ArrayList<>();
        loadSessions();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public List<SessionGroup> getRootGroups() {
        return rootGroups;
    }

    /**
     * Persists the session list from memory to the JSON file on disk.
     */
    public void saveSessions() {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(Paths.get(CONFIG_DIR, CONFIG_FILE).toString()), StandardCharsets.UTF_8)) {
            gson.toJson(rootGroups, writer);
            log.info("Session list successfully saved to JSON file.");
        } catch (IOException e) {
            log.error("Failed to save session configuration file.", e);
        }
    }

    /**
     * Loads session data from the JSON file into memory on application startup.
     */
    public void loadSessions() {
        File file = new File(Paths.get(CONFIG_DIR, CONFIG_FILE).toString());

        // Ensure the configuration directory exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                log.info("Created configuration directory: {}", parentDir.getPath());
            } else {
                log.error("Failed to create configuration directory: {}", parentDir.getPath());
            }
        }

        // Create default data if the file is missing
        if (!file.exists()) {
            log.info("Configuration file {} not found, generating default data.", CONFIG_FILE);
            createDefaultData();
            saveSessions();
            return;
        }

        try (Reader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<SessionGroup>>() {
            }.getType();
            List<SessionGroup> loadedData = gson.fromJson(reader, listType);
            if (loadedData != null) {
                this.rootGroups = loadedData;
            }
            log.info("Successfully loaded session list from disk.");
        } catch (IOException e) {
            log.error("Error reading session configuration file, initializing empty list.", e);
            this.rootGroups = new ArrayList<>();
        }
    }

    /**
     * Generates initial sample data for the first-time application run.
     */
    private void createDefaultData() {
        SessionGroup defaultGroup = new SessionGroup("Linux Devices");

        SessionConfig config1 = new SessionConfig();
        config1.setDisplayName("Linux E13F8");
        config1.setPortName("COM3");
        config1.setBaudRate(115200);
        config1.setMode("TERMINAL");

        SessionConfig config2 = new SessionConfig();
        config2.setDisplayName("IoT Sensor Node");
        config2.setPortName("COM4");
        config2.setBaudRate(9600);
        config2.setMode("UART");

        defaultGroup.getItems().add(config1);
        defaultGroup.getItems().add(config2);

        this.rootGroups.add(defaultGroup);
    }
}
