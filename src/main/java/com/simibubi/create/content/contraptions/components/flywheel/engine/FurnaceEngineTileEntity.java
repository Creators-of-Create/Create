package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.components.flywheel.FlywheelBlock;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class FurnaceEngineTileEntity extends EngineTileEntity {
	float modifier = 0;
	boolean active = false;
	float speed = 0;
	float capacity = 0;

	public FurnaceEngineTileEntity(TileEntityType<? extends FurnaceEngineTileEntity> type) {
		super(type);
	}

	@Override
	public void lazyTick() {
		updateFurnace();
		super.lazyTick();
	}

	@Override
	public void tick() {
		slimUpdateFurnace();
		super.tick();
	}

	public void updateFurnace() {
		BlockPos furnacePos = EngineBlock.getBaseBlockPos(getBlockState(), pos);
		BlockState state = world.getBlockState(furnacePos);
		if (world.getTileEntity(furnacePos) == null) {return;}
		if (!(state.getBlock() instanceof AbstractFurnaceBlock))
			return;

		for (Direction d : Direction.values()) {
			System.out.println(world.getBlockState(furnacePos.offset(d)));
			if (AllTags.AllBlockTags.FLYWHEELBLACKLIST.matches(world.getBlockState(furnacePos.offset(d)))) {
				world.destroyBlock(furnacePos.offset(d), true);
				return;
			}
		}

		modifier = state.getBlock() == Blocks.BLAST_FURNACE ? 2 : 1;
		speed = active ? 16 * modifier : 0;
		capacity = (float)
				(active ? AllConfigs.SERVER.kinetics.stressValues.getCapacityOf(AllBlocks.FURNACE_ENGINE.get()) : 0);

		refreshWheelSpeed();
	}

	public void slimUpdateFurnace() {
		BlockPos furnacePos = EngineBlock.getBaseBlockPos(getBlockState(), pos);
		if (world.getTileEntity(furnacePos) == null) {return;}
		if (!(world.getTileEntity(furnacePos) instanceof AbstractFurnaceTileEntity)) {return;}

		AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) world.getTileEntity(furnacePos);
		NonNullList<ItemStack> items = ObfuscationReflectionHelper.getPrivateValue(AbstractFurnaceTileEntity.class, furnace, "items");

		active = items != null && items.get(0).getCount() != 0;

		appliedCapacity = active ? capacity : 0;
		appliedSpeed = active ? speed : 0;

		if (!active && poweredWheel != null) {
			poweredWheel.setStoppingCooldown(1);
			refreshWheelSpeed();
		}
	}
}
