package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class DisplayLinkPeripheral extends SyncedPeripheral<DisplayLinkBlockEntity> {

	public static final String TAG_KEY = "ComputerSourceList";
	private final AtomicInteger cursorX = new AtomicInteger();
	private final AtomicInteger cursorY = new AtomicInteger();

	public DisplayLinkPeripheral(DisplayLinkBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction
	public final void setCursorPos(int x, int y) {
		cursorX.set(x - 1);
		cursorY.set(y - 1);
	}

	@LuaFunction
	public final Object[] getCursorPos() {
		return new Object[] {cursorX.get() + 1, cursorY.get() + 1};
	}

	@LuaFunction(mainThread = true)
	public final Object[] getSize() {
		blockEntity.updateGatheredData();
		DisplayTargetStats stats = blockEntity.activeTarget.provideStats(new DisplayLinkContext(blockEntity.getLevel(), blockEntity));
		return new Object[]{stats.maxRows(), stats.maxColumns()};
	}

	@LuaFunction
	public final boolean isColor() {
		return false;
	}

	@LuaFunction
	public final boolean isColour() {
		return false;
	}

	@LuaFunction
	public final void write(String text) {
		ListTag tag = blockEntity.getSourceConfig().getList(TAG_KEY, Tag.TAG_STRING);

		int x = cursorX.get();
		int y = cursorY.get();

		for (int i = tag.size(); i <= y; i++) {
			tag.add(StringTag.valueOf(""));
		}

		StringBuilder builder = new StringBuilder(tag.getString(y));

		builder.append(" ".repeat(Math.max(0, x - builder.length())));
		builder.replace(x, x + text.length(), text);

		tag.set(y, StringTag.valueOf(builder.toString()));

		synchronized (blockEntity) {
			blockEntity.getSourceConfig().put(TAG_KEY, tag);
		}

		cursorX.set(x + text.length());
	}

	@LuaFunction
	public final void clearLine() {
		ListTag tag = blockEntity.getSourceConfig().getList(TAG_KEY, Tag.TAG_STRING);

		if (tag.size() > cursorY.get())
			tag.set(cursorY.get(), StringTag.valueOf(""));

		synchronized (blockEntity) {
			blockEntity.getSourceConfig().put(TAG_KEY, tag);
		}
	}

	@LuaFunction
	public final void clear() {
		synchronized (blockEntity) {
			blockEntity.getSourceConfig().put(TAG_KEY, new ListTag());
		}
	}

	@LuaFunction(mainThread = true)
	public final void update() {
		blockEntity.tickSource();
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_DisplayLink";
	}

}
