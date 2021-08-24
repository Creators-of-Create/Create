package com.simibubi.create.foundation.utility.recipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.function.Supplier;

public class TileEntityAwareRecipeWrapper extends RecipeWrapper implements Supplier<TileEntity> {
	private final TileEntity tileEntity;

	public TileEntityAwareRecipeWrapper(IItemHandlerModifiable inv, TileEntity tileEntity) {
		super(inv);
		this.tileEntity = tileEntity;
	}

	@Override
	public TileEntity get() {
		return tileEntity;
	}
}
