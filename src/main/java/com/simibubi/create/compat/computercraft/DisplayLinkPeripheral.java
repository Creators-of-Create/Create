package com.simibubi.create.compat.computercraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.block.display.DisplayLinkTileEntity;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class DisplayLinkPeripheral implements IPeripheral {

	public static final String TAG_KEY = "ComputerSourceList";

	private final DisplayLinkTileEntity tile;

	public DisplayLinkPeripheral(DisplayLinkTileEntity tile) {
		this.tile = tile;
	}

	@LuaFunction
	public void writeLine(String line) {
		ListTag tag = this.tile.getSourceConfig().getList(TAG_KEY, Tag.TAG_STRING);
		tag.add(StringTag.valueOf(line));

		this.tile.getSourceConfig().put(TAG_KEY, tag);
	}

	@LuaFunction
	public void setLine(int lineNumber, String line) {
		ListTag tag = this.tile.getSourceConfig().getList(TAG_KEY, Tag.TAG_STRING);
		int size = tag.size();

		if (lineNumber < size) {
			tag.set(lineNumber, StringTag.valueOf(line));

		} else {
			for (int i = 0; i < lineNumber - size; i++) {
				tag.add(StringTag.valueOf(""));
			}

			tag.add(StringTag.valueOf(line));
		}

		this.tile.getSourceConfig().put(TAG_KEY, tag);
	}

	@LuaFunction
	public void writeLines(IArguments arguments) throws LuaException {
		ListTag tag = this.tile.getSourceConfig().getList(TAG_KEY, Tag.TAG_STRING);
		List<String> lines = getLinesFromArguments(arguments, 0);

		for (String line : lines) {
			tag.add(StringTag.valueOf(line));
		}

		this.tile.getSourceConfig().put(TAG_KEY, tag);
	}

	@LuaFunction
	public void setLines(IArguments arguments) throws LuaException {
		ListTag tag = this.tile.getSourceConfig().getList(TAG_KEY, Tag.TAG_STRING);
		List<String> lines = getLinesFromArguments(arguments, 1);

		int size = tag.size();
		int lineNumber = arguments.getInt(0);
		int i = 0;

		for (int j = lineNumber; j < Math.min(size, lines.size()); j++) {
			tag.set(j, StringTag.valueOf(lines.get(i)));
			i++;
		}

		for (int j = 0; j < lineNumber - size; j++) {
			tag.add(StringTag.valueOf(""));
		}

		for (int j = i; j < lines.size(); j++) {
			tag.add(StringTag.valueOf(lines.get(i)));
			i++;
		}

		this.tile.getSourceConfig().put(TAG_KEY, tag);
	}

	@LuaFunction
	public void clear() {
		this.tile.getSourceConfig().put(TAG_KEY, new ListTag());
	}

	@LuaFunction(mainThread = true)
	public void update() {
		this.tile.tickSource();
	}

	private List<String> getLinesFromArguments(IArguments arguments, int offset) throws LuaException {
		List<String> lines = new ArrayList<>();

		if (arguments.count() > offset + 1) {
			for (int i = offset; i < arguments.count(); i++) {
				lines.add(arguments.getString(i));
			}

		} else {
			Map<?, ?> map = arguments.getTable(offset);

			for (Object line : map.values()) {
				lines.add(line.toString());
			}
		}

		return lines;
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_DisplayLink";
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return this == other;
	}

}
