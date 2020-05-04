package com.simibubi.create.modules.contraptions.relays.belt.item;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.item.IAddedByOther;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BeltConnectorItem extends BlockItem implements IAddedByOther {

	public BeltConnectorItem(Properties properties) {
		super(AllBlocks.BELT.get(), properties);
	}

	@Override
	public String getTranslationKey() {
		return getDefaultTranslationKey();
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (context.getPlayer().isSneaking()) {
			context.getItem().setTag(null);
			return ActionResultType.SUCCESS;
		}

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

			if (firstPulley != null && !firstPulley.equals(pos)) {
				createBelts(world, firstPulley, pos);

				if (!context.getPlayer().isCreative())
					context.getItem().shrink(1);
			}

			if (!context.getItem().isEmpty()) {
				context.getItem().setTag(null);
				context.getPlayer().getCooldownTracker().setCooldown(this, 5);
			}
			return ActionResultType.SUCCESS;
		}

		tag.put("FirstPulley", NBTUtil.writeBlockPos(pos));
		context.getItem().setTag(tag);
		context.getPlayer().getCooldownTracker().setCooldown(this, 5);
		return ActionResultType.SUCCESS;
	}

	private void createBelts(World world, BlockPos start, BlockPos end) {

		BeltBlock.Slope slope = getSlopeBetween(start, end);
		Direction facing = getFacingFromTo(start, end);

		BlockPos diff = end.subtract(start);
		if (diff.getX() == diff.getZ())
			facing = Direction.getFacingFromAxis(facing.getAxisDirection(),
					world.getBlockState(start).get(BlockStateProperties.AXIS) == Axis.X ? Axis.Z : Axis.X);

		List<BlockPos> beltsToCreate = getBeltChainBetween(start, end, slope, facing);
		BlockState beltBlock = AllBlocks.BELT.get().getDefaultState();

		for (BlockPos pos : beltsToCreate) {
			BeltBlock.Part part = pos.equals(start) ? Part.START : pos.equals(end) ? Part.END : Part.MIDDLE;
			boolean pulley = ShaftBlock.isShaft(world.getBlockState(pos));
			if (part == Part.MIDDLE && pulley)
				part = Part.PULLEY;
			world.setBlockState(pos, beltBlock.with(BeltBlock.SLOPE, slope).with(BeltBlock.PART, part)
					.with(BeltBlock.HORIZONTAL_FACING, facing), 3);
		}
	}

	private Direction getFacingFromTo(BlockPos start, BlockPos end) {
		Axis beltAxis = start.getX() == end.getX() ? Axis.Z : Axis.X;
		BlockPos diff = end.subtract(start);
		AxisDirection axisDirection = AxisDirection.POSITIVE;

		if (diff.getX() == 0 && diff.getZ() == 0)
			axisDirection = diff.getY() > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		else
			axisDirection = beltAxis.getCoordinate(diff.getX(), 0, diff.getZ()) > 0 ? AxisDirection.POSITIVE
					: AxisDirection.NEGATIVE;

		return Direction.getFacingFromAxis(axisDirection, beltAxis);
	}

	private Slope getSlopeBetween(BlockPos start, BlockPos end) {
		BlockPos diff = end.subtract(start);

		if (diff.getY() != 0) {
			if (diff.getZ() != 0 || diff.getX() != 0)
				return diff.getY() > 0 ? Slope.UPWARD : Slope.DOWNWARD;
			return Slope.VERTICAL;
		}
		return Slope.HORIZONTAL;
	}

	private List<BlockPos> getBeltChainBetween(BlockPos start, BlockPos end, Slope slope, Direction direction) {
		List<BlockPos> positions = new LinkedList<>();
		int limit = 1000;
		BlockPos current = start;

		do {
			positions.add(current);

			if (slope == Slope.VERTICAL) {
				current = current.up(direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1);
				continue;
			}

			current = current.offset(direction);
			if (slope != Slope.HORIZONTAL)
				current = current.up(slope == Slope.UPWARD ? 1 : -1);

		} while (!current.equals(end) && limit-- > 0);

		positions.add(end);
		return positions;
	}

	public static boolean canConnect(World world, BlockPos first, BlockPos second) {
		if (!world.isAreaLoaded(first, 1))
			return false;
		if (!world.isAreaLoaded(second, 1))
			return false;
		if (!second.withinDistance(first, AllConfigs.SERVER.kinetics.maxBeltLength.get()))
			return false;

		BlockPos diff = second.subtract(first);
		Axis axis = world.getBlockState(first).get(BlockStateProperties.AXIS);

		int x = diff.getX();
		int y = diff.getY();
		int z = diff.getZ();
		int sames = ((Math.abs(x) == Math.abs(y)) ? 1 : 0) + ((Math.abs(y) == Math.abs(z)) ? 1 : 0)
				+ ((Math.abs(z) == Math.abs(x)) ? 1 : 0);

		if (axis.getCoordinate(x, y, z) != 0)
			return false;
		if (sames != 1)
			return false;
		if (axis != world.getBlockState(second).get(BlockStateProperties.AXIS))
			return false;

		TileEntity tileEntity = world.getTileEntity(first);
		TileEntity tileEntity2 = world.getTileEntity(second);

		if (!(tileEntity instanceof KineticTileEntity))
			return false;
		if (!(tileEntity2 instanceof KineticTileEntity))
			return false;

		float speed1 = ((KineticTileEntity) tileEntity).getTheoreticalSpeed();
		float speed2 = ((KineticTileEntity) tileEntity2).getTheoreticalSpeed();
		if (Math.signum(speed1) != Math.signum(speed2) && speed1 != 0 && speed2 != 0)
			return false;

		BlockPos step = new BlockPos(Math.signum(diff.getX()), Math.signum(diff.getY()), Math.signum(diff.getZ()));
		int limit = 1000;
		for (BlockPos currentPos = first.add(step); !currentPos.equals(second) && limit-- > 0; currentPos =
			currentPos.add(step)) {
			BlockState blockState = world.getBlockState(currentPos);
			if (ShaftBlock.isShaft(blockState) && blockState.get(ShaftBlock.AXIS) == axis)
				continue;
			if (!blockState.getMaterial().isReplaceable())
				return false;
		}

		return true;

	}

	public static boolean validateAxis(World world, BlockPos pos) {
		if (!world.isAreaLoaded(pos, 1))
			return false;
		if (!ShaftBlock.isShaft(world.getBlockState(pos)))
			return false;
		if (world.getBlockState(pos).get(BlockStateProperties.AXIS) == Axis.Y)
			return false;
		return true;
	}

}
