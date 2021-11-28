package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import com.simibubi.create.lib.utility.NBT;

public class NixieTubeTileEntity extends SmartTileEntity {

	private static final Couple<String> EMPTY = Couple.create("", "");

	private boolean hasCustomText;
	private int redstoneStrength;
	private JsonElement rawCustomText;
	private int customTextIndex;
	private Component parsedCustomText;
	private Couple<String> displayedStrings;

	public NixieTubeTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		hasCustomText = false;
		redstoneStrength = 0;
	}

	@Override
	public void tick() {
		super.tick();

		// Dynamic text components have to be ticked manually and re-sent to the client
		if (level instanceof ServerLevel && hasCustomText) {
			Couple<String> currentStrings = displayedStrings;
			parsedCustomText = parseCustomText();
			updateDisplayedStrings();
			if (currentStrings == null || !currentStrings.equals(displayedStrings))
				sendData();
		}
	}

	@Override
	public void initialize() {
		if (level.isClientSide)
			updateDisplayedStrings();
	}

	//

	public boolean reactsToRedstone() {
		return !hasCustomText;
	}

	public Couple<String> getDisplayedStrings() {
		if (displayedStrings == null)
			return EMPTY;
		return displayedStrings;
	}

	public void updateRedstoneStrength(int signalStrength) {
		clearCustomText();
		redstoneStrength = signalStrength;
		notifyUpdate();
	}

	public void displayCustomNameOf(ItemStack stack, int nixiePositionInRow) {
		CompoundTag compoundnbt = stack.getTagElement("display");
		if (compoundnbt != null && compoundnbt.contains("Name", Tag.TAG_STRING)) {
			hasCustomText = true;
			rawCustomText = getJsonFromString(compoundnbt.getString("Name"));
			customTextIndex = nixiePositionInRow;
			parsedCustomText = parseCustomText();
			notifyUpdate();
		}
	}

	public void updateDisplayedStrings() {
		if (!hasCustomText) {
			displayedStrings = Couple.create(redstoneStrength < 10 ? "0" : "1", String.valueOf(redstoneStrength % 10));
		} else {
			String fullText = parsedCustomText.getString();
			int index = customTextIndex * 2;
			displayedStrings = Couple.create(charOrEmpty(fullText, index), charOrEmpty(fullText, index + 1));
		}
	}

	public void clearCustomText() {
		hasCustomText = false;
		rawCustomText = null;
		customTextIndex = 0;
		parsedCustomText = null;
	}

	//

	@Override
	protected void fromTag(CompoundTag nbt, boolean clientPacket) {
		super.fromTag(nbt, clientPacket);

		if (nbt.contains("RawCustomText", Tag.TAG_STRING)) {
			rawCustomText = getJsonFromString(nbt.getString("RawCustomText"));
			// Check if string forms valid JSON
			if (rawCustomText != null && !rawCustomText.isJsonNull()) {
				Component deserializedComponent = parseCustomText();
				// Check if JSON forms valid component
				if (deserializedComponent != null) {
					try {
						// Try to deserialize previously parsed component
						parsedCustomText = Component.Serializer.fromJson(nbt.getString("CustomText"));
					} catch (JsonParseException e) {
						//
					}
					if (parsedCustomText == null) {
						// Use test component to ensure field isn't null
						parsedCustomText = deserializedComponent;
					}
					hasCustomText = true;
					customTextIndex = nbt.getInt("CustomTextIndex");
				}
			}
		}

		if (!hasCustomText) {
			clearCustomText();
			redstoneStrength = nbt.getInt("RedstoneStrength");
		}

		if (clientPacket)
			updateDisplayedStrings();
	}

	@Override
	protected void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);

		if (hasCustomText) {
			nbt.putString("RawCustomText", rawCustomText.toString());
			nbt.putInt("CustomTextIndex", customTextIndex);
			nbt.putString("CustomText", Component.Serializer.toJson(parsedCustomText));
		} else {
			nbt.putInt("RedstoneStrength", redstoneStrength);
		}
	}

	private JsonElement getJsonFromString(String string) {
		try {
			return new JsonParser().parse(string);
		} catch (JsonParseException e) {
			return null;
		}
	}

	private String charOrEmpty(String string, int index) {
		return string.length() <= index ? " " : string.substring(index, index + 1);
	}

	protected Component parseCustomText() {
		try {
			return parseDynamicComponent(Component.Serializer.fromJson(rawCustomText));
		} catch (JsonParseException e) {
			return null;
		}
	}

	protected Component parseDynamicComponent(Component customText) {
		if (level instanceof ServerLevel) {
			try {
				return ComponentUtils.updateForEntity(getCommandSource(null), customText, null, 0);
			} catch (CommandSyntaxException e) {
				//
			}
		}
		return customText;
	}

	// From SignTileEntity
	public CommandSourceStack getCommandSource(@Nullable ServerPlayer p_195539_1_) {
		String s = p_195539_1_ == null ? "Nixie Tube" : p_195539_1_.getName().getString();
		Component itextcomponent = (Component)(p_195539_1_ == null ? new TextComponent("Nixie Tube") : p_195539_1_.getDisplayName());
		return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(this.worldPosition), Vec2.ZERO, (ServerLevel)this.level, 2, s, itextcomponent, this.level.getServer(), p_195539_1_);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
