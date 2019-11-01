package com.simibubi.create.modules.contraptions.receivers;

import java.util.Optional;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.logistics.InWorldProcessing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MechanicalPressTileEntity extends KineticTileEntity {

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
			if (runningTicks < 40) {
				float num = (runningTicks - 1 + partialTicks) / 30f;
				return MathHelper.clamp(num * num * num, 0, beltMode ? 1 + 3 / 16f : 1);
			}
			if (runningTicks >= 40) {
				return MathHelper.clamp(((60 - runningTicks) + 1 - partialTicks) / 20f, 0, beltMode ? 1 + 3 / 16f : 1);
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
		super.tick();
		
		if (!running)
			return;

		if (runningTicks == 30) {
			AxisAlignedBB bb = new AxisAlignedBB(pos.down(beltMode ? 2 : 1));
			for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, bb)) {
				if (!(entity instanceof ItemEntity))
					continue;
				ItemEntity itemEntity = (ItemEntity) entity;

				if (world.isRemote) {
					for (int i = 0; i < 20; i++) {
						Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, world.rand, .25f).mul(1, 0, 1);
						world.addParticle(new ItemParticleData(ParticleTypes.ITEM, itemEntity.getItem()), entity.posX,
								entity.posY, entity.posZ, motion.x, motion.y, motion.z);
					}
				}

				if (!world.isRemote) {
					Optional<PressingRecipe> recipe = getRecipe(itemEntity);
					if (recipe.isPresent())
						InWorldProcessing.applyRecipeOn(itemEntity, recipe.get());
				}
			}
			if (!world.isRemote) {
				world.playSound(null, getPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, .5f, 1f);
				world.playSound(null, getPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, .125f, 1f);
			}
		}

		if (!world.isRemote && runningTicks > 60) {
			finished = true;
			if (!beltMode)
				finished = world.isBlockPowered(pos);
			running = false;
			sendData();
			return;
		}

		runningTicks++;
	}

	public Optional<PressingRecipe> getRecipe(ItemEntity itemEntity) {
		pressingInv.setInventorySlotContents(0, itemEntity.getItem());
		Optional<PressingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.Types.PRESSING, pressingInv,
				world);
		return recipe;
	}

}
