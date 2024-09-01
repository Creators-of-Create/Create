package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlockEntity;
import com.simibubi.create.foundation.utility.Components;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.LuaValues;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class NixieTubePeripheral extends SyncedPeripheral<NixieTubeBlockEntity> {

	private static final String EMPTY_COMPONENT_JSON = Component.Serializer.toJson(Components.literal(""));

	public NixieTubePeripheral(NixieTubeBlockEntity blockEntity) {
		super(blockEntity);
	}

	@Override
	protected void onFirstAttach() {
		// When first attaching to a computer, clear out the entire nixie tube row.
		super.onFirstAttach();
		Level world = blockEntity.getLevel();
		if (world == null)
			return;
		NixieTubeBlock.walkNixies(world, blockEntity.getBlockPos(), true,
				(currentPos, rowPosition) -> {
					if (world.getBlockEntity(currentPos) instanceof NixieTubeBlockEntity ntbe)
						ntbe.displayCustomText(EMPTY_COMPONENT_JSON, rowPosition);
				});
	}

	@Override
	protected void onLastDetach() {
		// When detaching from the last computer, reset the entire nixie tube row back to redstone display,
		// except if it's still being controlled from some other tube. onLastDetach runs after the
		// hasAttachedComputer flag is reset, so we can use walkNixies()'s computer control rejection for that.
		super.onLastDetach();
		Level world = blockEntity.getLevel();
		if (world == null)
			return;
		// Check if the nixie tube block is still there; if it isn't then the nixie was removed/destroyed
		// and the row reset is handled in NixieTubeBlock::remove.
		BlockState state = world.getBlockState(blockEntity.getBlockPos());
		if (!(state.getBlock() instanceof NixieTubeBlock))
			return;
		NixieTubeBlock.walkNixies(world, blockEntity.getBlockPos(), false,
				(currentPos, rowPosition) -> {
					if (world.getBlockEntity(currentPos) instanceof NixieTubeBlockEntity ntbe) {
						NixieTubeBlock.updateDisplayedRedstoneValue(ntbe, true);
					}
				});
	}

	@LuaFunction(mainThread = true)
	public void setText(IArguments arguments) throws LuaException {
		Level world = blockEntity.getLevel();
		if (world == null)
			return;
		blockEntity.computerSignal = null;

		String tagElement = Component.Serializer.toJson(Components.literal(arguments.getString(0)));

		@Nullable String colour = arguments.optString(1, null);
		BlockState state = null;
		DyeColor dye = null;
		if (colour != null) {
			state = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
			dye = LuaValues.checkEnum(1, DyeColor.class, colour.equals("grey") ? "gray" : colour);
		}

		changeTextNixie(tagElement, state, dye);
	}

	@LuaFunction(mainThread = true)
	public void setTextColour(String colour) throws LuaException {
		Level world = blockEntity.getLevel();
		if (world == null)
			return;
		BlockState state = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
		DyeColor dye = LuaValues.checkEnum(1, DyeColor.class, colour.equals("grey") ? "gray" : colour);
		changeTextNixie(null, state, dye);
	}

	@LuaFunction(mainThread = true)
	public void setTextColor(String color) throws LuaException {
		setTextColour(color);
	}

	private void changeTextNixie(@Nullable String tagElement, @Nullable BlockState state, @Nullable DyeColor dye) {
		Level world = blockEntity.getLevel();
		if (world == null)
			return;
		NixieTubeBlock.walkNixies(world, blockEntity.getBlockPos(), true, (currentPos, rowPosition) -> {
			if (tagElement != null)
				((NixieTubeBlock) blockEntity.getBlockState().getBlock()).withBlockEntityDo(
						world, currentPos, be -> be.displayCustomText(tagElement, rowPosition));
			if (state != null && dye != null)
				world.setBlockAndUpdate(currentPos, NixieTubeBlock.withColor(state, dye));
		});
	}

	@LuaFunction(mainThread = true)
	public void setSignal(IArguments arguments) throws LuaException {
		if (arguments.optTable(0).isPresent())
			setSignal(signal().first, arguments.getTable(0));
		if (arguments.optTable(1).isPresent())
			setSignal(signal().second, arguments.getTable(1));
	}

	private void setSignal(NixieTubeBlockEntity.ComputerSignal.TubeDisplay display, @NotNull Map<?, ?> attrs)
			throws LuaException {
		if (attrs.containsKey("r"))
			display.r = constrainByte("r", 0, 255, attrs.get("r"));
		if (attrs.containsKey("g"))
			display.g = constrainByte("g", 0, 255, attrs.get("g"));
		if (attrs.containsKey("b"))
			display.b = constrainByte("r", 0, 255, attrs.get("b"));
		if (attrs.containsKey("glowWidth"))
			display.glowWidth = constrainByte("glowWidth", 1, 4, attrs.get("glowWidth"));
		if (attrs.containsKey("glowHeight"))
			display.glowHeight = constrainByte("glowHeight", 1, 4, attrs.get("glowHeight"));
		if (attrs.containsKey("blinkPeriod"))
			display.blinkPeriod = constrainByte("blinkPeriod", 0, 255, attrs.get("blinkPeriod"));
		if (attrs.containsKey("blinkOffTime"))
			display.blinkOffTime = constrainByte("blinkOffTime", 0, 255, attrs.get("blinkOffTime"));
		if (display.r == 0 && display.g == 0 && display.b == 0) {
			display.blinkPeriod = 0;
			display.blinkOffTime = 0;
		} else if (display.blinkPeriod == 0) {
			display.blinkPeriod = 1;
			display.blinkOffTime = 0;
		}
		blockEntity.notifyUpdate();
	}

	private byte constrainByte(String name, int min, int max, Object rawValue) throws LuaException {
		if (!(rawValue instanceof Number))
			throw LuaValues.badField(name, "number", LuaValues.getType(rawValue));
		int value = ((Number) rawValue).intValue();
		if (value < min || value > max)
			throw new LuaException("field " + name + " must be in range " + min + "-" + max);
		return (byte) value;
	}

	private NixieTubeBlockEntity.ComputerSignal signal() {
		if (blockEntity.computerSignal == null)
			blockEntity.computerSignal = new NixieTubeBlockEntity.ComputerSignal();
		return blockEntity.computerSignal;
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_NixieTube";
	}

}
