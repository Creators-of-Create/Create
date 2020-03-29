package com.simibubi.create.modules.contraptions.relays.advanced.sequencer;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.modules.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.modules.contraptions.base.KineticBlock;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class SequencedGearshiftBlock extends HorizontalAxisKineticBlock
		implements IWithTileEntity<SequencedGearshiftTileEntity> {

	public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
	public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 5);

	public SequencedGearshiftBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(STATE, VERTICAL));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SequencedGearshiftTileEntity();
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(STATE) != 0;
		if (previouslyPowered != worldIn.isBlockPowered(pos))
			withTileEntityDo(worldIn, pos, SequencedGearshiftTileEntity::onRedstoneUpdate);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		if (state.get(VERTICAL))
			return face.getAxis().isVertical();
		return super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		ItemStack held = player.getHeldItemMainhand();
		if (AllItems.WRENCH.typeOf(held))
			return false;
		if (held.getItem() instanceof BlockItem) {
			BlockItem blockItem = (BlockItem) held.getItem();
			if (blockItem.getBlock() instanceof KineticBlock && hasShaftTowards(worldIn, pos, state, hit.getFace()))
				return false;
		}

		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			displayScreen((SequencedGearshiftTileEntity) worldIn.getTileEntity(pos));
		});
		return true;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(SequencedGearshiftTileEntity te) {
		ScreenOpener.open(new SequencedGearshiftScreen(te));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Axis preferredAxis = RotatedPillarKineticBlock.getPreferredAxis(context);
		if (preferredAxis != null && !context.getPlayer().isSneaking())
			return withAxis(preferredAxis, context);
		return withAxis(context.getNearestLookingDirection().getAxis(), context);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		Direction facing = context.getFace();
		if (facing.getAxis().isVertical() && !state.get(VERTICAL)) {
			KineticTileEntity.switchToBlockState(context.getWorld(), context.getPos(), state.cycle(VERTICAL));
			return ActionResultType.SUCCESS;
		}
		return super.onWrenched(state, context);
	}

	private BlockState withAxis(Axis axis, BlockItemUseContext context) {
		BlockState state = getDefaultState().with(VERTICAL, axis.isVertical());
		if (axis.isVertical())
			return state.with(HORIZONTAL_AXIS, context.getPlacementHorizontalFacing().getAxis());
		return state.with(HORIZONTAL_AXIS, axis);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		if (state.get(VERTICAL))
			return Axis.Y;
		return super.getRotationAxis(state);
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

}
