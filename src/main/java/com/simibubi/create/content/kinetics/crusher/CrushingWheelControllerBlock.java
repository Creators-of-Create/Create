package com.simibubi.create.content.kinetics.crusher;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrushingWheelControllerBlock extends DirectionalBlock implements IBE<CrushingWheelControllerBlockEntity> {

	public CrushingWheelControllerBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	public static final BooleanProperty VALID = BooleanProperty.create("valid");

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		return false;
	}

	@Override
	public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(VALID);
		builder.add(FACING);
		super.createBlockStateDefinition(builder);
	}

	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		if (!state.getValue(VALID))
			return;

		Direction facing = state.getValue(FACING);
		Axis axis = facing.getAxis();

		checkEntityForProcessing(worldIn, pos, entityIn);

		withBlockEntityDo(worldIn, pos, be -> {
			if (be.processingEntity == entityIn)

				entityIn.makeStuckInBlock(state, new Vec3(axis == Axis.X ? (double) 0.05F : 0.25D,
					axis == Axis.Y ? (double) 0.05F : 0.25D, axis == Axis.Z ? (double) 0.05F : 0.25D));
		});
	}

	public void checkEntityForProcessing(Level worldIn, BlockPos pos, Entity entityIn) {
		CrushingWheelControllerBlockEntity be = getBlockEntity(worldIn, pos);
		if (be == null)
			return;
		if (be.crushingspeed == 0)
			return;
//		if (entityIn instanceof ItemEntity)
//			((ItemEntity) entityIn).setPickUpDelay(10);
		CompoundTag data = entityIn.getPersistentData();
		if (data.contains("BypassCrushingWheel")) {
			if (pos.equals(NbtUtils.readBlockPos(data.getCompound("BypassCrushingWheel"))))
				return;
		}
		if (be.isOccupied())
			return;
		boolean isPlayer = entityIn instanceof Player;
		if (isPlayer && ((Player) entityIn).isCreative())
			return;
		if (isPlayer && entityIn.level.getDifficulty() == Difficulty.PEACEFUL)
			return;

		be.startCrushing(entityIn);
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		// Moved to onEntityCollision to allow for omnidirectional input
	}

	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
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
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		updateSpeed(stateIn, worldIn, currentPos);
		return stateIn;
	}

	public void updateSpeed(BlockState state, LevelAccessor world, BlockPos pos) {
		withBlockEntityDo(world, pos, be -> {
			if (!state.getValue(VALID)) {
				if (be.crushingspeed != 0) {
					be.crushingspeed = 0;
					be.sendData();
				}
				return;
			}

			for (Direction d : Iterate.directions) {
				BlockState neighbour = world.getBlockState(pos.relative(d));
				if (!AllBlocks.CRUSHING_WHEEL.has(neighbour))
					continue;
				if (neighbour.getValue(BlockStateProperties.AXIS) == d.getAxis())
					continue;
				BlockEntity adjBE = world.getBlockEntity(pos.relative(d));
				if (!(adjBE instanceof CrushingWheelBlockEntity cwte))
					continue;
				be.crushingspeed = Math.abs(cwte.getSpeed() / 50f);
				be.sendData();
				
				cwte.award(AllAdvancements.CRUSHING_WHEEL);
				if (cwte.getSpeed() > 255) 
					cwte.award(AllAdvancements.CRUSHER_MAXED);
				
				break;
			}
		});
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		VoxelShape standardShape = AllShapes.CRUSHING_WHEEL_CONTROLLER_COLLISION.get(state.getValue(FACING));

		if (!state.getValue(VALID))
			return standardShape;
		if (!(context instanceof EntityCollisionContext))
			return standardShape;
		Entity entity = ((EntityCollisionContext) context).getEntity();
		if (entity == null)
			return standardShape;

		CompoundTag data = entity.getPersistentData();
		if (data.contains("BypassCrushingWheel"))
			if (pos.equals(NbtUtils.readBlockPos(data.getCompound("BypassCrushingWheel"))))
				if (state.getValue(FACING) != Direction.UP) // Allow output items to land on top of the block rather
															// than falling back through.
					return Shapes.empty();

		CrushingWheelControllerBlockEntity be = getBlockEntity(worldIn, pos);
		if (be != null && be.processingEntity == entity)
			return Shapes.empty();

		return standardShape;
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
			return;

		withBlockEntityDo(worldIn, pos, be -> ItemHelper.dropContents(worldIn, pos, be.inventory));
		worldIn.removeBlockEntity(pos);
	}

	@Override
	public Class<CrushingWheelControllerBlockEntity> getBlockEntityClass() {
		return CrushingWheelControllerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends CrushingWheelControllerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER.get();
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
