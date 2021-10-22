package org.worlio.WorldsOrganizer;

import java.io.*;

public class Restorer {

	DataInputStream dis;

	File file;
	public WorldListObject listObj;

	private int oID;

	Restorer(String path) {
		new Restorer(new File(path));
	}

	Restorer(File file) {
		Console.print("Initialized Restorer", 1, ConsoleType.INFO);
		this.file = file;
		try {
			FileInputStream fis = new FileInputStream(file);
			dis = new DataInputStream(fis);
			Console.print("Restorer file set as '" + file.getAbsolutePath() + "'", 1, ConsoleType.INFO);
		} catch (IOException e) {
			Console.print("IOException reading file: '" + file.getAbsolutePath() + "'", ConsoleType.ERROR);
		}
	}

	WorldListObject read() throws IOException {
		if (dis == null) {
			throw new InvalidPersisterException();
		}

		try {
			if (!readString().equals("PERSISTER Worlds, Inc.")) { // Persister Header
				throw new InvalidPersisterException();
			} else {
				int pVersion = readInt(); // Persister Version
				Console.print("Persister Version detected: " + pVersion, 1, ConsoleType.INFO);
				if (pVersion != 7) Console.print("Version not supported! Issues may occur!", ConsoleType.WARNING);

				int count = readInt(); // Vector Count

				readInt(); // Class ID
				return readVector(count);
			}
		} catch (NullPointerException e) {
			throw new InvalidPersisterException();
		}
	}

	private WorldListObject readVector(int count) throws IOException {
		Console.print("Starting read of '" + file.getPath() + "'", 1, ConsoleType.INFO);

		listObj = new WorldListObject();

		oID = readInt(); // Object ID
		String typeText = readString(); // Class Name
		listObj.classType = WorldsType.valueOfClass(typeText);
		if (listObj.classType != null) {
			Console.print("ClassName read as '" + typeText + "'. Setting type to '" + WorldsType.valueOfClass(typeText) + "'.", 2, ConsoleType.DEBUG);

			for (int i = 0; i < count; i++) {
				if (i > 0) readInt();
				readInt(); // Version
				WorldList newData = null;
				if (listObj.classType == WorldsType.AVATAR) newData = new AvatarObject(readString(), readString());
				else if (listObj.classType == WorldsType.WORLDSMARK) newData = new MarkObject(readString(), readString());
				assert newData != null;
				listObj.add(newData);
				Console.print("Created WorldList item: { name: '" + newData.getName() + "', value: '" + newData.getValue() + "' }", 2, ConsoleType.DEBUG);
				Console.print("Added new WorldList item to WorldListObject", 1, ConsoleType.DEBUG);
			}
			assert readString().equals("END PERSISTER");
		}

		return listObj;
	}

	String readString() throws IOException {
		if (readBoolean()) {
			return null;
		} else {
			return dis.readUTF();
		}
	}

	int readInt() throws IOException {
		return dis.readInt();
	}

	byte readByte() throws IOException {
		return dis.readByte();
	}

	boolean readBoolean() throws IOException {
		return dis.readBoolean();
	}

}
