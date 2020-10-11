package com.simibubi.create.content.logistics.block.redstone;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.world.server.ServerWorld;

public class NixieTubeTileEntity extends SmartTileEntity {

	Optional<Pair<ITextComponent, Integer>> customText;
	JsonElement rawCustomText;
	Couple<String> renderText;

	int redstoneStrength;

	public NixieTubeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		redstoneStrength = 0;
		customText = Optional.empty();
	}

	@Override
	public void tick() {
		super.tick();

		// Dynamic text components have to be ticked manually and re-sent to the client
		if (customText.isPresent() && world instanceof ServerWorld) {
			Pair<ITextComponent, Integer> textSection = customText.get();
			textSection.setFirst(updateDynamicTextComponents(ITextComponent.Serializer.fromJson(rawCustomText)));

			Couple<String> currentText = getVisibleText();
			if (renderText != null && renderText.equals(currentText))
				return;

			renderText = currentText;
			sendData();
		}
	}

	//

	public void clearCustomText() {
		if (!customText.isPresent())
			return;
		displayRedstoneStrength(0);
	}

	public void displayCustomNameOf(ItemStack stack, int nixiePositionInRow) {
		CompoundNBT compoundnbt = stack.getChildTag("display");
		if (compoundnbt != null && compoundnbt.contains("Name", 8)) {
			JsonElement fromJson = getJsonFromString(compoundnbt.getString("Name"));
			ITextComponent displayed = ITextComponent.Serializer.fromJson(fromJson);
			if (this.world instanceof ServerWorld)
				displayed = updateDynamicTextComponents(displayed);
			this.customText = Optional.of(Pair.of(displayed, nixiePositionInRow));
			this.rawCustomText = fromJson;
			notifyUpdate();
		}
	}

	public void displayRedstoneStrength(int signalStrength) {
		customText = Optional.empty();
		redstoneStrength = signalStrength;
		notifyUpdate();
	}

	public boolean reactsToRedstone() {
		return !customText.isPresent();
	}

	public Couple<String> getVisibleText() {
		if (!customText.isPresent())
			return Couple.create(redstoneStrength < 10 ? "0" : "1", redstoneStrength % 10 + "");
		String fullText = createStringFromComponentText(customText.get()
			.getFirst());
		int index = customText.get()
			.getSecond() * 2;
		return Couple.create(charOrEmpty(fullText, index), charOrEmpty(fullText, index + 1));
	}

	//

	@Override
	protected void read(CompoundNBT nbt, boolean clientPacket) {
		customText = Optional.empty();
		redstoneStrength = nbt.getInt("RedstoneStrength");
		if (nbt.contains("CustomText")) {
			ITextComponent displayed = ITextComponent.Serializer.fromJson(nbt.getString("CustomText"));
			rawCustomText = getJsonFromString(nbt.getString("RawCustomText"));
			customText = Optional.of(Pair.of(displayed, nbt.getInt("CustomTextIndex")));
		}
		super.read(nbt, clientPacket);
	}

	@Override
	protected void write(CompoundNBT nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		nbt.putInt("RedstoneStrength", redstoneStrength);

		if (customText.isPresent()) {
			nbt.putString("RawCustomText", rawCustomText.toString());
			nbt.putString("CustomText", ITextComponent.Serializer.toJson(customText.get()
				.getFirst()));
			nbt.putInt("CustomTextIndex", customText.get()
				.getSecond());
		}
	}

	private JsonElement getJsonFromString(String string) {
		return new JsonParser().parse(string);
	}

	protected ITextComponent updateDynamicTextComponents(ITextComponent customText) {
		try {
			return TextComponentUtils.updateForEntity(this.getCommandSource((ServerPlayerEntity) null), customText,
				(Entity) null, 0);
		} catch (CommandSyntaxException e) {
		}
		return customText;
	}

	// From SignTileEntity
	protected CommandSource getCommandSource(@Nullable ServerPlayerEntity p_195539_1_) {
		String s = p_195539_1_ == null ? "Sign"
			: p_195539_1_.getName()
				.getString();
		ITextComponent itextcomponent =
			(ITextComponent) (p_195539_1_ == null ? new StringTextComponent("Sign") : p_195539_1_.getDisplayName());
		return new CommandSource(ICommandSource.field_213139_a_,
			new Vec3d((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
				(double) this.pos.getZ() + 0.5D),
			Vec2f.ZERO, (ServerWorld) this.world, 2, s, itextcomponent, this.world.getServer(), p_195539_1_);
	}

	protected String createStringFromComponentText(ITextComponent iTextComponent) {
		StringBuilder stringbuilder = new StringBuilder();
		Iterator<ITextComponent> iterator = iTextComponent.stream()
			.iterator();
		while (iterator.hasNext())
			stringbuilder.append(iterator.next()
				.getUnformattedComponentText());
		return stringbuilder.toString();
	}

	private String charOrEmpty(String string, int index) {
		return string.length() <= index ? " " : string.substring(index, index + 1);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
