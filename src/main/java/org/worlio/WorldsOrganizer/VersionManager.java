package org.worlio.WorldsOrganizer;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VersionManager {

    String url;
    public Version currentVersion = new Version(Console.getVersion());
    public Version newVersion;
    static JsonMapper mapper = new JsonMapper();
    Map<?, ?> json;
    int format;

    private boolean isValid() {
        return format == 2;
    }

    VersionManager() throws IOException {
        json = readJsonFromUrl(Main.configManager.getStringValue(ConfigEntry.updateURL));
        format = (int)json.get("format");
        if (!isValid()) throw new IOException("Invalid JSON Update format: Format Version is not 2");
        url = (String) json.get("url");
        Map<?, ?> updates = (Map<?, ?>) json.get("versions");
        LinkedHashMap<?, ?> ver = (LinkedHashMap<?, ?>) updates.get(Main.configManager.getStringValue(ConfigEntry.channel));
        newVersion = new Version((String)ver.keySet().toArray()[0]);
    }

    public boolean hasUpdate() {
        if (!isValid()) return false;
        return newVersion.compareTo(currentVersion) >= 1;
    }

    public void pushUpdate() {
        if (hasUpdate() && isValid()) {
            Dialog.showUpdate(newVersion);
        } else Console.print("No new updates available.");
    }

    public List<String> getChangelog(Version ver) {
        if (!isValid()) return new ArrayList<>();

        // God is horrified, and he has every right to be.
        return (List<String>)((LinkedHashMap<?, ?>) ((Map<?,?>)json.get("versions")).get(Main.configManager.getStringValue(ConfigEntry.channel))).get(ver.get());
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static Map<?,?> readJsonFromUrl(String url) throws IOException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return mapper.readValue(jsonText, Map.class);
        }
    }

}
