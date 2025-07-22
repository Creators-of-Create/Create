package com.simibubi.create.foundation.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.simibubi.create.Create;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;

public class SavedDataUtil {
	public static <T extends SavedData> void saveWithDatOld(T savedData, File file) {
		if (savedData.isDirty()) {
			CompoundTag compoundtag = new CompoundTag();
			compoundtag.put("data", savedData.save(new CompoundTag()));
			NbtUtils.addCurrentDataVersion(compoundtag);

			String savedDataName = file.getName().split("\\.")[0];

			try {
				File temp = File.createTempFile(savedDataName, ".dat", file.getParentFile());
				NbtIo.writeCompressed(compoundtag, temp);
				File oldFile = Paths.get(file.getParent(), savedDataName + ".dat_old").toFile();
				Util.safeReplaceFile(file, temp, oldFile);
			} catch (IOException ioexception) {
				Create.LOGGER.error("Could not save data {}", savedData, ioexception);
			}

			savedData.setDirty(false);
		}
	}
}
