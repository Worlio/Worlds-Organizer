package org.worlio.WorldsOrganizer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WorldsTab {

    File file;

    private Tab tab = null;
    Control content = null;
    Pane mainPane;

    private boolean showingFinder = false;
    private boolean modified = false;

    WorldListObject worldList = new WorldListObject();

    private final CommandStack commandStack = new CommandStack();

    public WorldsTab() {
    }

    public Tab getTab(File file) {
        return getTab(WorldsType.NULL, file);
    }

    public Tab getTab(WorldsType type) {
        return getTab(type, null);
    }

    public Tab getTab(WorldsType type, File file) {
        if (tab != null) {
            return tab;
        } else {
            Restorer restorer;
            if (file != null) {
                this.file = file;
                try {
                    restorer = new Restorer(file);
                    worldList = restorer.read();
                } catch (IOException e) {
                    return null;
                }
            } else {
                assert type != null;
                worldList.classType = type;
                worldList.add(createItem(type));
            }

            Tab tab;
            switch (worldList.classType) {
                default:
                    tab = new Tab(); break;
                case AVATAR: case WORLDSMARK:
                    tab = new Tab(generateTitle(), getWorldList()); break;
            }
            this.tab = tab;

            update();

            tab.setOnCloseRequest(event -> {
                Console.print("Performing tab close", 1, ConsoleType.INFO);
                event.consume();
                quitTab();
            });

            return tab;
        }
    }

    public Pane getWorldList() {
        if (mainPane != null) {
            return mainPane;
        } else {
            content = new TableView<WorldList>();
            ((TableView<?>)content).setEditable(true);

            ToolBar toolBar = new ToolBar();

            Console.print("Tab initialized", 1, ConsoleType.INFO);

            Button addBtn = new Button();
            addBtn.setTooltip(new Tooltip("Add Value"));
            addBtn.setGraphic(new ImageView(AppIcon.add));
            toolBar.getItems().add(addBtn);

            addBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tabs.indexOf(this);

                if (tabIndex >= 0) {
                    this.addItem();
                    this.setFocus(((TableView<?>)content).getItems().size() - 1);

                    Main.tabs.set(tabIndex, this);
                }
            });

            Button delBtn = new Button();
            delBtn.setTooltip(new Tooltip("Delete Value"));
            delBtn.setGraphic(new ImageView(AppIcon.remove));
            toolBar.getItems().add(delBtn);

            delBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tabs.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView<?>)content).getSelectionModel().getFocusedIndex();
                    this.deleteItem(index);
                    this.setFocus(index < ((TableView<?>)content).getItems().size() ? index : index - 1);

                    Main.tabs.set(tabIndex, this);
                }
            });

            Button mupBtn = new Button();
            mupBtn.setTooltip(new Tooltip("Move Value Up"));
            mupBtn.setGraphic(new ImageView(AppIcon.moveUp));
            toolBar.getItems().add(mupBtn);

            mupBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tabs.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView<?>)content).getSelectionModel().getFocusedIndex();
                    this.moveValue(index, -1);
                    this.setFocus(index - 1);

                    Main.tabs.set(tabIndex, this);
                }
            });

            Button mdwBtn = new Button();
            mdwBtn.setTooltip(new Tooltip("Move Value Down"));
            mdwBtn.setGraphic(new ImageView(AppIcon.moveDown));
            toolBar.getItems().add(mdwBtn);

            mdwBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                int tabIndex = Main.tabs.indexOf(this);

                if (tabIndex >= 0) {
                    int index = ((TableView<?>)content).getSelectionModel().getFocusedIndex();
                    this.moveValue(index, 1);
                    this.setFocus(index + 1);

                    Main.tabs.set(tabIndex, this);
                }
            });

            toolBar.getItems().add(new Separator());

            Button findBtn = new Button();
            findBtn.setTooltip(new Tooltip("Find/Replace"));
            findBtn.setGraphic(new ImageView(AppIcon.findReplace));
            toolBar.getItems().add(findBtn);

            VBox findingBox = getFindPane();
            VBox.setVgrow(findingBox, Priority.ALWAYS);
            findingBox.setVisible(false);
            findingBox.setManaged(false);

            findBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                Console.print("Toggled Find/Replace pane", 1, ConsoleType.INFO);
                findingBox.setManaged(!showingFinder);
                findingBox.setVisible(!showingFinder);
                showingFinder = !showingFinder;
            });

            Button checkBtn = new Button();
            checkBtn.setTooltip(new Tooltip("Link Checker"));
            checkBtn.setGraphic(new ImageView(AppIcon.linkCheck));
            toolBar.getItems().add(checkBtn);

            checkBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                Console.print("Beginning Link Checker", 1, ConsoleType.INFO);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.getDialogPane().setMinWidth(600);
                alert.setResizable(true);
                alert.setTitle("Link Checker");
                alert.setHeaderText("Checking Links...");
                alert.initOwner(Main.mainStage);

                TableView<WorldTableItem> checkTable = new TableView<>();
                checkTable.setEditable(false);

                checkTable.setMaxWidth(Double.MAX_VALUE);
                checkTable.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(checkTable, Priority.ALWAYS);
                GridPane.setHgrow(checkTable, Priority.ALWAYS);

                TableColumn<WorldTableItem, Integer> checkIndexColumn = new TableColumn<>("#");
                checkIndexColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.05));
                checkIndexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));

                TableColumn<WorldTableItem, String> checkLabelColumn = new TableColumn<>("Label");
                checkLabelColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.4));
                checkLabelColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

                TableColumn<WorldTableItem, String> checkValueColumn = new TableColumn<>("Value");
                checkValueColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.4));
                checkValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

                TableColumn<WorldTableItem, String> checkStatusColumn = new TableColumn<>("Status");
                checkStatusColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.1));
                checkStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

                checkTable.getColumns().addAll(checkIndexColumn, checkLabelColumn, checkValueColumn, checkStatusColumn);

                alert.getDialogPane().setContent(checkTable);

                alert.initOwner(Main.mainStage.getOwner());

                List<WorldTableItem> errorItems = new ArrayList<>();

                AtomicBoolean haltThread = new AtomicBoolean(false);

                Task<Boolean> task = new Task<Boolean>() {
                    @Override public Boolean call() {

                        for (int w = 0; w < ((TableView<?>)content).getItems().size(); w++) {
                            if (haltThread.get()) return true;
                            WorldList wl = (WorldList)((TableView<?>)content).getItems().get(w);

                            String value = wl.getValue();
                            int index = ((TableView<?>)content).getItems().indexOf(wl);

                            WorldTableItem tabItem;
                            if (value.startsWith("http")) {
                                Console.print("Trying '" + value + "'", 1, ConsoleType.INFO);
                                try {
                                    if (!Console.testURL(value)) {
                                        Console.print("URL '" + value + "' failed!", 1, ConsoleType.ERROR);
                                        tabItem = new WorldTableItem(index, wl.getName(), value, false);
                                        errorItems.add(tabItem);
                                    } else {
                                        Console.print("URL '" + value + "' passed!", 1, ConsoleType.SUCCESS);
                                        tabItem = new WorldTableItem(index, wl.getName(), value, true);
                                    }
                                    checkTable.getItems().add(0, tabItem);
                                } catch (IOException ioException) {
                                    Console.print("IOException encountered while checking URLs for Link Checker.", ConsoleType.ERROR);
                                    ioException.printStackTrace();
                                    Dialog.showException(ioException);
                                    return false;
                                }
                            }
                            checkTable.refresh();
                        }

                        return true;
                    }
                };



                task.setOnRunning((a) -> alert.show());
                task.setOnSucceeded((a) -> {
                    alert.close();
                    showLinkResults(errorItems);
                });
                task.setOnFailed((a) -> {
                    Console.print("Link Checking failed for unknown reasons.", ConsoleType.ERROR);
                    Dialog.showException(new Exception("Unknown"));
                    alert.close();
                });
                Thread taskTh = new Thread(task);
                taskTh.start();

                alert.setOnCloseRequest((a) -> {
                    Console.print("Closing Link Check", 1, ConsoleType.INFO);
                    haltThread.set(true);
                    alert.close();
                });

            });

            TableColumn<WorldList, String> indexColumn = new TableColumn<>("#");
            indexColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.05));
            indexColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(((TableView<?>)content).getItems().indexOf(p.getValue())));
            indexColumn.setSortable(false);
            indexColumn.setEditable(false);

            TableColumn<WorldList, String> labelColumn = new TableColumn<>("Label");
            labelColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.4));
            labelColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            labelColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            labelColumn.setSortable(false);

            labelColumn.setOnEditCommit(t -> {
                commandStack.doCommand(new Command() {
                    final String oldValue = t.getOldValue();
                    final String newValue = t.getNewValue();
                    WorldList item = t.getTableView().getItems().get(t.getTablePosition().getRow());

                    @Override
                    public void execute() {
                        Console.print("Editing Label: '" + oldValue + "' -> '" + newValue + "'", 1, ConsoleType.DEBUG);
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(newValue);
                        ((TableView<?>) content).refresh();
                        setSaved(false);
                    }

                    @Override
                    public void undo() {
                        Console.print("Undoing Label: '" + oldValue + "' <- '" + newValue + "'", 1, ConsoleType.DEBUG);
                        item.setName(oldValue);
                        ((TableView<?>)content).refresh();
                        setSaved(false);
                    }
                });
            });

            TableColumn<WorldList, String> valueColumn = new TableColumn<>("Value");
            valueColumn.prefWidthProperty().bind(content.widthProperty().multiply(0.525));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            valueColumn.setSortable(false);

            valueColumn.setOnEditCommit(t -> {
                commandStack.doCommand(new Command() {

                    final String oldValue = t.getOldValue();
                    final String newValue = t.getNewValue();
                    WorldList item = t.getTableView().getItems().get(t.getTablePosition().getRow());

                    @Override
                    public void execute() {
                        Console.print("Editing Value: '" + oldValue + "' -> '" + newValue + "'", 1, ConsoleType.DEBUG);
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(newValue);
                        ((TableView<?>) content).refresh();
                        setSaved(false);
                    }

                    @Override
                    public void undo() {
                        Console.print("Undoing Value: '" + oldValue + "' <- '" + newValue + "'", 1, ConsoleType.DEBUG);
                        item.setValue(oldValue);
                        ((TableView<?>)content).refresh();
                        setSaved(false);
                    }
                });
            });

            for (WorldList list : worldList.getValues()) {
                ((TableView<WorldList>)content).getItems().add(list);
            }
            Console.print("List values added.", 1, ConsoleType.INFO);

            ((TableView<WorldList>)content).getColumns().addAll(indexColumn, labelColumn, valueColumn);

            VBox.setVgrow(content, Priority.ALWAYS);
            HBox.setHgrow(toolBar, Priority.ALWAYS);
            VBox.setVgrow(toolBar, Priority.ALWAYS);

            VBox endV = new VBox(content, findingBox);
            HBox.setHgrow(endV, Priority.ALWAYS);
            VBox.setVgrow(endV, Priority.ALWAYS);

            switch (Main.configManager.getIntValue(ConfigEntry.toolbarPos)) {
                case 0:
                    toolBar.setOrientation(Orientation.HORIZONTAL);
                    return new VBox(toolBar, endV);
                case 1:
                    toolBar.setOrientation(Orientation.VERTICAL);
                    return new HBox(endV, toolBar);
                case 2:
                    toolBar.setOrientation(Orientation.HORIZONTAL);
                    return new VBox(endV, toolBar);
                default:
                case 3:
                    toolBar.setOrientation(Orientation.VERTICAL);
                    return new HBox(toolBar, endV);
            }
        }
    }

    public void addItem() {
        assert content instanceof TableView;

        commandStack.doCommand(new Command() {

            WorldList newData;

            @Override
            public void execute() {
                Console.print("Adding new item", 1, ConsoleType.DEBUG);
                newData = createItem(worldList.classType);
                worldList.add(newData);
                ((TableView)content).getItems().add(newData);
                setSaved(false);
            }

            @Override
            public void undo() {
                Console.print("Undoing new item", 1, ConsoleType.DEBUG);
                worldList.remove(newData);
                ((TableView)content).getItems().remove(newData);
                setSaved(false);
            }
        });
    }

    public void deleteItem(int i) {
        assert content instanceof TableView;

        if (i >= 0 && ((TableView<?>)content).getItems().size() > i) {
            commandStack.doCommand(new Command() {

                WorldList item = (WorldList) ((TableView<?>) content).getItems().get(i);
                int index = i;

                @Override
                public void execute() {
                    Console.print("Deleting Item", 1, ConsoleType.DEBUG);
                    worldList.remove(i);
                    ((TableView) content).getItems().remove(i);
                    setSaved(false);
                }

                @Override
                public void undo() {
                    Console.print("Undoing Delete", 1, ConsoleType.DEBUG);
                    worldList.add(index, item);
                    ((TableView) content).getItems().add(index, item);
                    setSaved(false);
                }
            });
        } else Console.print("Unable to delete item: No item selected!", ConsoleType.WARNING);
    }

    public void moveValue(int i, int moveBy) {
        assert content instanceof TableView;

        if (i >= 0 && i + moveBy >= 0 && ((TableView<?>)content).getItems().size() > i && ((TableView<?>)content).getItems().size() > i + moveBy) {

            commandStack.doCommand(new Command() {

                final WorldList item = (WorldList) ((TableView) content).getItems().get(i);
                final WorldList rItem = (WorldList) ((TableView) content).getItems().get(i + moveBy);
                final int index = i;
                final int moved = moveBy;

                @Override
                public void execute() {
                    Console.print("Moving index " + index + " by " + moveBy, 1, ConsoleType.DEBUG);
                    doMove(i, rItem);
                    doMove(i + moveBy, item);
                    setSaved(false);
                }

                @Override
                public void undo() {
                    Console.print("Undoing move of index " + index + " by " + moveBy, 1, ConsoleType.DEBUG);
                    doMove(index + moved, rItem);
                    doMove(index, item);
                    setSaved(false);
                }

                private void doMove(int i, WorldList o) {
                    ((TableView) content).getItems().set(i, o);
                    worldList.set(i, o);
                }
            });
        } else Console.print("Cannot move item.", ConsoleType.WARNING);
    }

    public void setFocus(int i) {
        Console.print("Forced Focus on index " + i, 1, ConsoleType.DEBUG);
        ((TableView)content).getSelectionModel().select(i);
        ((TableView)content).scrollTo(i);
    }

    private void quitTab() {
        if (!getSaved()) {
            Console.print("Displaying Quit alert", 1, ConsoleType.DEBUG);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Close Tab");
            alert.setHeaderText("Are you sure you want to close this tab?");
            alert.setContentText("You have unsaved changes. Closing now will lose your progress.");
            alert.initOwner(Main.mainStage);

            ButtonType dontSaveButton = new ButtonType("Discard Changes");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(dontSaveButton, buttonTypeCancel);

            alert.getDialogPane().setMinSize(200,200);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == dontSaveButton) {
                closeTab();
            } else {
                alert.close();
                Main.setStatusText("Tab Alert Closed");
            }
        } else {
            closeTab();
        }

    }

    private void closeTab() {
        Main.setStatusText("Closing Tab");
        int index = tab.getTabPane().getSelectionModel().getSelectedIndex() - 1;
        Main.tabs.remove(index);

        EventHandler<Event> handler = tab.getOnClosed();
        if (null != handler) {
            handler.handle(null);
        } else {
            tab.getTabPane().getTabs().remove(tab);
        }
        Console.print("Tab closed", 1, ConsoleType.DEBUG);
    }

    private void setIcon(WorldsType type) {
        switch (type) {
            default:
                tab.setGraphic(new ImageView(AppIcon.unknownFile));
                break;
            case AVATAR:
                tab.setGraphic(new ImageView(AppIcon.avatarFile));
                break;
            case WORLDSMARK:
                tab.setGraphic(new ImageView(AppIcon.markFile));
                break;
        }
    }

    public void update() {
        Console.print("Performing tooltip and title update", 1, ConsoleType.DEBUG);
        if (file != null) tab.setTooltip(new Tooltip(file.getAbsolutePath()));
        else tab.setTooltip(new Tooltip(worldList.classType.name));
        tab.setText(generateTitle());
        setIcon(worldList.classType);
    }

    public void update(File newFile) {
        this.file = newFile;
        update();
    }

    private String generateTitle() {
        if (file != null) {
            return file.getName();
        } else {
            switch (worldList.classType) {
                default:
                    return "Untitled";
                case AVATAR:
                    return "Untitled.avatars";
                case WORLDSMARK:
                    return "Untitled.worldsmarks";
            }
        }
    }

    public void setSaved(boolean value) {
        if (!value) {
            modified = true;
            tab.setGraphic(new ImageView(AppIcon.saveFile));
        } else{
            update();
            modified = false;
        }
    }

    public boolean getSaved() {
        return !modified;
    }

    public WorldList createItem(WorldsType type) {
        switch (type) {
            default:
                return null;
            case AVATAR:
                return new AvatarObject("New Avatar", "avatar:holden.mov");
            case WORLDSMARK:
                return new MarkObject("New Mark", "home:GroundZero/groundzero.world");
        }
    }

    public void doUndo() {
        if (commandStack.canUndo()) {
            commandStack.undo();
            Main.setStatusText("Undid last operation.");
        } else {
            Console.print("Nothing to undo.");
            Main.setStatusText("Nothing to undo.");
        }
    }

    public void doRedo() {
        if (commandStack.canRedo()) {
            commandStack.redo();
            Main.setStatusText("Redid last operation.");
        } else {
            Console.print("Nothing to redo.");
            Main.setStatusText("Nothing to redo.");
        }
    }

    public void showLinkResults(List<WorldTableItem> list) {
        Console.print("Displaying Link Checker results", 1, ConsoleType.INFO);
        AtomicInteger addition = new AtomicInteger();
        addition.set(0);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setMinWidth(800);
        alert.setResizable(true);
        alert.setTitle("Link Checker Results");
        alert.setHeaderText("The followings links have been found to be dead: " + list.size() + " out of " + worldList.size());
        alert.initOwner(Main.mainStage);

        Button deleteBtn = new Button();
        deleteBtn.setTooltip(new Tooltip("Delete"));
        deleteBtn.setGraphic(new ImageView(AppIcon.remove));
        Button deleteAllBtn = new Button("Delete All");
        deleteAllBtn.setTooltip(new Tooltip("Delete All"));
        deleteAllBtn.setGraphic(new ImageView(AppIcon.removeAll));

        TableView<WorldTableItem> errorTable = new TableView<>();
        errorTable.setEditable(true);

        errorTable.setMaxWidth(Double.MAX_VALUE);
        errorTable.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(errorTable, Priority.ALWAYS);
        GridPane.setHgrow(errorTable, Priority.ALWAYS);

        TableColumn<WorldTableItem, Integer> indexColumn = new TableColumn<>("#");
        indexColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.05));
        indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
        indexColumn.setEditable(false);

        TableColumn<WorldTableItem, String> labelColumn = new TableColumn<>("Label");
        labelColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.4));
        labelColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        labelColumn.setCellFactory(TextFieldTableCell.<WorldTableItem>forTableColumn());
        labelColumn.setEditable(true);
        labelColumn.setOnEditCommit(t -> {
            commandStack.doCommand(
                    new Command() {

                        String oldValue = t.getOldValue();
                        String newValue = t.getNewValue();
                        WorldList item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                        WorldList tableItem = ((WorldList) ((TableView) content).getItems().get(t.getRowValue().getIndex() + addition.get()));

                        @Override
                        public void execute() {
                            item.setName(newValue);
                            tableItem.setName(newValue);
                            ((TableView) content).refresh();
                            setSaved(false);
                        }

                        @Override
                        public void undo() {
                            item.setName(oldValue);
                            tableItem.setName(oldValue);
                            ((TableView) content).refresh();
                            setSaved(false);
                        }
                    });
        });

        TableColumn<WorldTableItem, String> valueColumn = new TableColumn<>("Value");
        valueColumn.prefWidthProperty().bind(errorTable.widthProperty().multiply(0.525));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.<WorldTableItem>forTableColumn());
        valueColumn.setEditable(true);
        valueColumn.setOnEditCommit(t -> {
            commandStack.doCommand(
                    new Command() {

                        String oldValue = t.getOldValue();
                        String newValue = t.getNewValue();
                        WorldList item = t.getTableView().getItems().get(t.getTablePosition().getRow());
                        WorldList tableItem = ((WorldList) ((TableView) content).getItems().get(t.getRowValue().getIndex() + addition.get()));

                        @Override
                        public void execute() {
                            item.setValue(newValue);
                            tableItem.setValue(newValue);
                            ((TableView) content).refresh();
                            setSaved(false);
                        }

                        @Override
                        public void undo() {
                            item.setValue(oldValue);
                            tableItem.setValue(oldValue);
                            ((TableView) content).refresh();
                            setSaved(false);
                        }
                    });
        });

        for (WorldTableItem item : list) {
            errorTable.getItems().add(item);
        }


        ToolBar btnBar = new ToolBar();
        btnBar.getItems().addAll(deleteBtn, deleteAllBtn);

        errorTable.getColumns().addAll(indexColumn, labelColumn, valueColumn);

        VBox vBox = new VBox(errorTable, btnBar);

        alert.getDialogPane().setContent(vBox);

        deleteBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> { commandStack.doCommand(
            new Command() {

                int selected = errorTable.getSelectionModel().getSelectedIndex();
                int tableSel = errorTable.getItems().get(selected).getIndex();
                WorldTableItem item = errorTable.getItems().get(selected);
                int added = tableSel + addition.get();

                @Override
                public void execute() {
                    worldList.remove(tableSel + addition.get());
                    ((TableView)content).getItems().remove(tableSel + addition.get());

                    errorTable.getItems().remove(selected);
                    list.remove(selected);
                    addition.getAndDecrement();
                    setSaved(false);
                }

                @Override
                public void undo() {
                    worldList.add(added, item);
                    ((TableView)content).getItems().add(added, item);

                    errorTable.getItems().add(selected, item);
                    list.add(selected, item);
                    addition.getAndIncrement();
                    setSaved(false);
                }
            });
        });

        deleteAllBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            Alert askSure = new Alert(Alert.AlertType.CONFIRMATION);
            askSure.setTitle("Delete All?");
            askSure.setHeaderText("Are you sure you want to delete all the items in this list?");
            askSure.setContentText("You won't be able to undo this change!");

            ButtonType delSureButton = new ButtonType("Discard Changes");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            askSure.getButtonTypes().setAll(delSureButton, buttonTypeCancel);

            askSure.getDialogPane().setMinSize(200,200);
            Optional<ButtonType> result = askSure.showAndWait();
            if (result.get() == delSureButton) {
                for (WorldTableItem item : list) {
                    errorTable.getItems().remove(0);
                    worldList.remove(item.getIndex() + addition.get());
                    ((TableView) content).getItems().remove(item.getIndex() + addition.get());
                    addition.getAndDecrement();
                }
                setSaved(false);
            } else {
                askSure.close();
            }
        });

        alert.showAndWait();
    }

    private VBox getFindPane() {
        Console.print("Initializing FindPane", 1, ConsoleType.INFO);
        Text findText = new Text("Find Text");
        findText.setFont(Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 12));
        TextField findInput = new TextField();

        Text replText = new Text("Replace Text");
        replText.setFont(Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 12));
        TextField replInput = new TextField();

        Slider selSlider = new Slider(-1,1,0);
        selSlider.setMajorTickUnit(1);
        selSlider.setMinorTickCount(0);
        selSlider.setSnapToTicks(true);
        Text labelTxt = new Text("Label");
        Text valueTxt = new Text("Value");

        HBox.setHgrow(selSlider, Priority.ALWAYS);
        HBox.setHgrow(labelTxt, Priority.ALWAYS);
        HBox.setHgrow(valueTxt, Priority.ALWAYS);

        GridPane findBar = new GridPane();

        Button findButton = new Button("Find");
        findButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            Console.print("Performing find", 1, ConsoleType.INFO);
            int curI = ((TableView)content).getSelectionModel().getSelectedIndex();
            if (curI < 0) curI = 0;
            Main.setStatusText("Searching...");
            for (int a = curI+1; a < worldList.size(); a++) {
                if (worldList.get(a).getName().contains(findInput.getCharacters()) || worldList.get(a).getValue().contains(findInput.getCharacters())) {
                    setFocus(a);
                    break;
                }
            }
        });

        Button replButton = new Button("Replace");
        replButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            commandStack.doCommand(new Command() {

                WorldList replaced;
                final int index = ((TableView<WorldList>)content).getSelectionModel().getSelectedIndex();
                final double slider = selSlider.getValue();

                @Override
                public void execute() {
                    Console.print("Performing replace", 1, ConsoleType.INFO);
                    WorldList item = ((TableView<WorldList>)content).getSelectionModel().getSelectedItem();
                    if (item.getName().contains(findInput.getCharacters()) || item.getValue().contains(findInput.getCharacters())) {
                        WorldTableItem tableItem = new WorldTableItem(((TableView) content).getItems().indexOf(item), item.getName(), item.getValue());
                        if (selSlider.getValue() >= 0)
                            item.setValue(item.getValue().replace(findInput.getCharacters(), replInput.getCharacters()));
                        if (selSlider.getValue() <= 0)
                            item.setName(item.getName().replace(findInput.getCharacters(), replInput.getCharacters()));
                        Main.setStatusText("Replaced item");
                        replaced = tableItem;
                    }
                    ((TableView) content).refresh();
                    setSaved(false);
                }

                @Override
                public void undo() {
                    WorldList item = (WorldList)((TableView) content).getItems().get(index);
                    if (slider >= 0) item.setValue(replaced.getValue());
                    if (slider <= 0) item.setName(replaced.getName());
                    ((TableView) content).refresh();
                    setSaved(false);
                }
            });
        });

        Button replAllButton = new Button("Replace All");
        replAllButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            commandStack.doCommand(new Command() {

                final List<WorldTableItem> replaced = new ArrayList<>();
                final double slider = selSlider.getValue();

                @Override
                public void execute() {
                    Console.print("Performing replace all", 1, ConsoleType.INFO);
                    int count = 0;
                    for (WorldList item : worldList.getValues()) {
                        if (item.getName().contains(findInput.getCharacters()) || item.getValue().contains(findInput.getCharacters())) {
                            count++;
                            WorldTableItem tableItem = new WorldTableItem(((TableView)content).getItems().indexOf(item), item.getName(), item.getValue());
                            if (selSlider.getValue() >= 0) item.setValue(item.getValue().replace(findInput.getCharacters(), replInput.getCharacters()));
                            if (selSlider.getValue() <= 0) item.setName(item.getName().replace(findInput.getCharacters(), replInput.getCharacters()));
                            replaced.add(tableItem);
                        }
                    }
                    Main.setStatusText("Replaced " + count + " items.");
                    ((TableView)content).refresh();
                    setSaved(false);
                }

                @Override
                public void undo() {
                    for (WorldTableItem tableItem : replaced) {
                        WorldList item = (WorldList)((TableView)content).getItems().get(tableItem.getIndex());
                        if (slider >= 0) item.setValue(tableItem.getValue());
                        if (slider <= 0) item.setName(tableItem.getName());
                    }
                    ((TableView) content).refresh();
                    setSaved(false);
                }
            });
        });

        ButtonBar btns = new ButtonBar();
        btns.getButtons().addAll(findButton, replButton, replAllButton);
        GridPane.setColumnSpan(btns, 2);

        findBar.add(findText, 0, 0);
        findBar.add(findInput, 1, 0);

        findBar.add(replText, 0, 1);
        findBar.add(replInput, 1, 1);

        findBar.add(new HBox(labelTxt, selSlider, valueTxt), 0, 2);
        findBar.add(btns, 1, 2);

        GridPane.setHgrow(findInput, Priority.ALWAYS);
        GridPane.setHgrow(replInput, Priority.ALWAYS);
        GridPane.setHgrow(btns, Priority.ALWAYS);
        findBar.setHgap(5);
        findBar.setVgap(5);

        findBar.setPadding(new Insets(10, 10, 10, 10));
        VBox.setVgrow(findBar, Priority.ALWAYS);

        Console.print("Completed FindPane", 1, ConsoleType.INFO);
        return new VBox(findBar);
    }
}
