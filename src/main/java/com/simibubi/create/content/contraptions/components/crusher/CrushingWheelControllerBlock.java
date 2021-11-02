package com.simibubi.create.content.contraptions.components.crusher;

import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class CrushingWheelControllerBlock extends DirectionalBlock
		implements ITE<CrushingWheelControllerTileEntity> {

	public CrushingWheelControllerBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	public static final BooleanProperty VALID = BooleanProperty.create("valid");

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext) {
		return false;
	}

	@Override
	public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CRUSHING_WHEEL_CONTROLLER.create();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(VALID);
		builder.add(FACING);
		super.createBlockStateDefinition(builder);
	}

	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!state.getValue(VALID))
			return;

		Direction facing = state.getValue(FACING);
		Axis axis = facing.getAxis();

		checkEntityForProcessing(worldIn, pos, entityIn);

		withTileEntityDo(worldIn, pos, te -> {
			if (te.processingEntity == entityIn)

				entityIn.makeStuckInBlock(state, new Vector3d(axis == Axis.X ? (double) 0.05F : 0.25D
						, axis == Axis.Y ? (double) 0.05F : 0.25D
						, axis == Axis.Z ? (double) 0.05F : 0.25D));
		});
	}

	public void checkEntityForProcessing(World worldIn, BlockPos pos, Entity entityIn) {
		CrushingWheelControllerTileEntity te = getTileEntity(worldIn, pos);
		if (te == null)
			return;
		if (te.crushingspeed == 0)
			return;
		if (entityIn instanceof ItemEntity)
			((ItemEntity) entityIn).setPickUpDelay(10);
		CompoundNBT data = entityIn.getPersistentData();
		if (data.contains("BypassCrushingWheel")) {
			if (pos.equals(NBTUtil.readBlockPos(data.getCompound("BypassCrushingWheel"))))
				return;
		}
		if (te.isOccupied())
			return;
		boolean isPlayer = entityIn instanceof PlayerEntity;
		if (isPlayer && ((PlayerEntity) entityIn).isCreative())
			return;
		if (isPlayer && entityIn.level.getDifficulty() == Difficulty.PEACEFUL)
			return;

		te.startCrushing(entityIn);
	}

	@Override
	public void updateEntityAfterFallOn(IBlockReader worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		//Moved to onEntityCollision to allow for omnidirectional input
	}

	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (!stateIn.getValue(VALID))
			return;
		if (rand.nextInt(1) != 0)
			return;
		double d0 = (double) ((float) pos.getX() + rand.nextFloat());
		double d1 = (double) ((float) pos.getY() + rand.nextFloat());
		double d2 = (double) ((float) pos.getZ() + rand.nextFloat());
		worldIn.addParticle(ParticleTypes.CRIT, d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		updateSpeed(stateIn, worldIn, currentPos);
		return stateIn;
	}

	public void updateSpeed(BlockState state, IWorld world, BlockPos pos) {
		withTileEntityDo(world, pos, te -> {
			if (!state.getValue(VALID)) {
				if (te.crushingspeed != 0) {
					te.crushingspeed = 0;
					te.sendData();
				}
				return;
			}

			for (Direction d : Iterate.directions) {
				BlockState neighbour = world.getBlockState(pos.relative(d));
				if (!AllBlocks.CRUSHING_WHEEL.has(neighbour))
					continue;
				if (neighbour.getValue(BlockStateProperties.AXIS) == d.getAxis())
					continue;
				TileEntity adjTe = world.getBlockEntity(pos.relative(d));
				if (!(adjTe instanceof KineticTileEntity))
					continue;
				te.crushingspeed = Math.abs(((KineticTileEntity) adjTe).getSpeed() / 50f);
				te.sendData();
				break;
			}
		});
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
										ISelectionContext context) {
		VoxelShape standardShape = AllShapes.CRUSHING_WHEEL_CONTROLLER_COLLISION.get(state.getValue(FACING));

		if (!state.getValue(VALID))
			return standardShape;

		Entity entity = context.getEntity();
		if (entity == null)
			return standardShape;

		CompoundNBT data = entity.getPersistentData();
		if (data.contains("BypassCrushingWheel"))
			if (pos.equals(NBTUtil.readBlockPos(data.getCompound("BypassCrushingWheel"))))
				if (state.getValue(FACING) != Direction.UP) // Allow output items to land on top of the block rather than falling back through.
					return VoxelShapes.empty();

		CrushingWheelControllerTileEntity te = getTileEntity(worldIn, pos);
		if (te != null && te.processingEntity == entity)
			return VoxelShapes.empty();

		return standardShape;
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		worldIn.removeBlockEntity(pos);
	}

	@Override
	public Class<CrushingWheelControllerTileEntity> getTileEntityClass() {
		return CrushingWheelControllerTileEntity.class;
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
