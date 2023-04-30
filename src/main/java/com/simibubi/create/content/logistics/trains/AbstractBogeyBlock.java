package com.simibubi.create.content.logistics.trains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllBogeyStyles;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;
import com.simibubi.create.content.logistics.trains.track.StandardBogeyTileEntity;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.block.ITE;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractBogeyBlock extends Block implements ITE<StandardBogeyTileEntity>, ProperWaterloggedBlock, ISpecialBlockItemRequirement, IWrenchable {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	static final List<ResourceLocation> BOGEYS = new ArrayList<>();
	public BogeySizes.BogeySize size;


	public AbstractBogeyBlock(Properties pProperties, BogeySizes.BogeySize size) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
		this.size = size;
	}

	public static void register(ResourceLocation block) {
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

	public abstract Vec3 getConnectorAnchorOffset();

	public boolean allowsSingleBogeyCarriage() {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void render(@Nullable BlockState state, float wheelAngle, PoseStack ms, float partialTicks,
		MultiBufferSource buffers, int light, int overlay, StandardBogeyTileEntity sbte) {
		BogeyStyle style = sbte.getStyle();
		final Optional<BogeyRenderer.CommonRenderer> commonRenderer
				= style.getNewCommonRenderInstance();
		final BogeyRenderer renderer = style.getInWorldRenderInstance(this.getSize());
		if (state != null) {
			ms.translate(.5f, .5f, .5f);
			if (state.getValue(AXIS) == Direction.Axis.X)
				ms.mulPose(Vector3f.YP.rotationDegrees(90));
		}
		ms.translate(0, -1.5 - 1 / 128f, 0);
		VertexConsumer vb = buffers.getBuffer(RenderType.cutoutMipped());
		renderer.render(sbte.getBogeyData(), wheelAngle, ms, light, vb);
		commonRenderer.ifPresent(common ->
				common.render(sbte.getBogeyData(), wheelAngle, ms, light, vb));
	}

	public BogeySizes.BogeySize getSize() {
		return this.size;
	};

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
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
								 BlockHitResult hit) {
		if (level.isClientSide)
			return InteractionResult.PASS;
		ItemStack stack = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && stack.is(AllItems.WRENCH.get()) && !player.getCooldowns().isOnCooldown(stack.getItem())
				&& AllBogeyStyles.BOGEY_STYLES.size() > 1) {
			Collection<BogeyStyle> styles = AllBogeyStyles.BOGEY_STYLES.values();

			if (styles.size() <= 1)
				return InteractionResult.PASS;

			BlockEntity be = level.getBlockEntity(pos);

			if (!(be instanceof StandardBogeyTileEntity sbte))
				return InteractionResult.FAIL;

			player.getCooldowns().addCooldown(stack.getItem(), 20);
			BogeyStyle currentStyle = sbte.getStyle();
			BogeySizes.BogeySize size = getSize();

			BogeyStyle style = this.getNextStyle(currentStyle);
			Set<BogeySizes.BogeySize> validSizes = style.validSizes();

			for (int i = 0; i < BogeySizes.count(); i++) {
				if (validSizes.contains(size)) break;
				size = size.increment();
			}

			sbte.setBogeyStyle(style);

			if (size == getSize()) {
				player.displayClientMessage(Lang.translateDirect("create.bogey.style.updated_style"), true);
			} else {
				CompoundTag oldData = sbte.getBogeyData();
				level.setBlock(pos, this.getStateOfSize(sbte, size), 3);
				BlockEntity newBlockEntity = level.getBlockEntity(pos);
				if (!(newBlockEntity instanceof StandardBogeyTileEntity newTileEntity))
					return InteractionResult.FAIL;
				newTileEntity.setBogeyData(oldData);
				player.displayClientMessage(Lang.translateDirect("create.bogey.style.updated_style_and_size"), true);
			}
			return InteractionResult.CONSUME;
		}

		return InteractionResult.PASS;
	}


	@Override
	public BlockState getRotatedBlockState(BlockState state, Direction targetedFace) {
		Block block = state.getBlock();
		int indexOf = BOGEYS.indexOf(RegisteredObjects.getKeyOrThrow(block));
		if (indexOf == -1)
			return state;
		int index = (indexOf + 1) % BOGEYS.size();
		Direction bogeyUpDirection = getBogeyUpDirection();
		boolean trackAxisAlongFirstCoordinate = isTrackAxisAlongFirstCoordinate(state);

		while (index != indexOf) {
			ResourceLocation id = BOGEYS.get(index);
			Block newBlock = ForgeRegistries.BLOCKS.getValue(id);
			if (newBlock instanceof AbstractBogeyBlock bogey) {
				BlockState matchingBogey = bogey.getMatchingBogey(bogeyUpDirection, trackAxisAlongFirstCoordinate);
				if (matchingBogey != null)
					return matchingBogey.hasProperty(WATERLOGGED)
						? matchingBogey.setValue(WATERLOGGED, state.getValue(WATERLOGGED))
						: matchingBogey;
			}
			index = (index + 1) % BOGEYS.size();
		}

		return state;
	}

	public BlockState getNextSize(Level level, BlockPos pos) {
		BlockEntity te = level.getBlockEntity(pos);
		if (te instanceof StandardBogeyTileEntity sbte)
			return this.getNextSize(sbte);
		return level.getBlockState(pos);
	}

	public BlockState getNextSize(StandardBogeyTileEntity sbte) {
		BogeySizes.BogeySize size = this.getSize();
		BogeyStyle style = sbte.getStyle();
		BlockState nextBlock = style.getNextBlock(size).defaultBlockState();
		return nextBlock.hasProperty(WATERLOGGED)
				? nextBlock.setValue(WATERLOGGED, sbte.getBlockState().getValue(WATERLOGGED))
				: nextBlock;
	}

	public BlockState getStateOfSize(StandardBogeyTileEntity sbte, BogeySizes.BogeySize size) {
		BogeyStyle style = sbte.getStyle();
		BlockState state = style.getBlockOfSize(size).defaultBlockState();
		return state.hasProperty(WATERLOGGED)
				? state.setValue(WATERLOGGED, sbte.getBlockState().getValue(WATERLOGGED))
				: state;
	}

	public BogeyStyle getNextStyle(Level level, BlockPos pos) {
		BlockEntity te = level.getBlockEntity(pos);
		if (te instanceof StandardBogeyTileEntity sbte)
			return this.getNextStyle(sbte.getStyle());
		return AllBogeyStyles.STANDARD;
	}

	public BogeyStyle getNextStyle(BogeyStyle style) {
		Collection<BogeyStyle> allStyles = AllBogeyStyles.BOGEY_STYLES.values();
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
}
