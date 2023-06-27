package com.simibubi.create.content.processing.burner;

import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlazeBurnerBlock extends HorizontalDirectionalBlock implements IBE<BlazeBurnerBlockEntity>, IWrenchable {

	public static final EnumProperty<HeatLevel> HEAT_LEVEL = EnumProperty.create("blaze", HeatLevel.class);

	public BlazeBurnerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(HEAT_LEVEL, HeatLevel.NONE));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HEAT_LEVEL, FACING);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		if (world.isClientSide)
			return;
		BlockEntity blockEntity = world.getBlockEntity(pos.above());
		if (!(blockEntity instanceof BasinBlockEntity))
			return;
		BasinBlockEntity basin = (BasinBlockEntity) blockEntity;
		basin.notifyChangeOfContents();
	}

	@Override
	public Class<BlazeBurnerBlockEntity> getBlockEntityClass() {
		return BlazeBurnerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends BlazeBurnerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.HEATER.get();
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (state.getValue(HEAT_LEVEL) == HeatLevel.NONE)
			return null;
		return IBE.super.newBlockEntity(pos, state);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult blockRayTraceResult) {
		ItemStack heldItem = player.getItemInHand(hand);
		HeatLevel heat = state.getValue(HEAT_LEVEL);

		if (AllItems.GOGGLES.isIn(heldItem) && heat != HeatLevel.NONE)
			return onBlockEntityUse(world, pos, bbte -> {
				if (bbte.goggles)
					return InteractionResult.PASS;
				bbte.goggles = true;
				bbte.notifyUpdate();
				return InteractionResult.SUCCESS;
			});

		if (heldItem.isEmpty() && heat != HeatLevel.NONE)
			return onBlockEntityUse(world, pos, bbte -> {
				if (!bbte.goggles)
					return InteractionResult.PASS;
				bbte.goggles = false;
				bbte.notifyUpdate();
				return InteractionResult.SUCCESS;
			});

		if (heat == HeatLevel.NONE) {
			if (heldItem.getItem() instanceof FlintAndSteelItem) {
				world.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
					world.random.nextFloat() * 0.4F + 0.8F);
				if (world.isClientSide)
					return InteractionResult.SUCCESS;
				heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
				world.setBlockAndUpdate(pos, AllBlocks.LIT_BLAZE_BURNER.getDefaultState());
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		boolean doNotConsume = player.isCreative();
		boolean forceOverflow = !(player instanceof FakePlayer);

		InteractionResultHolder<ItemStack> res =
			tryInsert(state, world, pos, heldItem, doNotConsume, forceOverflow, false);
		ItemStack leftover = res.getObject();
		if (!world.isClientSide && !doNotConsume && !leftover.isEmpty()) {
			if (heldItem.isEmpty()) {
				player.setItemInHand(hand, leftover);
			} else if (!player.getInventory()
				.add(leftover)) {
				player.drop(leftover, false);
			}
		}

		return res.getResult() == InteractionResult.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	public static InteractionResultHolder<ItemStack> tryInsert(BlockState state, Level world, BlockPos pos,
		ItemStack stack, boolean doNotConsume, boolean forceOverflow, boolean simulate) {
		if (!state.hasBlockEntity())
			return InteractionResultHolder.fail(ItemStack.EMPTY);

		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof BlazeBurnerBlockEntity))
			return InteractionResultHolder.fail(ItemStack.EMPTY);
		BlazeBurnerBlockEntity burnerBE = (BlazeBurnerBlockEntity) be;

		if (burnerBE.isCreativeFuel(stack)) {
			if (!simulate)
				burnerBE.applyCreativeFuel();
			return InteractionResultHolder.success(ItemStack.EMPTY);
		}
		if (!burnerBE.tryUpdateFuel(stack, forceOverflow, simulate))
			return InteractionResultHolder.fail(ItemStack.EMPTY);

		if (!doNotConsume) {
			ItemStack container = stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY;
			if (!world.isClientSide) {
				stack.shrink(1);
			}
			return InteractionResultHolder.success(container);
		}
		return InteractionResultHolder.success(ItemStack.EMPTY);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		ItemStack stack = context.getItemInHand();
		Item item = stack.getItem();
		BlockState defaultState = defaultBlockState();
		if (!(item instanceof BlazeBurnerBlockItem))
			return defaultState;
		HeatLevel initialHeat =
			((BlazeBurnerBlockItem) item).hasCapturedBlaze() ? HeatLevel.SMOULDERING : HeatLevel.NONE;
		return defaultState.setValue(HEAT_LEVEL, initialHeat)
			.setValue(FACING, context.getHorizontalDirection()
				.getOpposite());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		return AllShapes.HEATER_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockGetter p_220071_2_, BlockPos p_220071_3_,
		CollisionContext p_220071_4_) {
		if (p_220071_4_ == CollisionContext.empty())
			return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
		return getShape(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level p_180641_2_, BlockPos p_180641_3_) {
		return Math.max(0, state.getValue(HEAT_LEVEL)
			.ordinal() - 1);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
		if (random.nextInt(10) != 0)
			return;
		if (!state.getValue(HEAT_LEVEL)
			.isAtLeast(HeatLevel.SMOULDERING))
			return;
		world.playLocalSound((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F),
			(double) ((float) pos.getZ() + 0.5F), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
			0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
	}

	public static HeatLevel getHeatLevelOf(BlockState blockState) {
		return blockState.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) ? blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL)
			: HeatLevel.NONE;
	}

	public static int getLight(BlockState state) {
		HeatLevel level = state.getValue(HEAT_LEVEL);
		return switch (level) {
		case NONE -> 0;
		case SMOULDERING -> 8;
		default -> 15;
		};
	}

	public static LootTable.Builder buildLootTable() {
		LootItemCondition.Builder survivesExplosion = ExplosionCondition.survivesExplosion();
		BlazeBurnerBlock block = AllBlocks.BLAZE_BURNER.get();
		LootTable.Builder builder = LootTable.lootTable();
		LootPool.Builder poolBuilder = LootPool.lootPool();
		for (HeatLevel level : HeatLevel.values()) {
			ItemLike drop = level == HeatLevel.NONE ? AllItems.EMPTY_BLAZE_BURNER.get() : AllBlocks.BLAZE_BURNER.get();
			poolBuilder.add(LootItem.lootTableItem(drop)
				.when(survivesExplosion)
				.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
					.setProperties(StatePropertiesPredicate.Builder.properties()
						.hasProperty(HEAT_LEVEL, level))));
		}
		builder.withPool(poolBuilder.setRolls(ConstantValue.exactly(1)));
		return builder;
	}

	public enum HeatLevel implements StringRepresentable {
		NONE, SMOULDERING, FADING, KINDLED, SEETHING,;

		public static HeatLevel byIndex(int index) {
			return values()[index];
		}

		public HeatLevel nextActiveLevel() {
			return byIndex(ordinal() % (values().length - 1) + 1);
		}

		public boolean isAtLeast(HeatLevel heatLevel) {
			return this.ordinal() >= heatLevel.ordinal();
		}

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

}
