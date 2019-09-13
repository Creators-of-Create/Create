package com.simibubi.create.modules.contraptions.receivers;

import java.util.Optional;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.logistics.InWorldProcessing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MechanicalPressTileEntity extends KineticTileEntity implements ITickableTileEntity {

	public static class PressingInv extends RecipeWrapper {
		public PressingInv() {
			super(new ItemStackHandler(1));
		}
	}

	private static PressingInv pressingInv = new PressingInv();
	public int runningTicks;
	public boolean running;
	public boolean beltMode;
	public boolean finished;

	public MechanicalPressTileEntity() {
		super(AllTileEntities.MECHANICAL_PRESS.type);
	}

	@Override
	public void read(CompoundNBT compound) {
		running = compound.getBoolean("Running");
		beltMode = compound.getBoolean("OnBelt");
		finished = compound.getBoolean("Finished");
		runningTicks = compound.getInt("Ticks");
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putBoolean("Running", running);
		compound.putBoolean("OnBelt", beltMode);
		compound.putBoolean("Finished", finished);
		compound.putInt("Ticks", runningTicks);
		return super.write(compound);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).expand(0, -1.5, 0);
	}

	public float getRenderedHeadOffset(float partialTicks) {
		if (running) {
			if (runningTicks < 50) {
				return MathHelper.clamp((runningTicks - 1 + partialTicks) / 20f, 0, beltMode ? 1 + 3 / 16f : 1);
			}
			if (runningTicks >= 50) {
				return MathHelper.clamp(((100 - runningTicks) + 1 - partialTicks) / 20f, 0, beltMode ? 1 + 3 / 16f : 1);
			}
		}
		return 0;
	}

	public void start(boolean onBelt) {
		beltMode = onBelt;
		running = true;
		runningTicks = 0;
		sendData();
	}

	@Override
	public void tick() {
		if (!running)
			return;

		if (!world.isRemote && runningTicks > 100) {

			AxisAlignedBB bb = new AxisAlignedBB(pos.down(beltMode ? 2 : 1));
			for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, bb)) {
				if (!(entity instanceof ItemEntity))
					continue;

				ItemEntity itemEntity = (ItemEntity) entity;
				pressingInv.setInventorySlotContents(0, itemEntity.getItem());
				Optional<PressingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.Types.PRESSING,
						pressingInv, world);
				if (recipe.isPresent())
					InWorldProcessing.applyRecipeOn(itemEntity, recipe.get());
			}

			finished = true;
			if (!beltMode)
				finished = world.isBlockPowered(pos);
			running = false;
			sendData();
			return;
		}

		runningTicks++;
	}

}
