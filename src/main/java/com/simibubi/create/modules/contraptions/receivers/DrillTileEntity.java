package com.simibubi.create.modules.contraptions.receivers;

import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public class DrillTileEntity extends KineticTileEntity {

	private static final AtomicInteger NEXT_DRILL_ID = new AtomicInteger();

	public static DamageSource damageSourceDrill = new DamageSource("create.drill").setDamageBypassesArmor();
	private int ticksUntilNextProgress;
	private int destroyProgress;
	private int drillId = -NEXT_DRILL_ID.incrementAndGet();

	public DrillTileEntity() {
		super(AllTileEntities.DRILL.type);
	}

	@Override
	public void onSpeedChanged() {
		if (destroyProgress == -1)
			destroyNextTick();
	}

	public void destroyNextTick() {
		ticksUntilNextProgress = 1;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("Progress", destroyProgress);
		compound.putInt("NextTick", ticksUntilNextProgress);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		destroyProgress = compound.getInt("Progress");
		ticksUntilNextProgress = compound.getInt("NextTick");
		super.read(compound);
	}

	@Override
	public void remove() {
		if (!world.isRemote && destroyProgress != 0) {
			BlockPos posToBreak = pos.offset(getBlockState().get(BlockStateProperties.FACING));
			world.sendBlockBreakProgress(drillId, posToBreak, -1);
		}
		super.remove();
	}

	@Override
	public void tick() {
		super.tick();
		
		if (world.isRemote)
			return;
		if (speed == 0)
			return;

		BlockPos posToBreak = pos.offset(getBlockState().get(BlockStateProperties.FACING));

		if (ticksUntilNextProgress < 0) {
			for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(posToBreak)))
				if (!(entity instanceof ItemEntity))
					entity.attackEntityFrom(damageSourceDrill, MathHelper.clamp(Math.abs(speed / 512f) + 1, 0, 20));
			return;
		}
		if (ticksUntilNextProgress-- > 0)
			return;

		BlockState stateToBreak = world.getBlockState(posToBreak);
		float blockHardness = stateToBreak.getBlockHardness(world, posToBreak);

		if (stateToBreak.getMaterial().isLiquid() || stateToBreak.getBlock() instanceof AirBlock
				|| blockHardness == -1) {
			if (destroyProgress != 0) {
				destroyProgress = 0;
				world.sendBlockBreakProgress(drillId, posToBreak, -1);
			}
			return;
		}

		float breakSpeed = Math.abs(speed / 100f);
		destroyProgress += MathHelper.clamp((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);

		if (destroyProgress >= 10) {

			IFluidState ifluidstate = world.getFluidState(pos);
			world.playEvent(2001, posToBreak, Block.getStateId(stateToBreak));
			TileEntity tileentity = stateToBreak.hasTileEntity() ? world.getTileEntity(posToBreak) : null;
			Vec3d vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(posToBreak), world.rand, .125f);

			Block.getDrops(stateToBreak, (ServerWorld) world, posToBreak, tileentity).forEach((stack) -> {
				if (!stack.isEmpty() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)
						&& !world.restoringBlockSnapshots) {
					ItemEntity itementity = new ItemEntity(world, vec.x, vec.y, vec.z, stack);
					itementity.setDefaultPickupDelay();
					itementity.setMotion(Vec3d.ZERO);
					world.addEntity(itementity);
				}
			});

			stateToBreak.spawnAdditionalDrops(world, posToBreak, ItemStack.EMPTY);
			world.setBlockState(posToBreak, ifluidstate.getBlockState(), 3);

			destroyProgress = 0;
			ticksUntilNextProgress = -1;
			world.sendBlockBreakProgress(drillId, posToBreak, -1);
			return;
		}

		ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
		world.sendBlockBreakProgress(drillId, posToBreak, (int) destroyProgress);
	}

}
