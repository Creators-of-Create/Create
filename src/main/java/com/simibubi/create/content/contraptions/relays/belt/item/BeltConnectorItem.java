package com.simibubi.create.content.contraptions.relays.belt.item;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractShaftBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraft.item.Item.Properties;

public class BeltConnectorItem extends BlockItem {

	public BeltConnectorItem(Properties properties) {
		super(AllBlocks.BELT.get(), properties);
	}

	@Override
	public String getDescriptionId() {
		return getOrCreateDescriptionId();
	}

	@Override
	public void fillItemCategory(ItemGroup p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
		if (p_150895_1_ == Create.BASE_CREATIVE_TAB)
			return;
		super.fillItemCategory(p_150895_1_, p_150895_2_);
	}

	@Nonnull
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		PlayerEntity playerEntity = context.getPlayer();
		if (playerEntity != null && playerEntity.isShiftKeyDown()) {
			context.getItemInHand()
				.setTag(null);
			return ActionResultType.SUCCESS;
		}

		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		boolean validAxis = validateAxis(world, pos);

		if (world.isClientSide)
			return validAxis ? ActionResultType.SUCCESS : ActionResultType.FAIL;

		CompoundNBT tag = context.getItemInHand()
			.getOrCreateTag();
		BlockPos firstPulley = null;

		// Remove first if no longer existant or valid
		if (tag.contains("FirstPulley")) {
			firstPulley = NBTUtil.readBlockPos(tag.getCompound("FirstPulley"));
			if (!validateAxis(world, firstPulley) || !firstPulley.closerThan(pos, maxLength() * 2)) {
				tag.remove("FirstPulley");
				context.getItemInHand()
					.setTag(tag);
			}
		}

		if (!validAxis || playerEntity == null)
			return ActionResultType.FAIL;

		if (tag.contains("FirstPulley")) {

			if (!canConnect(world, firstPulley, pos))
				return ActionResultType.FAIL;

			if (firstPulley != null && !firstPulley.equals(pos)) {
				createBelts(world, firstPulley, pos);
				AllTriggers.triggerFor(AllTriggers.CONNECT_BELT, playerEntity);
				if (!playerEntity.isCreative())
					context.getItemInHand()
						.shrink(1);
			}

			if (!context.getItemInHand()
				.isEmpty()) {
				context.getItemInHand()
					.setTag(null);
				playerEntity.getCooldowns()
					.addCooldown(this, 5);
			}
			return ActionResultType.SUCCESS;
		}

		tag.put("FirstPulley", NBTUtil.writeBlockPos(pos));
		context.getItemInHand()
			.setTag(tag);
		playerEntity.getCooldowns()
			.addCooldown(this, 5);
		return ActionResultType.SUCCESS;
	}

	public static void createBelts(World world, BlockPos start, BlockPos end) {

		BeltSlope slope = getSlopeBetween(start, end);
		Direction facing = getFacingFromTo(start, end);

		BlockPos diff = end.subtract(start);
		if (diff.getX() == diff.getZ())
			facing = Direction.get(facing.getAxisDirection(), world.getBlockState(start)
				.getValue(BlockStateProperties.AXIS) == Axis.X ? Axis.Z : Axis.X);

		List<BlockPos> beltsToCreate = getBeltChainBetween(start, end, slope, facing);
		BlockState beltBlock = AllBlocks.BELT.getDefaultState();

		for (BlockPos pos : beltsToCreate) {
			BeltPart part = pos.equals(start) ? BeltPart.START : pos.equals(end) ? BeltPart.END : BeltPart.MIDDLE;
			BlockState shaftState = world.getBlockState(pos);
			boolean pulley = ShaftBlock.isShaft(shaftState);
			if (part == BeltPart.MIDDLE && pulley)
				part = BeltPart.PULLEY;
			if (pulley && shaftState.getValue(AbstractShaftBlock.AXIS) == Axis.Y)
				slope = BeltSlope.SIDEWAYS;
			KineticTileEntity.switchToBlockState(world, pos, beltBlock.setValue(BeltBlock.SLOPE, slope)
				.setValue(BeltBlock.PART, part)
				.setValue(BeltBlock.HORIZONTAL_FACING, facing));
		}
	}

	private static Direction getFacingFromTo(BlockPos start, BlockPos end) {
		Axis beltAxis = start.getX() == end.getX() ? Axis.Z : Axis.X;
		BlockPos diff = end.subtract(start);
		AxisDirection axisDirection = AxisDirection.POSITIVE;

		if (diff.getX() == 0 && diff.getZ() == 0)
			axisDirection = diff.getY() > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
		else
			axisDirection = beltAxis.choose(diff.getX(), 0, diff.getZ()) > 0 ? AxisDirection.POSITIVE
				: AxisDirection.NEGATIVE;

		return Direction.get(axisDirection, beltAxis);
	}

	private static BeltSlope getSlopeBetween(BlockPos start, BlockPos end) {
		BlockPos diff = end.subtract(start);

		if (diff.getY() != 0) {
			if (diff.getZ() != 0 || diff.getX() != 0)
				return diff.getY() > 0 ? BeltSlope.UPWARD : BeltSlope.DOWNWARD;
			return BeltSlope.VERTICAL;
		}
		return BeltSlope.HORIZONTAL;
	}

	private static List<BlockPos> getBeltChainBetween(BlockPos start, BlockPos end, BeltSlope slope, Direction direction) {
		List<BlockPos> positions = new LinkedList<>();
		int limit = 1000;
		BlockPos current = start;

		do {
			positions.add(current);

			if (slope == BeltSlope.VERTICAL) {
				current = current.above(direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1);
				continue;
			}

			current = current.relative(direction);
			if (slope != BeltSlope.HORIZONTAL)
				current = current.above(slope == BeltSlope.UPWARD ? 1 : -1);

		} while (!current.equals(end) && limit-- > 0);

		positions.add(end);
		return positions;
	}

	public static boolean canConnect(World world, BlockPos first, BlockPos second) {
		if (!world.isAreaLoaded(first, 1))
			return false;
		if (!world.isAreaLoaded(second, 1))
			return false;
		if (!second.closerThan(first, maxLength()))
			return false;

		BlockPos diff = second.subtract(first);
		Axis shaftAxis = world.getBlockState(first)
			.getValue(BlockStateProperties.AXIS);

		int x = diff.getX();
		int y = diff.getY();
		int z = diff.getZ();
		int sames = ((Math.abs(x) == Math.abs(y)) ? 1 : 0) + ((Math.abs(y) == Math.abs(z)) ? 1 : 0)
			+ ((Math.abs(z) == Math.abs(x)) ? 1 : 0);

		if (shaftAxis.choose(x, y, z) != 0)
			return false;
		if (sames != 1)
			return false;
		if (shaftAxis != world.getBlockState(second)
			.getValue(BlockStateProperties.AXIS))
			return false;
		if (shaftAxis == Axis.Y && x != 0 && z != 0)
			return false;

		TileEntity tileEntity = world.getBlockEntity(first);
		TileEntity tileEntity2 = world.getBlockEntity(second);

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
		for (BlockPos currentPos = first.offset(step); !currentPos.equals(second) && limit-- > 0; currentPos =
			currentPos.offset(step)) {
			BlockState blockState = world.getBlockState(currentPos);
			if (ShaftBlock.isShaft(blockState) && blockState.getValue(AbstractShaftBlock.AXIS) == shaftAxis)
				continue;
			if (!blockState.getMaterial()
				.isReplaceable())
				return false;
		}

		return true;

	}

	protected static Integer maxLength() {
		return AllConfigs.SERVER.kinetics.maxBeltLength.get();
	}

	public static boolean validateAxis(World world, BlockPos pos) {
		if (!world.isAreaLoaded(pos, 1))
			return false;
		if (!ShaftBlock.isShaft(world.getBlockState(pos)))
			return false;
		return true;
	}

}
