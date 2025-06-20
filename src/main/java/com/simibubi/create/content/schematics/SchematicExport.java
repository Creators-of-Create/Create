package com.simibubi.create.content.schematics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.loading.FMLPaths;

public class SchematicExport {
	public static final Path SCHEMATICS = FMLPaths.GAMEDIR.get().resolve("schematics");

	/*
	 * java provides no way to safely creating file in directory
	 * Paths.resolve() doesn't work if user input some path like "/" or ".."
	 * A check before resolve() is needed
	 */
	private static boolean sanitizeFilename(String fileName) {
		if (".".equals(fileName)) return false;
		if ("..".equals(fileName)) return false;
		return fileName.indexOf(File.separatorChar) == -1;
	}

	/**
	 * Save a schematic to a file from a world.
	 * @param dir       the directory the schematic will be created in
	 * @param fileName  the ideal name of the schematic, may not be the name of the created file
	 * @param overwrite whether overwriting an existing schematic is allowed
	 * @param level     the level where the schematic structure is placed
	 * @param first     the first corner of the schematic area
	 * @param second    the second corner of the schematic area
	 * @return a SchematicExportResult
	 * @throws IOException if any i/o error occurs, for example, permission denied
	 */
	@Nullable
	public static SchematicExportResult saveSchematic(Path dir, String fileName, boolean overwrite, Level level, BlockPos first, BlockPos second) throws IOException {
		BoundingBox bb = BoundingBox.fromCorners(first, second);
		BlockPos origin = new BlockPos(bb.minX(), bb.minY(), bb.minZ());
		BlockPos bounds = new BlockPos(bb.getXSpan(), bb.getYSpan(), bb.getZSpan());

		StructureTemplate structure = new StructureTemplate();
		structure.fillFromWorld(level, origin, bounds, true, Blocks.AIR);
		CompoundTag data = structure.save(new CompoundTag());
		SchematicAndQuillItem.replaceStructureVoidWithAir(data);
		SchematicAndQuillItem.clampGlueBoxes(level, new AABB(origin, origin.offset(bounds)), data);

		if (fileName.isEmpty())
			fileName = CreateLang.translateDirect("schematicAndQuill.fallbackName").getString();
		if (!sanitizeFilename(fileName))
			throw new java.nio.file.AccessDeniedException(fileName, null, "illegal filename");

		if (!fileName.endsWith(".nbt"))
			fileName += ".nbt";
		Path file = dir.resolve(fileName).toAbsolutePath();

		Files.createDirectories(dir);
		OpenOption[] writeOption = new OpenOption[]{StandardOpenOption.CREATE_NEW};
		boolean overwritten = false;
		if (overwrite) {
			overwritten = Files.exists(file);
			writeOption = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
		}
		try (OutputStream out = Files.newOutputStream(file, writeOption)) {
			NbtIo.writeCompressed(data, out);
		}
		return new SchematicExportResult(file, dir, fileName, overwritten, origin, bounds);
	}

	public record SchematicExportResult(Path file, Path dir, String fileName, boolean overwritten, BlockPos origin, BlockPos bounds) {}
}
