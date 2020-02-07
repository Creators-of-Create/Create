package com.simibubi.create.modules.contraptions.components.contraptions.piston;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class MechanicalPistonBlock extends DirectionalAxisKineticBlock {

	public static final EnumProperty<PistonState> STATE = EnumProperty.create("state", PistonState.class);
	protected boolean isSticky;

	public MechanicalPistonBlock(boolean sticky) {
		super(Properties.from(Blocks.PISTON));
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(STATE, PistonState.RETRACTED));
		isSticky = sticky;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(STATE);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (state.get(STATE) != PistonState.RETRACTED)
			return false;
		if (!player.isAllowEdit())
			return false;
		if (!player.getHeldItem(handIn).getItem().isIn(Tags.Items.SLIMEBALLS))
			return false;
		Direction direction = state.get(FACING);
		if (hit.getFace() != direction)
			return false;
		if (((MechanicalPistonBlock) state.getBlock()).isSticky)
			return false;
		if (worldIn.isRemote) {
			Vec3d vec = hit.getHitVec();
			worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
			return true;
		}
		worldIn.playSound(null, pos, SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.BLOCKS, .5f, 1);
		if (!player.isCreative())
			player.getHeldItem(handIn).shrink(1);
		worldIn.setBlockState(pos, AllBlocks.STICKY_MECHANICAL_PISTON.get().getDefaultState().with(FACING, direction)
				.with(AXIS_ALONG_FIRST_COORDINATE, state.get(AXIS_ALONG_FIRST_COORDINATE)));
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalPistonTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (state.get(STATE) != PistonState.RETRACTED)
			return ActionResultType.PASS;
		return super.onWrenched(state, context);
	}

	@Override
	protected boolean turnBackOnWrenched() {
		return true;
	}

	public enum PistonState implements IStringSerializable {
		RETRACTED,
		MOVING,
		EXTENDED;

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		Direction direction = state.get(FACING);
		BlockPos pistonHead = null;
		BlockPos pistonBase = pos;
		boolean dropBlocks = player == null || !player.isCreative();

		Integer maxPoles = maxAllowedPistonPoles();
		for (int offset = 1; offset < maxPoles; offset++) {
			BlockPos currentPos = pos.offset(direction, offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (AllBlocks.PISTON_POLE.typeOf(block)
					&& direction.getAxis() == block.get(BlockStateProperties.FACING).getAxis())
				continue;

			if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(block) && block.get(BlockStateProperties.FACING) == direction) {
				pistonHead = currentPos;
			}

			break;
		}

		if (pistonHead != null && pistonBase != null) {
			BlockPos.getAllInBox(pistonBase, pistonHead).filter(p -> !p.equals(pos))
					.forEach(p -> worldIn.destroyBlock(p, dropBlocks));
		}

		for (int offset = 1; offset < maxPoles; offset++) {
			BlockPos currentPos = pos.offset(direction.getOpposite(), offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (AllBlocks.PISTON_POLE.typeOf(block)
					&& direction.getAxis() == block.get(BlockStateProperties.FACING).getAxis()) {
				worldIn.destroyBlock(currentPos, dropBlocks);
				continue;
			}

			break;
		}

		super.onBlockHarvested(worldIn, pos, state, player);
	}

	public static int maxAllowedPistonPoles() {
		return AllConfigs.SERVER.kinetics.maxPistonPoles.get();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		if (state.get(STATE) == PistonState.EXTENDED)
			return AllShapes.MECHANICAL_PISTON_EXTENDED.get(state.get(FACING));

		if (state.get(STATE) == PistonState.MOVING)
			return AllShapes.MECHANICAL_PISTON.get(state.get(FACING));

		return VoxelShapes.fullCube();
	}

}
