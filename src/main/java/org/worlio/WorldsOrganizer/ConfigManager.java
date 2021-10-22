package org.worlio.WorldsOrganizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

enum ToolBarPosition {

    TOP("Top", 0),
    RIGHT("Right", 1),
    BOTTOM("Bottom", 2),
    LEFT("Left", 3);

    private String name;
    private int value;

    ToolBarPosition(String name, int value) {
        this.name = name;
        this.value = value;
    }
}

enum ConfigEntry {
    updateURL("update-url"),
    iconSize("icon-size"),
    fileBackup("file-backup"),
    darkMode("dark-mode"),
    toolbarPos("toolbar-position"),
    channel("channel"),
    status("show-status"),
    debug("debug");

    private String name;

    ConfigEntry(String name) {
        this.name = name;
    }
}

public class ConfigManager {

    private File configFile;

    public static final List<String> channels = Arrays.asList("stable", "beta");
    public static final List<String> toolBarPos = Arrays.asList("Top", "Right", "Bottom", "Left");

    private static HashMap<String, Object> defaultConfiguration = new HashMap<>();
    static HashMap<String, Object> configuration = new HashMap<>();
    private static ObjectMapper mapper;

    private void initialize() {
        mapper = new ObjectMapper();
        Console.print("Initializing default config values", 1, ConsoleType.INFO);
        defaultConfiguration.put(ConfigEntry.updateURL.name(), "https://worlio.com/WorldsOrganizer.json");
        defaultConfiguration.put(ConfigEntry.iconSize.name(), 24);
        defaultConfiguration.put(ConfigEntry.fileBackup.name(), true);
        defaultConfiguration.put(ConfigEntry.darkMode.name(), false);
        defaultConfiguration.put(ConfigEntry.toolbarPos.name(), 3);
        defaultConfiguration.put(ConfigEntry.channel.name(), "stable");
        defaultConfiguration.put(ConfigEntry.status.name(), true);
        defaultConfiguration.put(ConfigEntry.debug.name(), false);
    }

    public ConfigManager() {
        initialize();
    }

    public ConfigManager(File file) {
        initialize();
        configFile = file;
        try {
            if (!configFile.exists()) {
                Console.print("Config doesn't exist! Writing new one...", 1, ConsoleType.INFO);
                write();
                configuration = defaultConfiguration;
            } else {
                Console.print("Mapping config", 1, ConsoleType.INFO);
                configuration = mapper.readValue(new FileInputStream(configFile), new TypeReference<HashMap<String, Object>>() {});
            }
        } catch (IOException ioException) {
            write();
        }
    }

    public boolean write() {
        Console.print("Updating config", ConsoleType.INFO);
        return write(configFile);
    }

    public boolean write(HashMap<String, Object> hash) {
        if (!configFile.exists()) {
            if (!createFile(configFile)) return false;
        }
        assert configFile.exists();
        try(FileOutputStream fos = new FileOutputStream(configFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hash).getBytes());
            bos.close();
            fos.close();
            configuration = mapper.readValue(new FileInputStream(configFile), new TypeReference<HashMap<String, Object>>() {
            });
            return true;
        } catch (IOException e) {
            Console.print("IOException encountered while writing config file. Aborted!", ConsoleType.ERROR);
            return false;
        }
    }

    public boolean write(File file) {
        if (!file.exists()) {
            if (!createFile(file)) return false;
        }
        ObjectMapper mapper = new ObjectMapper();
        try(FileOutputStream fos = new FileOutputStream(configFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(configuration).getBytes());
            bos.close();
            fos.close();
            configuration = mapper.readValue(new FileInputStream(file), new TypeReference<HashMap<String, Object>>() {});
            return true;
        } catch (IOException e) {
            Console.print("IOException encountered while writing config file. Aborted!", ConsoleType.ERROR);
            return false;
        }
    }

    private boolean createFile(File file) {
        try {
            assert file.createNewFile();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getBooleanValue(ConfigEntry entry) {
        return (boolean)getValue(entry);
    }

    public String getStringValue(ConfigEntry entry) {
        return (String)getValue(entry);
    }

    public int getIntValue(ConfigEntry entry) {
        return (int)getValue(entry);
    }

    public double getDoubleValue(ConfigEntry entry) {
        return (double)getValue(entry);
    }

    private Object getValue(ConfigEntry entry) {
        try {
            Object o = configuration.get(entry.name());
            if (o == null) throw new NullPointerException();
            else return o;
        } catch (NullPointerException e) {
            return defaultConfiguration.get(entry.name());
        }
    }

    private void set(ConfigEntry entry, Object value) {
        if (configuration.containsKey(entry.name())) configuration.replace(entry.name(), value);
        else configuration.put(entry.name(), value);
    }

    public void setValue(ConfigEntry entry, Object value) {
        switch (entry) {
            default:
                set(entry, value);
                break;
            case toolbarPos:
                switch ((String)value) {
                    case "Top":
                        set(entry, 0);
                        break;
                    case "Right":
                        set(entry, 1);
                        break;
                    case "Bottom":
                        set(entry, 2);
                        break;
                    default:
                    case "Left":
                        set(entry, 3);
                        break;
                }
                break;
        }
    }

}
