package com.simibubi.create.foundation.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;

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
				String s = data.getString("Text" + (i + 1));
				ITextComponent textcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
				if (textcomponent != null && textcomponent.getStyle() != null
						&& textcomponent.getStyle().getClickEvent() != null)
					return null;
			}
			return data;
		});
	}

	private NBTProcessors() {
	}

	@Nullable
	public static CompoundNBT process(TileEntity tileEntity, CompoundNBT compound, boolean survival) {
		if (compound == null)
			return null;
		TileEntityType<?> type = tileEntity.getType();
		if (survival && survivalProcessors.containsKey(type))
			compound = survivalProcessors.get(type).apply(compound);
		if (processors.containsKey(type))
			return processors.get(type).apply(compound);
		if (tileEntity.onlyOpsCanSetNbt())
			return null;
		return compound;
	}

}
