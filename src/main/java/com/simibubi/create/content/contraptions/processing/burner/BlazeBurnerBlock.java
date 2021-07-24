package com.simibubi.create.content.contraptions.processing.burner;

import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Lang;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.ILootCondition.IBuilder;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlazeBurnerBlock extends Block implements ITE<BlazeBurnerTileEntity> {

	public static final EnumProperty<HeatLevel> HEAT_LEVEL = EnumProperty.create("blaze", HeatLevel.class);

	public BlazeBurnerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(HEAT_LEVEL, HeatLevel.NONE));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HEAT_LEVEL);
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		if (world.isClientSide)
			return;
		TileEntity tileEntity = world.getBlockEntity(pos.above());
		if (!(tileEntity instanceof BasinTileEntity))
			return;
		BasinTileEntity basin = (BasinTileEntity) tileEntity;
		basin.notifyChangeOfContents();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return state.getValue(HEAT_LEVEL)
			.isAtLeast(HeatLevel.SMOULDERING);
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> list) {
		list.add(AllItems.EMPTY_BLAZE_BURNER.asStack());
		super.fillItemCategory(group, list);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.HEATER.create();
	}

	@Override
	public Class<BlazeBurnerTileEntity> getTileEntityClass() {
		return BlazeBurnerTileEntity.class;
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult blockRayTraceResult) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (!state.hasTileEntity()) {
			if (heldItem.getItem() instanceof FlintAndSteelItem) {
				world.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
					world.random.nextFloat() * 0.4F + 0.8F);
				if (world.isClientSide)
					return ActionResultType.SUCCESS;
				heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
				world.setBlockAndUpdate(pos, AllBlocks.LIT_BLAZE_BURNER.getDefaultState());
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.PASS;
		}

		boolean doNotConsume = player.isCreative();
		boolean forceOverflow = !(player instanceof FakePlayer);

		ActionResult<ItemStack> res = tryInsert(state, world, pos, heldItem, doNotConsume, forceOverflow, false);
		ItemStack leftover = res.getObject();
		if (!world.isClientSide && !doNotConsume && !leftover.isEmpty()) {
			if (heldItem.isEmpty()) {
				player.setItemInHand(hand, leftover);
			} else if (!player.inventory.add(leftover)) {
				player.drop(leftover, false);
			}
		}

		return res.getResult() == ActionResultType.SUCCESS ? ActionResultType.SUCCESS : ActionResultType.PASS;
	}

	public static ActionResult<ItemStack> tryInsert(BlockState state, World world, BlockPos pos, ItemStack stack, boolean doNotConsume,
		boolean forceOverflow, boolean simulate) {
		if (!state.hasTileEntity())
			return ActionResult.fail(ItemStack.EMPTY);

		TileEntity te = world.getBlockEntity(pos);
		if (!(te instanceof BlazeBurnerTileEntity))
			return ActionResult.fail(ItemStack.EMPTY);
		BlazeBurnerTileEntity burnerTE = (BlazeBurnerTileEntity) te;

		if (burnerTE.isCreativeFuel(stack)) {
			if (!simulate)
				burnerTE.applyCreativeFuel();
			return ActionResult.success(ItemStack.EMPTY);
		}
		if (!burnerTE.tryUpdateFuel(stack, forceOverflow, simulate))
			return ActionResult.fail(ItemStack.EMPTY);

		if (!doNotConsume) {
			ItemStack container = stack.getContainerItem();
			if (!world.isClientSide && !simulate) {
				stack.shrink(1);
			}
			if (!container.isEmpty()) {
				return ActionResult.success(container);
			}
		}
		return ActionResult.success(ItemStack.EMPTY);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		ItemStack stack = context.getItemInHand();
		Item item = stack.getItem();
		BlockState defaultState = defaultBlockState();
		if (!(item instanceof BlazeBurnerBlockItem))
			return defaultState;
		HeatLevel initialHeat =
			((BlazeBurnerBlockItem) item).hasCapturedBlaze() ? HeatLevel.SMOULDERING : HeatLevel.NONE;
		return defaultState.setValue(HEAT_LEVEL, initialHeat);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		return AllShapes.HEATER_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState p_220071_1_, IBlockReader p_220071_2_, BlockPos p_220071_3_,
		ISelectionContext p_220071_4_) {
		if (p_220071_4_ == ISelectionContext.empty())
			return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
		return getShape(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, World p_180641_2_, BlockPos p_180641_3_) {
		return Math.max(0, state.getValue(HEAT_LEVEL).ordinal() - 1);
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(10) != 0)
			return;
		if (!state.getValue(HEAT_LEVEL)
			.isAtLeast(HeatLevel.SMOULDERING))
			return;
		world.playLocalSound((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F),
			(double) ((float) pos.getZ() + 0.5F), SoundEvents.CAMPFIRE_CRACKLE, SoundCategory.BLOCKS,
			0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
	}

	public static HeatLevel getHeatLevelOf(BlockState blockState) {
		return blockState.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) ? blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL)
			: HeatLevel.NONE;
	}

	public static int getLight(BlockState state) {
		return MathHelper.clamp(state.getValue(HEAT_LEVEL)
			.ordinal() * 4 - 1, 0, 15);
	}

	public static LootTable.Builder buildLootTable() {
		IBuilder survivesExplosion = SurvivesExplosion.survivesExplosion();
		BlazeBurnerBlock block = AllBlocks.BLAZE_BURNER.get();

		LootTable.Builder builder = LootTable.lootTable();
		LootPool.Builder poolBuilder = LootPool.lootPool();
		for (HeatLevel level : HeatLevel.values()) {
			IItemProvider drop =
				level == HeatLevel.NONE ? AllItems.EMPTY_BLAZE_BURNER.get() : AllBlocks.BLAZE_BURNER.get();
			poolBuilder.add(ItemLootEntry.lootTableItem(drop)
				.when(survivesExplosion)
				.when(BlockStateProperty.hasBlockStateProperties(block)
					.setProperties(StatePropertiesPredicate.Builder.properties()
						.hasProperty(HEAT_LEVEL, level))));
		}
		builder.withPool(poolBuilder.setRolls(ConstantRange.exactly(1)));
		return builder;
	}

	public enum HeatLevel implements IStringSerializable {
		NONE, SMOULDERING, FADING, KINDLED, SEETHING, ;

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
