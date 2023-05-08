package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;

public class MechanicalPistonBlock extends DirectionalAxisKineticBlock implements IBE<MechanicalPistonBlockEntity> {

	public static final EnumProperty<PistonState> STATE = EnumProperty.create("state", PistonState.class);
	protected boolean isSticky;

	public static MechanicalPistonBlock normal(Properties properties) {
		return new MechanicalPistonBlock(properties, false);
	}

	public static MechanicalPistonBlock sticky(Properties properties) {
		return new MechanicalPistonBlock(properties, true);
	}

	protected MechanicalPistonBlock(Properties properties, boolean sticky) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH)
			.setValue(STATE, PistonState.RETRACTED));
		isSticky = sticky;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(STATE);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (!player.mayBuild())
			return InteractionResult.PASS;
		if (player.isShiftKeyDown())
			return InteractionResult.PASS;
		if (!player.getItemInHand(handIn)
			.is(Tags.Items.SLIMEBALLS)) {
			if (player.getItemInHand(handIn)
				.isEmpty()) {
				withBlockEntityDo(worldIn, pos, be -> be.assembleNextTick = true);
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}
		if (state.getValue(STATE) != PistonState.RETRACTED)
			return InteractionResult.PASS;
		Direction direction = state.getValue(FACING);
		if (hit.getDirection() != direction)
			return InteractionResult.PASS;
		if (((MechanicalPistonBlock) state.getBlock()).isSticky)
			return InteractionResult.PASS;
		if (worldIn.isClientSide) {
			Vec3 vec = hit.getLocation();
			worldIn.addParticle(ParticleTypes.ITEM_SLIME, vec.x, vec.y, vec.z, 0, 0, 0);
			return InteractionResult.SUCCESS;
		}
		AllSoundEvents.SLIME_ADDED.playOnServer(worldIn, pos, .5f, 1);
		if (!player.isCreative())
			player.getItemInHand(handIn)
				.shrink(1);
		worldIn.setBlockAndUpdate(pos, AllBlocks.STICKY_MECHANICAL_PISTON.getDefaultState()
			.setValue(FACING, direction)
			.setValue(AXIS_ALONG_FIRST_COORDINATE, state.getValue(AXIS_ALONG_FIRST_COORDINATE)));
		return InteractionResult.SUCCESS;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos fromPos,
		boolean p_220069_6_) {
		Direction direction = state.getValue(FACING);
		if (!fromPos.equals(pos.relative(direction.getOpposite())))
			return;
		if (!world.isClientSide && !world.getBlockTicks()
			.willTickThisTick(pos, this))
			world.scheduleTick(pos, this, 0);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource r) {
		Direction direction = state.getValue(FACING);
		BlockState pole = worldIn.getBlockState(pos.relative(direction.getOpposite()));
		if (!AllBlocks.PISTON_EXTENSION_POLE.has(pole))
			return;
		if (pole.getValue(PistonExtensionPoleBlock.FACING)
			.getAxis() != direction.getAxis())
			return;
		withBlockEntityDo(worldIn, pos, be -> {
			if (be.lastException == null)
				return;
			be.lastException = null;
			be.sendData();
		});
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (state.getValue(STATE) != PistonState.RETRACTED)
			return InteractionResult.PASS;
		return super.onWrenched(state, context);
	}

	public enum PistonState implements StringRepresentable {
		RETRACTED, MOVING, EXTENDED;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	@Override
	public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
		Direction direction = state.getValue(FACING);
		BlockPos pistonHead = null;
		BlockPos pistonBase = pos;
		boolean dropBlocks = player == null || !player.isCreative();

		Integer maxPoles = maxAllowedPistonPoles();
		for (int offset = 1; offset < maxPoles; offset++) {
			BlockPos currentPos = pos.relative(direction, offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (isExtensionPole(block) && direction.getAxis() == block.getValue(BlockStateProperties.FACING)
				.getAxis())
				continue;

			if (isPistonHead(block) && block.getValue(BlockStateProperties.FACING) == direction) {
				pistonHead = currentPos;
			}

			break;
		}

		if (pistonHead != null && pistonBase != null) {
			BlockPos.betweenClosedStream(pistonBase, pistonHead)
				.filter(p -> !p.equals(pos))
				.forEach(p -> worldIn.destroyBlock(p, dropBlocks));
		}

		for (int offset = 1; offset < maxPoles; offset++) {
			BlockPos currentPos = pos.relative(direction.getOpposite(), offset);
			BlockState block = worldIn.getBlockState(currentPos);

			if (isExtensionPole(block) && direction.getAxis() == block.getValue(BlockStateProperties.FACING)
				.getAxis()) {
				worldIn.destroyBlock(currentPos, dropBlocks);
				continue;
			}

			break;
		}

		super.playerWillDestroy(worldIn, pos, state, player);
	}

	public static int maxAllowedPistonPoles() {
		return AllConfigs.server().kinetics.maxPistonPoles.get();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {

		if (state.getValue(STATE) == PistonState.EXTENDED)
			return AllShapes.MECHANICAL_PISTON_EXTENDED.get(state.getValue(FACING));

		if (state.getValue(STATE) == PistonState.MOVING)
			return AllShapes.MECHANICAL_PISTON.get(state.getValue(FACING));

		return Shapes.block();
	}

	@Override
	public Class<MechanicalPistonBlockEntity> getBlockEntityClass() {
		return MechanicalPistonBlockEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends MechanicalPistonBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.MECHANICAL_PISTON.get();
	}

	public static boolean isPiston(BlockState state) {
		return AllBlocks.MECHANICAL_PISTON.has(state) || isStickyPiston(state);
	}

	public static boolean isStickyPiston(BlockState state) {
		return AllBlocks.STICKY_MECHANICAL_PISTON.has(state);
	}

	public static boolean isExtensionPole(BlockState state) {
		return AllBlocks.PISTON_EXTENSION_POLE.has(state);
	}

	public static boolean isPistonHead(BlockState state) {
		return AllBlocks.MECHANICAL_PISTON_HEAD.has(state);
	}
}
