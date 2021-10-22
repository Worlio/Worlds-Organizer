package org.worlio.WorldsOrganizer;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Properties;

enum ConsoleType {
    ERROR(5),
    WARNING(4),
    DEBUG(3),
    SUCCESS(2),
    INFO(1),
    DEFAULT(0);

    private final int value;

    ConsoleType(int value) {
        this.value = value;
    }
}

public class Console {

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    final static Properties properties = new Properties();

    public static void print(String message) {
        print(message, ConsoleType.INFO);
    }

    public static void print(String message, ConsoleType type) {
        print(message, 0, type);
    }

    public static void print(String message, int debug, ConsoleType type) {
        if (Main.debugMode >= debug) {
            String output = "";
            switch (type) {
                case DEFAULT:
                    break;
                case INFO:
                    output = BLUE + "INFO " + RESET;
                    break;
                case SUCCESS:
                    output = GREEN + "SUCCESS " + RESET;
                    break;
                case DEBUG:
                    output = CYAN + "DEBUG " + RESET;
                    break;
                case WARNING:
                    output = YELLOW + "WARNING " + RESET;
                    break;
                case ERROR:
                    output = RED + "ERROR " + RESET;
                    break;
            }
            output += message;
            System.out.println(output);
        }
    }

    private static String getProperty(String property) {
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
            return properties.getProperty(property);
        } catch (IOException e) {
            return "?";
        }
    }

    public static String getVersion(){
        return getProperty("version");
    }

    public static String getDate(){
        return getProperty("buildDate");
    }

    public static String getHelp() {
        String command;
        command = "WorldsOrganizer.jar [OPTIONS]\n" +
                "Commands:\n" +
                commandCreate("   --debug=n", "Enable debug mode, which displays extra log information in the command-line. (1 - Logging, 2 - Complete Debug)") + "\n" +
                commandCreate("-i --input", "Start application with input files.") + "\n" +
                commandCreate("-h --help", "Show this output.");

        return command;
    }

    private static String commandCreate(String cmd, String info) {
        return "     " + cmd + "                         ".substring(cmd.length()) + info;
    }

    public static boolean testURL(String address) throws IOException {
        int responseCode;
        try {
            URL url = new URL(address);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");
            huc.setConnectTimeout(15000);

            responseCode = huc.getResponseCode();
        } catch (Exception e) {
            responseCode = 404;
        }

        return HttpURLConnection.HTTP_OK == responseCode;
    }

    public static void process() {
        Dialog.process();
    }

    public static File getParent() {
        try {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException e) {
            Dialog.showException(e);
            return null;
        }
    }

    public static String changelogify(List<String> list) {
        StringBuilder changes = new StringBuilder();
        for (String line : list) {
            changes.append(" - ").append(line).append("\n");
        }
        return changes.toString();
    }

}
