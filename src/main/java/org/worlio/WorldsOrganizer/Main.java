package org.worlio.WorldsOrganizer;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main extends Application {

	static int debugMode = 0;
	static ConfigManager configManager;
	static VersionManager verMan;

	static List<File> startFiles = new ArrayList<>();
	static List<WorldsTab> tabs = new ArrayList<>();

	private TabPane tabPane;
	private static Pane statusPane;

	public static Stage mainStage;
	static HostServices hostServices;

	@Override
	public void init() {
		Console.print("Worlds Organizer v" + Console.getVersion());
		Console.print("Running on Java " + System.getProperty("java.version"));

		configManager = new ConfigManager(new File((Console.getParent() + "/config.json")));

		try {
			verMan = new VersionManager();
		} catch (IOException e) {
			Console.print("An IOException was encountered attempting to obtain the version.", 1, ConsoleType.ERROR);
			e.printStackTrace();
		}

		if (debugMode < 1) debugMode = configManager.getBooleanValue(ConfigEntry.debug) ? 1 : 0;

		Console.print("Initializing main application.", ConsoleType.INFO);
		hostServices = this.getHostServices();
	}

	public static void main(String[] args) {

		boolean doRun = true;
		for (int a = 0; a < args.length; a++) {
			String arg = args[a];
			if (arg.startsWith("--debug")) {
				try {
					debugMode = Integer.parseInt(args[a+1]);
					Console.print("Debug mode set to: " + debugMode, 1, ConsoleType.INFO);
					a++;
				} catch (NumberFormatException e) {
					debugMode = 0;
					Console.print("NumberFormatException encountered! Defaulting to 0.", 1, ConsoleType.WARNING);
				}
			} else if (arg.equals("-i") || arg.equals("--input")) {
				try {
					File newFile = new File(args[a + 1]);
					startFiles.add(newFile);
					a++;
				} catch (Exception e) {
					Console.print("Invalid file location in argument!", 0, ConsoleType.ERROR);
				}
			} else if (arg.equals("-h") || arg.equals("--help")) {
				doRun = false;
				Console.getHelp();
				break;
			} else {
				doRun = false;
				Console.print(arg + " is not a valid argument. Please use '-help' to see a list of options and arguments.", 0, ConsoleType.WARNING);
				break;
			}
		}

		if (doRun) launch(args);
	}

	@Override
	public void start(final Stage stage) {
		mainStage = stage;
		mainStage.setTitle("Worlds Organizer");
		mainStage.getIcons().add(AppIcon.logo);

		Console.print("Starting Window", 1, ConsoleType.INFO);


		mainStage.setOnCloseRequest(a -> {
			quit();
		});


		ToolBar menuBar = new ToolBar();

		Button newFileBtn = new Button("New");
		newFileBtn.setGraphic(new ImageView(AppIcon.newFile));
		menuBar.getItems().add(newFileBtn);

		Button openFileBtn = new Button("Open");
		openFileBtn.setGraphic(new ImageView(AppIcon.openFile));
		menuBar.getItems().add(openFileBtn);

		Button saveFileBtn = new Button("Save");
		saveFileBtn.setGraphic(new ImageView(AppIcon.saveFile));
		menuBar.getItems().add(saveFileBtn);

		Button saveAsFileBtn = new Button("Save As");
		saveAsFileBtn.setGraphic(new ImageView(AppIcon.saveFileAs));
		menuBar.getItems().add(saveAsFileBtn);

		menuBar.getItems().add(new Separator());

		Button undoBtn = new Button();
		undoBtn.setGraphic(new ImageView(AppIcon.undo));
		undoBtn.setTooltip(new Tooltip("Undo"));
		menuBar.getItems().add(undoBtn);

		Button redoBtn = new Button();
		redoBtn.setGraphic(new ImageView(AppIcon.redo));
		redoBtn.setTooltip(new Tooltip("Redo"));
		menuBar.getItems().add(redoBtn);

		menuBar.getItems().add(new Separator());

		Button confBtn = new Button();
		confBtn.setTooltip(new Tooltip("Preferences"));
		confBtn.setGraphic(new ImageView(AppIcon.config));
		menuBar.getItems().add(confBtn);

		Button quitBtn = new Button("Quit");
		quitBtn.setGraphic(new ImageView(AppIcon.quitApp));
		menuBar.getItems().add(quitBtn);

		// Adding a tab pane
		tabPane = new TabPane();

		Console.print("Initializing Start Page", 1, ConsoleType.INFO);

		// This will be permanent, and be exempt from the tab values we record.
		Tab startTab = getStartPage();
		startTab.setClosable(false);

		tabPane.getTabs().add(startTab);

		// Assigning all the events now.
		Console.print("Assigning Events", 1, ConsoleType.INFO);
		tabPane.addEventFilter(Tab.CLOSED_EVENT, f -> {
			Console.print("Detected Tab Closed. Index " + (tabPane.getSelectionModel().getSelectedIndex() - 1) + ".", 1, ConsoleType.INFO);
			tabs.remove(tabPane.getSelectionModel().getSelectedIndex() - 1);
		});

		// First adding the status bar
		statusPane = new VBox(new Text());
		statusPane.setPadding(new Insets(4, 4, 4, 4));
		VBox vBox = new VBox(menuBar, tabPane);
		if (configManager.getBooleanValue(ConfigEntry.status)) vBox.getChildren().add(statusPane);
		Console.print("Completed Base Window Initialization", 1, ConsoleType.INFO);
		Scene scene = new Scene(vBox, 960, 600);

		newFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			if (e.isShiftDown()) {
				try {
					WorldListObject tempW = tabs.get(0).worldList;
					if (tempW.size() <= 1) {
						if (
								tempW.get(0).getName().equals(
										"D" + "F"
								) && tempW.get(0).getValue().equals(
										"6" + "/" + "2" + "7" + "/" + "2" + "0"
								)
						) {
							Console.process();
						} else newFile();
					} else newFile();
				} catch (NullPointerException | IndexOutOfBoundsException ignored) {}
			} else newFile();
		});

		openFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			openFile(null);
		});

		saveFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			try {
				WorldsTab tableObj = tabs.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
				saveFile(tableObj, tableObj.file);
				Main.setStatusText("Saved file to '" + tableObj.file.getAbsolutePath() + "'.");
			} catch (IndexOutOfBoundsException i) {
				Console.print("IndexOutOfBoundsException encountered! Must be on file tab to save.", ConsoleType.ERROR);
			}
		});

		saveAsFileBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			try {
				WorldsTab tableObj = tabs.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
				saveFile(tableObj);
			} catch (IndexOutOfBoundsException i) {
				Console.print("IndexOutOfBoundsException encountered! Must be on file tab to save.", ConsoleType.ERROR);
			}
		});

		undoBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			if (tabs.size() > 0 && tabPane.getSelectionModel().getSelectedIndex() > 0) {
				WorldsTab tableObj = tabs.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
				tableObj.doUndo();
			}
		});

		redoBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			if (tabs.size() > 0 && tabPane.getSelectionModel().getSelectedIndex() > 0) {
			WorldsTab tableObj = tabs.get(tabPane.getSelectionModel().getSelectedIndex() - 1);
			tableObj.doRedo();
			}
		});

		confBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			// This is it's own window to circumvent the issues with Buttons
			// and design on Dialog.

			Stage confStage = new Stage();
			confStage.initOwner(mainStage);
			confStage.setTitle("Preferences");

			Console.print("Displaying Configuration Window", 1, ConsoleType.INFO);


			// General Tab

			Label channelName = new Label("Update Channel");
			channelName.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
			Label channelInfo = new Label("Channel to use for update checks and links: 'stable' will provide tested " +
					"build updates while 'beta' is more of an experimental bug-finding experience.");
			channelInfo.setWrapText(true);

			ObservableList<String> channels =
					FXCollections.observableArrayList(ConfigManager.channels);

			ComboBox<String> channelOption = new ComboBox(channels);
			channelOption.getSelectionModel().select(configManager.getStringValue(ConfigEntry.channel));
			HBox.setHgrow(channelOption, Priority.ALWAYS);


			// Appearance Tab

			Label darkName = new Label("Use Dark Mode");
			darkName.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
			Label darkInfo = new Label("Changes the style of the application to use a basic dark theme.");
			darkInfo.setWrapText(true);

			CheckBox darkOption = new CheckBox("Enabled");
			darkOption.setSelected(configManager.getBooleanValue(ConfigEntry.darkMode));
			darkOption.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
				configManager.setValue(ConfigEntry.darkMode, isSelected);
			});
			VBox.setVgrow(darkOption, Priority.ALWAYS);

			Label statusName = new Label("Display Status Bar");
			statusName.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
			Label statusInfo = new Label("Changes the style of the application to use a basic dark theme. Requires a restart to apply.");
			statusInfo.setWrapText(true);

			CheckBox statusOption = new CheckBox("Enabled");
			statusOption.setSelected(configManager.getBooleanValue(ConfigEntry.status));
			statusOption.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
				configManager.setValue(ConfigEntry.status, isSelected);
			});
			VBox.setVgrow(statusOption, Priority.ALWAYS);

			Label toolName = new Label("Toolbar Location");
			toolName.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
			Label toolInfo = new Label("The edge to place the toolbar that holds the file tools. Default is 'LEFT'.");
			toolInfo.setWrapText(true);

			Label toolLabel = new Label("Toolbar Location: ");
			HBox.setHgrow(toolLabel, Priority.SOMETIMES);

			ObservableList<String> toolPos =
					FXCollections.observableArrayList(ConfigManager.toolBarPos);

			ComboBox<String> toolPosOption = new ComboBox<>(toolPos);
			toolPosOption.getSelectionModel().select(configManager.getIntValue(ConfigEntry.toolbarPos));
			HBox.setHgrow(toolPosOption, Priority.ALWAYS);

			Label sizeName = new Label("Icon Size");
			sizeName.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
			Label sizeInfo = new Label("Size (in pixels) of icons in the window. Requires a restart to apply.");
			sizeInfo.setWrapText(true);

			Slider iconSizeSlider = new Slider(16, 96, configManager.getIntValue(ConfigEntry.iconSize));
			iconSizeSlider.setTooltip(new Tooltip("Set icon size for the interface icons."));
			HBox.setHgrow(iconSizeSlider, Priority.SOMETIMES);
			iconSizeSlider.setShowTickLabels(true);
			iconSizeSlider.setShowTickMarks(true);
			iconSizeSlider.setMajorTickUnit(16);
			iconSizeSlider.setMinorTickCount(1);
			iconSizeSlider.setSnapToTicks(true);


			// Advanced Tab

			Label backupName = new Label("Use Backups");
			backupName.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
			Label backupInfo = new Label("Creates a backup of the file before the save process.");
			backupInfo.setWrapText(true);

			CheckBox backupOption = new CheckBox("Enabled");
			backupOption.setSelected(configManager.getBooleanValue(ConfigEntry.fileBackup));
			backupOption.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
				configManager.setValue(ConfigEntry.fileBackup, isSelected);
			});
			VBox.setVgrow(backupOption, Priority.ALWAYS);

			Label debugName = new Label("Print Debug");
			debugName.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
			Label debugInfo = new Label("Prints out debug information in the commandline output. Requires a restart to apply.");
			debugInfo.setWrapText(true);

			CheckBox debugCheck = new CheckBox("Enabled");
			debugCheck.setSelected(configManager.getBooleanValue(ConfigEntry.debug));
			debugCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
				configManager.setValue(ConfigEntry.debug, isSelected);
			});
			VBox.setVgrow(debugCheck, Priority.ALWAYS);

			Button applyButton = new Button("Apply");
			applyButton.setDefaultButton(true);
			applyButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> {
				configManager.setValue(ConfigEntry.toolbarPos, toolPosOption.getValue());
				configManager.setValue(ConfigEntry.channel, channelOption.getValue());
				configManager.setValue(ConfigEntry.iconSize, (int)iconSizeSlider.getValue());
				configManager.write();
				toggleDark(scene);
				Main.setStatusText("Applied new settings.");
			});

			Button okButton = new Button("Ok");
			okButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> {
				configManager.setValue(ConfigEntry.toolbarPos, toolPosOption.getValue());
				configManager.setValue(ConfigEntry.channel, channelOption.getValue());
				configManager.setValue(ConfigEntry.iconSize, (int)iconSizeSlider.getValue());
				configManager.write();
				toggleDark(scene);
				confStage.close();
				Main.setStatusText("Applied new settings.");
			});

			Button cancelButton = new Button("Cancel");
			cancelButton.setCancelButton(true);
			cancelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> {
				confStage.close();
			});

			// Tabbing every option into their own VBox so we can have them split off in their own containers.
			// This makes it easier to add to tabs because it's just a simple declare. Also good for design.
			VBox vGeneral = new VBox(channelName, channelInfo, channelOption);
			ScrollPane general = new ScrollPane(vGeneral);
			general.setFitToWidth(true);
			Tab generalTab = new Tab("General", general);
			VBox.setVgrow(general, Priority.ALWAYS);
			vGeneral.setPadding(new Insets(10, 10, 10, 10));
			vGeneral.setSpacing(10);

			VBox vSkin = new VBox(darkName, darkInfo, darkOption, statusName, statusInfo, statusOption,
					toolName, toolInfo, toolPosOption, sizeName, sizeInfo, iconSizeSlider);
			ScrollPane skin = new ScrollPane(vSkin);
			skin.setFitToWidth(true);
			Tab skinTab = new Tab("Appearance", skin);
			VBox.setVgrow(skin, Priority.ALWAYS);
			vSkin.setPadding(new Insets(10, 10, 10, 10));
			vSkin.setSpacing(10);

			VBox vAdvance = new VBox(backupName, backupInfo, backupOption, debugName, debugInfo, debugCheck);
			ScrollPane advance = new ScrollPane(vAdvance);
			advance.setFitToWidth(true);
			Tab advanceTab = new Tab("Advanced", advance);
			VBox.setVgrow(advance, Priority.ALWAYS);
			vAdvance.setPadding(new Insets(10, 10, 10, 10));
			vAdvance.setSpacing(10);

			TabPane prefPane = new TabPane(generalTab, skinTab, advanceTab);
			VBox.setVgrow(prefPane, Priority.ALWAYS);
			prefPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

			general.maxWidthProperty().bind(prefPane.widthProperty());
			skin.maxWidthProperty().bind(prefPane.widthProperty());
			advance.maxWidthProperty().bind(prefPane.widthProperty());

			ButtonBar bBar = new ButtonBar();
			bBar.getButtons().addAll(applyButton, okButton, cancelButton);
			bBar.setPadding(new Insets(10, 10, 10, 10));

			confStage.setMinWidth(350);
			confStage.setMinHeight(250);

			confStage.setScene(new Scene(new VBox(prefPane, bBar), 350, 350));
			confStage.show();
		});

		quitBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
			e.consume();
			quit();
		});

		VBox.setVgrow(tabPane, Priority.ALWAYS);

		toggleDark(scene);

		Console.print("Scene Prepared", 1, ConsoleType.INFO);
		mainStage.setScene(scene);
		mainStage.show();
		Console.print("Displaying Main Window", 1, ConsoleType.INFO);

		// Going through files set up on command-line arguments.
		Console.print("Iterating through argument files", 1, ConsoleType.INFO);
		if (startFiles != null && startFiles.size() > 0) {
			try {
				for (File start : startFiles) {
					if (start.exists()) openFile(start);
					else Console.print("Unable to open file: File Not Found:\n'" + start.getPath() + "'", ConsoleType.ERROR);
				}
			} catch (Exception e) {
				Console.print("An unknown error occurred attempting to open argument files.", 1, ConsoleType.ERROR);
			}
		}
	}

	public static void setStatusText(String message) {
		statusPane.getChildren().set(0, new Text(message));

		// 2.5 Seconds later, the text will disappear. Makes it look neater and also makes sure you
		// aren't frozen I guess.
		Task<Void> sleeper = new Task<Void>() {
			@Override
			protected Void call() {
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					statusPane = new VBox(new Text());
				}
				return null;
			}
		};

		sleeper.setOnSucceeded(event -> statusPane.getChildren().set(0, new Text()));
		new Thread(sleeper).start();
	}

	private void toggleDark(Scene curScene) {
		if (configManager.getBooleanValue(ConfigEntry.darkMode)) curScene.getStylesheets().add("dark.css");
		else curScene.getStylesheets().remove("dark.css");
	}

	public Tab getStartPage() {
		VBox mainBox = new VBox();

		// Styling fancy texts.
		ImageView logoView = new ImageView(AppIcon.logo);
		Text nameTxt = new Text("Worlds Organizer v" + Console.getVersion());
		nameTxt.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
		Text buildTxt = new Text("Build Date: " + Console.getDate());
		buildTxt.setFont(Font.font("Verdana", FontWeight.NORMAL, FontPosture.REGULAR, 12));
		buildTxt.setFill(Color.GRAY);
		Text devTxt = new Text("Developed by Worlio LLC");
		devTxt.setFont(Font.font("Verdana", FontWeight.MEDIUM, FontPosture.REGULAR, 12));

		logoView.setPreserveRatio(true);
		logoView.fitHeightProperty().bind(mainBox.heightProperty().multiply(0.5));
		mainBox.setAlignment(Pos.CENTER);

		Button updateButton = new Button("Check for Updates");
		updateButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> {
			boolean isUpdatable;
			try {
				// Simply restarting the manager so it gets everything fresh.
				// Simple yet effective at what we want, without issues.
				verMan = new VersionManager();
				isUpdatable = verMan.hasUpdate();
				if (isUpdatable) {
					Console.print("Update detected! Showing dialog.", ConsoleType.INFO);
					verMan.pushUpdate();
				} else Console.print("No updates available.", ConsoleType.INFO);
			} catch (IOException e) {
				Console.print("Could not check for updates!", ConsoleType.ERROR);
				isUpdatable = false;
			}
			if (!isUpdatable) Dialog.showError("Update Check", "No new updates are available.");
		});

		Button changelogButton = new Button("What's new?");
		changelogButton.addEventFilter(MouseEvent.MOUSE_CLICKED, a -> {
			Stage changelogStage = new Stage();
			changelogStage.initOwner(mainStage);
			changelogStage.setTitle("What's new?");

			VBox changesBox = new VBox();

			// Reading the json file remotely, and then looking through every single value in the list.
			// It's simple text, so it makes it easy to parse and style.

			for (Map.Entry<?, ?> vC : ((Map<?, ?>)((Map<?, ?>)verMan.json.get("versions")).get(configManager.getStringValue(ConfigEntry.channel))).entrySet()) {

				// UPDATES UPDATES UPDATES
				if (new Version((String)vC.getKey()).equals(new Version(Console.getVersion()))) {
					Text channel = new Text("Current Version");
					channel.setFont(Font.font("Verdana", FontWeight.LIGHT, FontPosture.ITALIC, 12));
					changesBox.getChildren().add(channel);
				}

				Text version = new Text((String)vC.getKey());
				version.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
				changesBox.getChildren().add(version);
				for (String line : verMan.getChangelog(new Version((String)vC.getKey()))) {
					Text lineTxt = new Text(" - " + line);
					lineTxt.wrappingWidthProperty().bind(changesBox.prefWidthProperty().multiply(0.95));
					changesBox.getChildren().add(lineTxt);
				}
				changesBox.getChildren().add(new Separator());
			}
			changesBox.setPadding(new Insets(10, 10, 10, 10));

			// Organize it nicely within a small ScrollPane.
			// It took forever to get the text to wrap so this works.
			ScrollPane changesPane = new ScrollPane(changesBox);
			VBox.setVgrow(changesPane, Priority.ALWAYS);
			changesPane.prefWidthProperty().bind(changelogStage.widthProperty());
			changesPane.setFitToWidth(true);

			changesBox.prefWidthProperty().bind(changesPane.widthProperty());

			Button cancelButton = new Button("Close");
			cancelButton.setCancelButton(true);
			cancelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> changelogStage.close());

			ButtonBar bBar = new ButtonBar();
			bBar.getButtons().addAll(cancelButton);
			bBar.setPadding(new Insets(10, 10, 10, 10));

			changelogStage.setMinWidth(450);
			changelogStage.setMinHeight(400);

			changelogStage.setScene(new Scene(new VBox(changesPane, bBar), 450, 400));
			changelogStage.show();
		});

		HBox bBar = new HBox(changelogButton, updateButton);
		bBar.setAlignment(Pos.CENTER);
		bBar.setSpacing(10);
		bBar.setPadding(new Insets(10, 10, 10, 10));

		mainBox.getChildren().addAll(logoView, nameTxt, buildTxt, devTxt, bBar);
		return new Tab("Start Page", mainBox);
	}

	void newFile() {
		// Essentially a bare bones way of opening a file without the file.

		WorldsTab tableObj = new WorldsTab();
		WorldsType newType = Dialog.newFileList();
		if (newType != WorldsType.NULL) {
			Console.print("Creating a new tab", 1, ConsoleType.INFO);

			Tab tab = tableObj.getTab(newType);

			tabPane.getTabs().add(tab);
			tabs.add(tableObj);

			Main.setStatusText("Opened new tab.");

			tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
		}
	}

	void openFile(File file) {
		File openedFile;
		if (file == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open File");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("All Files", "*"),
					new FileChooser.ExtensionFilter("All Supported Formats", "*.avatars", "*.worldsmarks", "*.organizer-bkup"),
					new FileChooser.ExtensionFilter("Gamma Avatars (*.avatars)", "*.avatars"),
					new FileChooser.ExtensionFilter("Gamma WorldsMarks (*.worldsmarks)", "*.worldsmarks")
			);
			openedFile = fileChooser.showOpenDialog(mainStage);
		} else {
			openedFile = file;
		}

		if (openedFile != null) {
			WorldsTab tableObj = new WorldsTab();
			Tab tab = tableObj.getTab(openedFile);

			if (tab != null) {
				Main.setStatusText("Opened file '" + openedFile.getAbsolutePath() + "'.");

				tabPane.getTabs().add(tab);
				tabs.add(tableObj);

				tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
			} else {
				Console.print("InvalidPersisterFile encountered! File is not a supported format.", ConsoleType.ERROR);
				Dialog.showError("Invalid File!", "File selected is not a valid Persister format.");
			}
		}
	}

	void saveFile(WorldsTab tab) {
		saveFile(tab, null);
	}

	void saveFile(WorldsTab tab, File file) {
		File thisFile;
		if (file == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save As File");
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Gamma Avatars (*.avatars)", "*.avatars"),
					new FileChooser.ExtensionFilter("Gamma WorldsMarks (*.worldsmarks)", "*.worldsmarks")
			);
			fileChooser.setInitialFileName("gamma");
			thisFile = fileChooser.showSaveDialog(mainStage);

			if (thisFile != null) {
				switch (fileChooser.getSelectedExtensionFilter().getExtensions().get(0)) {
					default:
					case "*.avatars":
						tab.worldList.classType = WorldsType.AVATAR;
						break;
					case "*.worldsmarks":
						tab.worldList.classType = WorldsType.WORLDSMARK;
						break;
				}
			}
		} else {
			thisFile = file;
		}

		if (thisFile != null) {
			try {
				Saver saver = new Saver(thisFile);
				saver.save(tab.worldList);
				tab.setSaved(true);
				tab.update(thisFile);
				Main.setStatusText("Saved file to '" + thisFile.getAbsolutePath() + "'.");
			} catch (IOException e) {
				Console.print("Unable to save file: " + thisFile.getAbsolutePath(), ConsoleType.ERROR);
				Dialog.showException(e);
			}
		}
	}

	void quit() {
		boolean askForSure = false;
		for (WorldsTab t : tabs) {
			if (!t.getSaved()) {
				askForSure = true;
				break;
			}
		}

		if (askForSure) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.getDialogPane().setMinSize(200,200);
			alert.setTitle("Quit");
			alert.setHeaderText("Are you sure you want to quit?");
			alert.setContentText("You have unsaved changes. Quitting now will lose your progress.");

			ButtonType dontSaveButton = new ButtonType("Discard Changes");
			ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(dontSaveButton, buttonTypeCancel);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == dontSaveButton) {
				mainStage.close();
			} else {
				alert.close();
			}
		} else {
			mainStage.close();
		}
	}
}
