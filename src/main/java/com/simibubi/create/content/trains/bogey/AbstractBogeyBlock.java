package com.simibubi.create.content.trains.bogey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class AbstractBogeyBlock<T extends AbstractBogeyBlockEntity> extends Block implements IBE<T>, ProperWaterloggedBlock, ISpecialBlockItemRequirement, IWrenchable {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	static final List<ResourceLocation> BOGEYS = new ArrayList<>();
	public BogeySizes.BogeySize size;


	public AbstractBogeyBlock(Properties pProperties, BogeySizes.BogeySize size) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
		this.size = size;
	}

	public boolean isOnIncompatibleTrack(Carriage carriage, boolean leading) {
		TravellingPoint point = leading ? carriage.getLeadingPoint() : carriage.getTrailingPoint();
		CarriageBogey bogey = leading ? carriage.leadingBogey() : carriage.trailingBogey();
		TrackEdge currentEdge = point.edge;
		if (currentEdge == null)
			return false;
		return currentEdge.getTrackMaterial().trackType != getTrackType(bogey.getStyle());
	}

	public Set<TrackMaterial.TrackType> getValidPathfindingTypes(BogeyStyle style) {
		return ImmutableSet.of(getTrackType(style));
	}

	public abstract TrackMaterial.TrackType getTrackType(BogeyStyle style);

	/**
	 * Only for internal Create use. If you have your own style set, do not call this method
	 */
	@Deprecated
	public static void registerStandardBogey(ResourceLocation block) {
		BOGEYS.add(block);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS, WATERLOGGED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
								  LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return pState;
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	static final EnumSet<Direction> STICKY_X = EnumSet.of(Direction.EAST, Direction.WEST);
	static final EnumSet<Direction> STICKY_Z = EnumSet.of(Direction.SOUTH, Direction.NORTH);

	public EnumSet<Direction> getStickySurfaces(BlockGetter world, BlockPos pos, BlockState state) {
		return state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X ? STICKY_X : STICKY_Z;
	}

	public abstract double getWheelPointSpacing();

	public abstract double getWheelRadius();

	public Vec3 getConnectorAnchorOffset(boolean upsideDown) {
		return getConnectorAnchorOffset();
	}

	/**
	 * This should be implemented, but not called directly
	 */
	protected abstract Vec3 getConnectorAnchorOffset();

	public boolean allowsSingleBogeyCarriage() {
		return true;
	}

	public abstract BogeyStyle getDefaultStyle();

	/**
	 * Legacy system doesn't capture bogey block entities when constructing a train
	 */
	public boolean captureBlockEntityForTrain() {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void render(@Nullable BlockState state, float wheelAngle, PoseStack ms, float partialTicks,
		MultiBufferSource buffers, int light, int overlay, BogeyStyle style, CompoundTag bogeyData) {
		if (style == null)
			style = getDefaultStyle();

		final Optional<BogeyRenderer.CommonRenderer> commonRenderer
				= style.getInWorldCommonRenderInstance();
		final BogeyRenderer renderer = style.getInWorldRenderInstance(this.getSize());
		if (state != null) {
			ms.translate(.5f, .5f, .5f);
			if (state.getValue(AXIS) == Direction.Axis.X)
				ms.mulPose(Vector3f.YP.rotationDegrees(90));
		}
		ms.translate(0, -1.5 - 1 / 128f, 0);
		VertexConsumer vb = buffers.getBuffer(RenderType.cutoutMipped());
		if (bogeyData == null)
			bogeyData = new CompoundTag();
		renderer.render(bogeyData, wheelAngle, ms, light, vb, state == null);
		CompoundTag finalBogeyData = bogeyData;
		commonRenderer.ifPresent(common ->
				common.render(finalBogeyData, wheelAngle, ms, light, vb, state == null));
	}

	public BogeySizes.BogeySize getSize() {
		return this.size;
	}

	public Direction getBogeyUpDirection() {
		return Direction.UP;
	}

	public boolean isTrackAxisAlongFirstCoordinate(BlockState state) {
		return state.getValue(AXIS) == Direction.Axis.X;
	}

	@Nullable
	public BlockState getMatchingBogey(Direction upDirection, boolean axisAlongFirst) {
		if (upDirection != Direction.UP)
			return null;
		return defaultBlockState().setValue(AXIS, axisAlongFirst ? Direction.Axis.X : Direction.Axis.Z);
	}

	@Override
	public final InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
								 BlockHitResult hit) {
		if (level.isClientSide)
			return InteractionResult.PASS;
		ItemStack stack = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && stack.is(AllItems.WRENCH.get()) && !player.getCooldowns().isOnCooldown(stack.getItem())
				&& AllBogeyStyles.BOGEY_STYLES.size() > 1) {

			BlockEntity be = level.getBlockEntity(pos);

			if (!(be instanceof AbstractBogeyBlockEntity sbbe))
				return InteractionResult.FAIL;

			player.getCooldowns().addCooldown(stack.getItem(), 20);
			BogeyStyle currentStyle = sbbe.getStyle();

			BogeySizes.BogeySize size = getSize();

			BogeyStyle style = this.getNextStyle(currentStyle);
			if (style == currentStyle)
				return InteractionResult.PASS;

			Set<BogeySizes.BogeySize> validSizes = style.validSizes();

			for (int i = 0; i < BogeySizes.count(); i++) {
				if (validSizes.contains(size)) break;
				size = size.increment();
			}

			sbbe.setBogeyStyle(style);

			CompoundTag defaultData = style.defaultData;
			sbbe.setBogeyData(sbbe.getBogeyData().merge(defaultData));

			if (size == getSize()) {
				player.displayClientMessage(Lang.translateDirect("bogey.style.updated_style")
						.append(": ").append(style.displayName), true);
			} else {
				CompoundTag oldData = sbbe.getBogeyData();
				level.setBlock(pos, this.getStateOfSize(sbbe, size), 3);
				BlockEntity newBlockEntity = level.getBlockEntity(pos);
				if (!(newBlockEntity instanceof AbstractBogeyBlockEntity newBlockEntity1))
					return InteractionResult.FAIL;
				newBlockEntity1.setBogeyData(oldData);
				player.displayClientMessage(Lang.translateDirect("bogey.style.updated_style_and_size")
						.append(": ").append(style.displayName), true);
			}

			return InteractionResult.CONSUME;
		}

		return onInteractWithBogey(state, level, pos, player, hand, hit);
	}

	// Allows for custom interactions with bogey block to be added simply
	protected InteractionResult onInteractWithBogey(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
													BlockHitResult hit) {
		return InteractionResult.PASS;
	}

	/**
	 * If, instead of using the style-based cycling system you prefer to use separate blocks, return them from this method
	 */
	protected List<ResourceLocation> getBogeyBlockCycle() {
		return BOGEYS;
	}

	@Override
	public BlockState getRotatedBlockState(BlockState state, Direction targetedFace) {
		Block block = state.getBlock();
		List<ResourceLocation> bogeyCycle = getBogeyBlockCycle();
		int indexOf = bogeyCycle.indexOf(RegisteredObjects.getKeyOrThrow(block));
		if (indexOf == -1)
			return state;
		int index = (indexOf + 1) % bogeyCycle.size();
		Direction bogeyUpDirection = getBogeyUpDirection();
		boolean trackAxisAlongFirstCoordinate = isTrackAxisAlongFirstCoordinate(state);

		while (index != indexOf) {
			ResourceLocation id = bogeyCycle.get(index);
			Block newBlock = ForgeRegistries.BLOCKS.getValue(id);
			if (newBlock instanceof AbstractBogeyBlock<?> bogey) {
				BlockState matchingBogey = bogey.getMatchingBogey(bogeyUpDirection, trackAxisAlongFirstCoordinate);
				if (matchingBogey != null)
					return copyProperties(state, matchingBogey);
			}
			index = (index + 1) % bogeyCycle.size();
		}

		return state;
	}

	public BlockState getNextSize(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof AbstractBogeyBlockEntity sbbe)
			return this.getNextSize(sbbe);
		return level.getBlockState(pos);
	}

	/**
	 * List of BlockState Properties to copy between sizes
	 */
	public List<Property<?>> propertiesToCopy() {
		return ImmutableList.of(WATERLOGGED, AXIS);
	}

	// generic method needed to satisfy Property and BlockState's generic requirements
	private <V extends Comparable<V>> BlockState copyProperty(BlockState source, BlockState target, Property<V> property) {
		if (source.hasProperty(property) && target.hasProperty(property)) {
			return target.setValue(property, source.getValue(property));
		}
		return target;
	}

	private BlockState copyProperties(BlockState source, BlockState target) {
		for (Property<?> property : propertiesToCopy())
			target = copyProperty(source, target, property);
		return target;
	}

	public BlockState getNextSize(AbstractBogeyBlockEntity sbte) {
		BogeySizes.BogeySize size = this.getSize();
		BogeyStyle style = sbte.getStyle();
		BlockState nextBlock = style.getNextBlock(size).defaultBlockState();
		nextBlock = copyProperties(sbte.getBlockState(), nextBlock);
		return nextBlock;
	}

	public BlockState getStateOfSize(AbstractBogeyBlockEntity sbte, BogeySizes.BogeySize size) {
		BogeyStyle style = sbte.getStyle();
		BlockState state = style.getBlockOfSize(size).defaultBlockState();
		return copyProperties(sbte.getBlockState(), state);
	}

	public BogeyStyle getNextStyle(Level level, BlockPos pos) {
		BlockEntity te = level.getBlockEntity(pos);
		if (te instanceof AbstractBogeyBlockEntity sbbe)
			return this.getNextStyle(sbbe.getStyle());
		return getDefaultStyle();
	}

	public BogeyStyle getNextStyle(BogeyStyle style) {
		Collection<BogeyStyle> allStyles = style.getCycleGroup().values();
		if (allStyles.size() <= 1)
			return style;
		List<BogeyStyle> list = new ArrayList<>(allStyles);
		return Iterate.cycleValue(list, style);
	}


	@Override
	public @NotNull BlockState rotate(@NotNull BlockState pState, Rotation pRotation) {
		return switch (pRotation) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> pState.cycle(AXIS);
			default -> pState;
		};
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
		return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, AllBlocks.RAILWAY_CASING.asStack());
	}

	public boolean canBeUpsideDown() {
		return false;
	}

	public boolean isUpsideDown(BlockState state) {
		return false;
	}

	public BlockState getVersion(BlockState base, boolean upsideDown) {
		return base;
	}
}
