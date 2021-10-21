package com.simibubi.create.foundation.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.logistics.item.filter.FilterItem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public final class NBTProcessors {

	private static final Map<TileEntityType<?>, UnaryOperator<CompoundNBT>> processors = new HashMap<>();
	private static final Map<TileEntityType<?>, UnaryOperator<CompoundNBT>> survivalProcessors = new HashMap<>();

	public static synchronized void addProcessor(TileEntityType<?> type, UnaryOperator<CompoundNBT> processor) {
		processors.put(type, processor);
	}

	public static synchronized void addSurvivalProcessor(TileEntityType<?> type, UnaryOperator<CompoundNBT> processor) {
		survivalProcessors.put(type, processor);
	}

	static {
		addProcessor(TileEntityType.SIGN, data -> {
			for (int i = 0; i < 4; ++i) {
				if (textComponentHasClickEvent(data.getString("Text" + (i + 1))))
					return null;
			}
			return data;
		});
		addProcessor(TileEntityType.LECTERN, data -> {
			if (!data.contains("Book", Constants.NBT.TAG_COMPOUND))
				return data;
			CompoundNBT book = data.getCompound("Book");

			if (!book.contains("tag", Constants.NBT.TAG_COMPOUND))
				return data;
			CompoundNBT tag = book.getCompound("tag");

			if (!tag.contains("pages", Constants.NBT.TAG_LIST))
				return data;
			ListNBT pages = tag.getList("pages", Constants.NBT.TAG_STRING);

			for (INBT inbt : pages) {
				if (textComponentHasClickEvent(inbt.getAsString()))
					return null;
			}
			return data;
		});
		addSurvivalProcessor(AllTileEntities.FUNNEL.get(), data -> {
			if (data.contains("Filter")) {
				ItemStack filter = ItemStack.of(data.getCompound("Filter"));
				if (filter.getItem() instanceof FilterItem)
					data.remove("Filter");
			}
			return data;
		});
	}

	public static boolean textComponentHasClickEvent(String json) {
		ITextComponent component = ITextComponent.Serializer.fromJson(json.isEmpty() ? "\"\"" : json);
		return component != null && component.getStyle() != null && component.getStyle().getClickEvent() != null;
	}

	private NBTProcessors() {
	}

	@Nullable
	public static CompoundNBT process(TileEntity tileEntity, CompoundNBT compound, boolean survival) {
		if (compound == null)
			return null;
		TileEntityType<?> type = tileEntity.getType();
		if (survival && survivalProcessors.containsKey(type))
			compound = survivalProcessors.get(type)
				.apply(compound);
		if (compound != null && processors.containsKey(type))
			return processors.get(type)
				.apply(compound);
		if (tileEntity instanceof MobSpawnerTileEntity)
			return compound;
		if (tileEntity.onlyOpCanSetNbt())
			return null;
		return compound;
	}

}
