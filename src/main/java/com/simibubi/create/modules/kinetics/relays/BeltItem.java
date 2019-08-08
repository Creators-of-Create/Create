package com.simibubi.create.modules.kinetics.relays;

import com.simibubi.create.AllBlocks;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BeltItem extends Item {

	public static final int MAX_PULLEY_DISTANCE = 20;

	public BeltItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		boolean validAxis = validateAxis(world, pos);

		if (world.isRemote)
			return validAxis ? ActionResultType.SUCCESS : ActionResultType.FAIL;

		CompoundNBT tag = context.getItem().getOrCreateTag();
		BlockPos firstPulley = null;

		// Remove first if no longer existant or valid
		if (tag.contains("FirstPulley")) {
			firstPulley = NBTUtil.readBlockPos(tag.getCompound("FirstPulley"));
			if (!validateAxis(world, firstPulley)) {
				tag.remove("FirstPulley");
				context.getItem().setTag(tag);
			}
		}

		if (!validAxis)
			return ActionResultType.FAIL;

		if (tag.contains("FirstPulley")) {

			if (!canConnect(world, firstPulley, pos))
				return ActionResultType.FAIL;

			if (!firstPulley.equals(pos)) {
				makePulley(world, firstPulley);
				makePulley(world, pos);
				connectPulley(world, firstPulley, pos, true);
				connectPulley(world, pos, firstPulley, false);
				
				if (!context.getPlayer().isCreative())
					context.getItem().shrink(1);
			}

			tag.remove("FirstPulley");
			if (!context.getItem().isEmpty()) {
				context.getItem().setTag(tag);
				context.getPlayer().getCooldownTracker().setCooldown(this, 5);
			}
			return ActionResultType.SUCCESS;
		}

		tag.put("FirstPulley", NBTUtil.writeBlockPos(pos));
		context.getItem().setTag(tag);
		context.getPlayer().getCooldownTracker().setCooldown(this, 5);
		return ActionResultType.SUCCESS;
	}

	private void makePulley(World world, BlockPos pos) {
		world.setBlockState(pos, AllBlocks.BELT_PULLEY.get().getDefaultState().with(BlockStateProperties.AXIS,
				world.getBlockState(pos).get(BlockStateProperties.AXIS)));
	}
	
	private void connectPulley(World world, BlockPos pos, BlockPos target, boolean controller) {
		BeltPulleyTileEntity te = (BeltPulleyTileEntity) world.getTileEntity(pos);
		if (te != null) {
			te.setController(controller);
			te.setTarget(target);
		}
	}

	private boolean canConnect(World world, BlockPos first, BlockPos second) {
		if (!world.isAreaLoaded(first, 1))
			return false;
		if (!world.isAreaLoaded(second, 1))
			return false;
		if (!second.withinDistance(first, MAX_PULLEY_DISTANCE))
			return false;

		BlockPos diff = second.subtract(first);
		Axis axis = world.getBlockState(first).get(BlockStateProperties.AXIS);
		
		if (axis.getCoordinate(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return false;
		if (axis != world.getBlockState(second).get(BlockStateProperties.AXIS))
			return false;
		
		return true;
	}

	private boolean validateAxis(World world, BlockPos pos) {
		if (!world.isAreaLoaded(pos, 1))
			return false;
		if (!AllBlocks.AXIS.typeOf(world.getBlockState(pos)))
			return false;
		return true;
	}

}
