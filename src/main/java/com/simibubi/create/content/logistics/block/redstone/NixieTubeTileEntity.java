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

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class NixieTubeTileEntity extends SmartTileEntity {

	private static final Couple<String> EMPTY = Couple.create("", "");

	private boolean hasCustomText;
	private int redstoneStrength;
	private JsonElement rawCustomText;
	private int customTextIndex;
	private ITextComponent parsedCustomText;
	private Couple<String> displayedStrings;

	public NixieTubeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		hasCustomText = false;
		redstoneStrength = 0;
	}

	@Override
	public void tick() {
		super.tick();

		// Dynamic text components have to be ticked manually and re-sent to the client
		if (world instanceof ServerWorld && hasCustomText) {
			Couple<String> currentStrings = displayedStrings;
			parsedCustomText = parseCustomText();
			updateDisplayedStrings();
			if (currentStrings == null || !currentStrings.equals(displayedStrings))
				sendData();
		}
	}

	@Override
	public void initialize() {
		if (world.isRemote)
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
		CompoundNBT compoundnbt = stack.getChildTag("display");
		if (compoundnbt != null && compoundnbt.contains("Name", NBT.TAG_STRING)) {
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
	protected void fromTag(BlockState state, CompoundNBT nbt, boolean clientPacket) {
		super.fromTag(state, nbt, clientPacket);

		if (nbt.contains("RawCustomText", NBT.TAG_STRING)) {
			rawCustomText = getJsonFromString(nbt.getString("RawCustomText"));
			// Check if string forms valid JSON
			if (rawCustomText != null && !rawCustomText.isJsonNull()) {
				ITextComponent deserializedComponent = parseCustomText();
				// Check if JSON forms valid component
				if (deserializedComponent != null) {
					try {
						// Try to deserialize previously parsed component
						parsedCustomText = ITextComponent.Serializer.fromJson(nbt.getString("CustomText"));
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
	protected void write(CompoundNBT nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);

		if (hasCustomText) {
			nbt.putString("RawCustomText", rawCustomText.toString());
			nbt.putInt("CustomTextIndex", customTextIndex);
			nbt.putString("CustomText", ITextComponent.Serializer.toJson(parsedCustomText));
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

	protected ITextComponent parseCustomText() {
		try {
			return parseDynamicComponent(ITextComponent.Serializer.fromJson(rawCustomText));
		} catch (JsonParseException e) {
			return null;
		}
	}

	protected ITextComponent parseDynamicComponent(ITextComponent customText) {
		if (world instanceof ServerWorld) {
			try {
				return TextComponentUtils.parse(getCommandSource(null), customText, null, 0);
			} catch (CommandSyntaxException e) {
				//
			}
		}
		return customText;
	}

	// From SignTileEntity
	public CommandSource getCommandSource(@Nullable ServerPlayerEntity p_195539_1_) {
		String s = p_195539_1_ == null ? "Nixie Tube" : p_195539_1_.getName().getString();
		ITextComponent itextcomponent = (ITextComponent)(p_195539_1_ == null ? new StringTextComponent("Nixie Tube") : p_195539_1_.getDisplayName());
		return new CommandSource(ICommandSource.field_213139_a_, Vector3d.ofCenter(this.pos), Vector2f.ZERO, (ServerWorld)this.world, 2, s, itextcomponent, this.world.getServer(), p_195539_1_);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
