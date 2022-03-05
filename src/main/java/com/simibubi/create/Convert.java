package com.simibubi.create;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;

public class Convert {
	public static void main(String[] args) {
		File root = Paths.get("E:\\JetBrains\\IDEA\\projects\\Create-Refabricated\\src\\main\\java\\com\\simibubi\\create").toFile();
		iterateDir(root);
	}

	public static void iterateDir(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					iterateDir(f);
				} else {
					try (FileInputStream in = new FileInputStream(f)) {
						String data = new String(in.readAllBytes());
						data = data.replace("import io.github.fabricators_of_create.porting_lib", "import io.github.fabricators_of_create.porting_lib");
						in.close();
						FileOutputStream out = new FileOutputStream(f);
						out.write(data.getBytes());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
}
