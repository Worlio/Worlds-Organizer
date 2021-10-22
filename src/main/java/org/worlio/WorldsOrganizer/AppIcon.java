package org.worlio.WorldsOrganizer;

import javafx.scene.image.Image;

public class AppIcon {

    public static Image logo = new Image(Main.class.getResourceAsStream("/logo.png"));

    public static Image config = new Image(Main.class.getResourceAsStream("/icons/conf.png"), getSize(), getSize(), false, true);

    public static Image newFile = new Image(Main.class.getResourceAsStream("/icons/file-plus.png"), getSize(), getSize(), false, true);
    public static Image openFile = new Image(Main.class.getResourceAsStream("/icons/folder.png"), getSize(), getSize(), false, true);
    public static Image saveFile = new Image(Main.class.getResourceAsStream("/icons/save.png"), getSize(), getSize(), false, true);
    public static Image saveFileAs = new Image(Main.class.getResourceAsStream("/icons/save-as.png"), getSize(), getSize(), false, true);

    public static Image undo = new Image(Main.class.getResourceAsStream("/icons/undo.png"), getSize(), getSize(), false, true);
    public static Image redo = new Image(Main.class.getResourceAsStream("/icons/redo.png"), getSize(), getSize(), false, true);

    public static Image quitApp = new Image(Main.class.getResourceAsStream("/icons/quit.png"), getSize(), getSize(), false, true);

    public static Image add = new Image(Main.class.getResourceAsStream("/icons/plus.png"), getSize(), getSize(), false, true);
    public static Image remove = new Image(Main.class.getResourceAsStream("/icons/delete.png"), getSize(), getSize(), false, true);
    public static Image removeAll = new Image(Main.class.getResourceAsStream("/icons/delete-all.png"), getSize(), getSize(), false, true);
    public static Image moveUp = new Image(Main.class.getResourceAsStream("/icons/up.png"), getSize(), getSize(), false, true);
    public static Image moveDown = new Image(Main.class.getResourceAsStream("/icons/down.png"), getSize(), getSize(), false, true);

    public static Image findReplace = new Image(Main.class.getResourceAsStream("/icons/find.png"), getSize(), getSize(), false, true);
    public static Image linkCheck = new Image(Main.class.getResourceAsStream("/icons/link.png"), getSize(), getSize(), false, true);

    public static Image unknownFile = new Image(Main.class.getResourceAsStream("/icons/file.png"), getSize(), getSize(), false, true);
    public static Image avatarFile = new Image(Main.class.getResourceAsStream("/icons/avatar.png"), getSize(), getSize(), false, true);
    public static Image markFile = new Image(Main.class.getResourceAsStream("/icons/mark.png"), getSize(), getSize(), false, true);
    
    private static int getSize() {
        return Main.configManager.getIntValue(ConfigEntry.iconSize);
    }

}
