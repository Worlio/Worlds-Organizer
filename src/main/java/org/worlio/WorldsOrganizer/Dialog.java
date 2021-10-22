package org.worlio.WorldsOrganizer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Dialog {

    public static void showWindowDialog(String title, Pane pane) {
        showWindowDialog(title, pane, null);
    }

    public static void showWindowDialog(String title, Pane pane, Collection<Button> buttons) {
        Stage dStage = new Stage();
        dStage.initOwner(Main.mainStage);
        dStage.setTitle(title);

        Button cancelButton = new Button("Close");
        cancelButton.setCancelButton(true);
        cancelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> dStage.close());

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().add(cancelButton);
        if (buttons != null) buttonBar.getButtons().addAll(buttons);
        buttonBar.setPadding(new Insets(10, 10, 10, 10));

        dStage.setMinWidth(400);
        dStage.setMinHeight(400);

        dStage.setScene(new Scene(new VBox(pane, buttonBar), 400, 400));
        dStage.show();
    }

    public static void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("An Error Occurred");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(Main.mainStage);

        alert.getDialogPane().setMinSize(200,200);
        alert.showAndWait();
    }

    public static void showException(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("An Exception was encountered.");
        alert.setContentText("Please file a bug report if this problem persists.");
        alert.initOwner(Main.mainStage);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.getDialogPane().setMinSize(200,200);
        alert.showAndWait();
    }

    public static WorldsType newFileList() {
        List<String> choices = new ArrayList<>();
        choices.add("Avatars");
        choices.add("WorldsMarks");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("New File");
        dialog.setHeaderText("Select a type for the new file.");
        dialog.setContentText("Type:");
        dialog.initOwner(Main.mainStage);

        dialog.getDialogPane().setMinSize(200,200);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if (result.get().equals(choices.get(0)))
                return WorldsType.AVATAR;
            else if (result.get().equals(choices.get(1)))
                return WorldsType.WORLDSMARK;
            else return WorldsType.NULL;
        } else {
            return WorldsType.NULL;
        }
    }

    public static void showUpdate(Version newVer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        assert Main.verMan != null;
        alert.setTitle("Update Dialog");
        alert.setHeaderText("A new version (v" + newVer.get() + ") of Organizer is available for download.\nYou are currently on v" + Console.getVersion() + ".");
        alert.initOwner(Main.mainStage);

        ButtonType updateButton = new ButtonType("Open");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Label label = new Label("View Changelog");

        TextArea textArea = new TextArea(Console.changelogify(Main.verMan.getChangelog(newVer)));
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getButtonTypes().setAll(updateButton, buttonTypeCancel);

        alert.getDialogPane().setMinSize(200,200);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == updateButton) {
            Console.print("Displaying webpage in native browser.", 1, ConsoleType.INFO);
            Main.hostServices.showDocument(Main.verMan.url.replace("{ver}", newVer.get()));
        } else {
            alert.close();
        }
    }

    public static void process() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(
                "For all you give me, an " + "ea" + "st" + "er" + " " + "e" + "gg" + " for you."
        );
        alert.setContentText(
                "I" + " l" + "ov" + "e" + " yo" + "u," + "DO" + "SF" + "OX" + "!"
        );
        alert.initOwner(Main.mainStage);

        alert.getDialogPane().setMinSize(200,200);
        alert.showAndWait();
    }
}
