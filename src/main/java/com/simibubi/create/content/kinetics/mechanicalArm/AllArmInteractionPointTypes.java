package com.simibubi.create.content.kinetics.mechanicalArm;

import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.saw.SawBlock;
import com.simibubi.create.content.logistics.chute.AbstractChuteBlock;
import com.simibubi.create.content.logistics.funnel.AbstractFunnelBlock;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.content.logistics.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public class AllArmInteractionPointTypes {
	static {
		register("basin", new BasinType());
		register("belt", new BeltType());
		register("blaze_burner", new BlazeBurnerType());
		register("chute", new ChuteType());
		register("crafter", new CrafterType());
		register("crushing_wheels", new CrushingWheelsType());
		register("deployer", new DeployerType());
		register("depot", new DepotType());
		register("funnel", new FunnelType());
		register("millstone", new MillstoneType());
		register("packager", new PackagerType());
		register("saw", new SawType());

		register("campfire", new CampfireType());
		register("composter", new ComposterType());
		register("jukebox", new JukeboxType());
		register("respawn_anchor", new RespawnAnchorType());
	}

	private static <T extends ArmInteractionPointType> void register(String name, T type) {
		Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, Create.asResource(name), type);
	}

	@Internal
	public static void init() {
	}

	//

	public static class BasinType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return BasinBlock.isBasin(level, pos);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class BeltType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.BELT.has(state) && !(level.getBlockState(pos.above())
				.getBlock() instanceof BeltTunnelBlock) && BeltBlock.canTransportObjects(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new BeltPoint(this, level, pos, state);
		}
	}

	public static class BlazeBurnerType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.BLAZE_BURNER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new BlazeBurnerPoint(this, level, pos, state);
		}
	}

	public static class ChuteType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AbstractChuteBlock.isChute(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new TopFaceArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class CrafterType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_CRAFTER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new CrafterPoint(this, level, pos, state);
		}
	}

	public static class CrushingWheelsType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new CrushingWheelPoint(this, level, pos, state);
		}
	}

	public static class DeployerType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.DEPLOYER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new DeployerPoint(this, level, pos, state);
		}
	}

	public static class DepotType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.DEPOT.has(state) || AllBlocks.WEIGHTED_EJECTOR.has(state)
				|| AllBlocks.TRACK_STATION.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new DepotPoint(this, level, pos, state);
		}
	}

	public static class FunnelType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof AbstractFunnelBlock
				&& !(state.hasProperty(FunnelBlock.EXTRACTING) && state.getValue(FunnelBlock.EXTRACTING))
				&& !(state.hasProperty(BeltFunnelBlock.SHAPE)
				&& state.getValue(BeltFunnelBlock.SHAPE) == Shape.PUSHING);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new FunnelPoint(this, level, pos, state);
		}
	}

	public static class MillstoneType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.MILLSTONE.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class PackagerType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.PACKAGER.has(state) || AllBlocks.REPACKAGER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class SawType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_SAW.has(state) && state.getValue(SawBlock.FACING) == Direction.UP
				&& ((KineticBlockEntity) level.getBlockEntity(pos)).getSpeed() != 0;
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new DepotPoint(this, level, pos, state);
		}
	}

	public static class CampfireType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof CampfireBlock;
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new CampfirePoint(this, level, pos, state);
		}
	}

	public static class ComposterType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.is(Blocks.COMPOSTER);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ComposterPoint(this, level, pos, state);
		}
	}

	public static class JukeboxType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.is(Blocks.JUKEBOX);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new JukeboxPoint(this, level, pos, state);
		}
	}

	public static class RespawnAnchorType extends ArmInteractionPointType {
		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.is(Blocks.RESPAWN_ANCHOR);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new RespawnAnchorPoint(this, level, pos, state);
		}
	}

	//

	public static class DepositOnlyArmInteractionPoint extends ArmInteractionPoint {
		public DepositOnlyArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos,
											  BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public void cycleMode() {
		}

		@Override
		public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotCount(ArmBlockEntity armBlockEntity) {
			return 0;
		}
	}

	public static class TopFaceArmInteractionPoint extends ArmInteractionPoint {
		public TopFaceArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos)
				.add(.5f, 1, .5f);
		}
	}

	public static class BeltPoint extends DepotPoint {
		public BeltPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public void keepAlive() {
			super.keepAlive();
			BeltBlockEntity beltBE = BeltHelper.getSegmentBE(level, pos);
			if (beltBE == null)
				return;
			TransportedItemStackHandlerBehaviour transport =
				beltBE.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
			if (transport == null)
				return;
			MutableBoolean found = new MutableBoolean(false);
			transport.handleProcessingOnAllItems(tis -> {
				if (found.isTrue())
					return TransportedResult.doNothing();
				tis.lockedExternally = true;
				found.setTrue();
				return TransportedResult.doNothing();
			});
		}
	}

	public static class BlazeBurnerPoint extends DepositOnlyArmInteractionPoint {
		public BlazeBurnerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
			ItemStack input = stack.copy();
			InteractionResultHolder<ItemStack> res =
				BlazeBurnerBlock.tryInsert(cachedState, level, pos, input, false, false, simulate);
			ItemStack remainder = res.getObject();
			if (input.isEmpty()) {
				return remainder;
			} else {
				if (!simulate)
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
				return input;
			}
		}
	}

	public static class CrafterPoint extends ArmInteractionPoint {
		public CrafterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Direction getInteractionDirection() {
			return cachedState.getOptionalValue(MechanicalCrafterBlock.HORIZONTAL_FACING)
				.orElse(Direction.SOUTH)
				.getOpposite();
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return super.getInteractionPositionVector().add(Vec3.atLowerCornerOf(getInteractionDirection().getNormal())
				.scale(.5f));
		}

		@Override
		public void updateCachedState() {
			BlockState oldState = cachedState;
			super.updateCachedState();
			if (oldState != cachedState)
				cachedAngles = null;
		}

		@Override
		public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
			BlockEntity be = level.getBlockEntity(pos);
			if (!(be instanceof MechanicalCrafterBlockEntity crafter))
				return ItemStack.EMPTY;
			SmartInventory inventory = crafter.getInventory();
			inventory.allowExtraction();
			ItemStack extract = super.extract(armBlockEntity, slot, amount, simulate);
			inventory.forbidExtraction();
			return extract;
		}
	}

	public static class DeployerPoint extends ArmInteractionPoint {
		public DeployerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Direction getInteractionDirection() {
			return cachedState.getOptionalValue(DeployerBlock.FACING)
				.orElse(Direction.UP)
				.getOpposite();
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return super.getInteractionPositionVector().add(Vec3.atLowerCornerOf(getInteractionDirection().getNormal())
				.scale(.65f));
		}

		@Override
		public void updateCachedState() {
			BlockState oldState = cachedState;
			super.updateCachedState();
			if (oldState != cachedState)
				cachedAngles = null;
		}
	}

	public static class DepotPoint extends ArmInteractionPoint {
		public DepotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos)
				.add(.5f, 14 / 16f, .5f);
		}
	}

	public static class FunnelPoint extends DepositOnlyArmInteractionPoint {
		public FunnelPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			Direction funnelFacing = FunnelBlock.getFunnelFacing(cachedState);
			Vec3i normal = funnelFacing != null ? funnelFacing.getNormal() : Vec3i.ZERO;
			return VecHelper.getCenterOf(pos)
				.add(Vec3.atLowerCornerOf(normal)
					.scale(-.15f));
		}

		@Override
		protected Direction getInteractionDirection() {
			Direction funnelFacing = FunnelBlock.getFunnelFacing(cachedState);
			return funnelFacing != null ? funnelFacing.getOpposite() : Direction.UP;
		}

		@Override
		public void updateCachedState() {
			BlockState oldState = cachedState;
			super.updateCachedState();
			if (oldState != cachedState)
				cachedAngles = null;
		}

		@Override
		public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
			FilteringBehaviour filtering = BlockEntityBehaviour.get(level, pos, FilteringBehaviour.TYPE);
			InvManipulationBehaviour inserter = BlockEntityBehaviour.get(level, pos, InvManipulationBehaviour.TYPE);
			if (cachedState.getOptionalValue(BlockStateProperties.POWERED)
				.orElse(false))
				return stack;
			if (inserter == null)
				return stack;
			if (filtering != null && !filtering.test(stack))
				return stack;
			if (simulate)
				inserter.simulate();
			ItemStack insert = inserter.insert(stack);
			if (!simulate && insert.getCount() != stack.getCount()) {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof FunnelBlockEntity funnelBlockEntity) {
					funnelBlockEntity.onTransfer(stack);
					if (funnelBlockEntity.hasFlap())
						funnelBlockEntity.flap(true);
				}
			}
			return insert;
		}
	}

	public static class CampfirePoint extends DepositOnlyArmInteractionPoint {
		public CampfirePoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof CampfireBlockEntity campfireBE))
				return stack;
			Optional<RecipeHolder<CampfireCookingRecipe>> recipe = campfireBE.getCookableRecipe(stack);
			if (recipe.isEmpty())
				return stack;
			if (simulate) {
				boolean hasSpace = false;
				for (ItemStack campfireStack : campfireBE.getItems()) {
					if (campfireStack.isEmpty()) {
						hasSpace = true;
						break;
					}
				}
				if (!hasSpace)
					return stack;
				ItemStack remainder = stack.copy();
				remainder.shrink(1);
				return remainder;
			}
			ItemStack remainder = stack.copy();
			campfireBE.placeFood(null, remainder, recipe.get().value()
				.getCookingTime());
			return remainder;
		}
	}

	public static class ComposterPoint extends ArmInteractionPoint {
		public ComposterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos)
				.add(.5f, 13 / 16f, .5f);
		}

		@Override
		public void updateCachedState() {
			BlockState oldState = cachedState;
			super.updateCachedState();
			if (cachedHandler != null && oldState != cachedState)
				level.invalidateCapabilities(cachedHandler.pos());
		}

		@Nullable
		@Override
		protected IItemHandler getHandler(ArmBlockEntity armBlockEntity) {
			return null;
		}

		protected WorldlyContainer getContainer() {
			ComposterBlock composterBlock = (ComposterBlock) Blocks.COMPOSTER;
			return composterBlock.getContainer(cachedState, level, pos);
		}

		@Override
		public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
			IItemHandler handler = new SidedInvWrapper(getContainer(), Direction.UP);
			return ItemHandlerHelper.insertItem(handler, stack, simulate);
		}

		@Override
		public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
			IItemHandler handler = new SidedInvWrapper(getContainer(), Direction.DOWN);
			return handler.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotCount(ArmBlockEntity armBlockEntity) {
			return 2;
		}
	}

	public static class JukeboxPoint extends TopFaceArmInteractionPoint {
		public JukeboxPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public int getSlotCount(ArmBlockEntity armBlockEntity) {
			return 1;
		}

		@Override
		public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
			if (stack.get(DataComponents.JUKEBOX_PLAYABLE) == null)
				return stack;
			if (cachedState.getOptionalValue(JukeboxBlock.HAS_RECORD).orElse(true))
				return stack;
			if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukeboxBE))
				return stack;
			if (!jukeboxBE.getTheItem().isEmpty())
				return stack;
			ItemStack remainder = stack.copy();
			ItemStack toInsert = remainder.split(1);
			if (!simulate)
				jukeboxBE.setTheItem(toInsert);
			return remainder;
		}

		@Override
		public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
			if (!cachedState.getOptionalValue(JukeboxBlock.HAS_RECORD).orElse(false))
				return ItemStack.EMPTY;
			if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukeboxBE))
				return ItemStack.EMPTY;
			if (!simulate)
				return jukeboxBE.removeItem(slot, amount);
			return jukeboxBE.getTheItem();
		}
	}

	public static class RespawnAnchorPoint extends DepositOnlyArmInteractionPoint {
		public RespawnAnchorPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos)
				.add(.5f, 1, .5f);
		}

		@Override
		public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
			if (!stack.is(Items.GLOWSTONE))
				return stack;
			if (cachedState.getOptionalValue(RespawnAnchorBlock.CHARGE)
				.orElse(4) == 4)
				return stack;
			if (!simulate)
				RespawnAnchorBlock.charge(null, level, pos, cachedState);
			ItemStack remainder = stack.copy();
			remainder.shrink(1);
			return remainder;
		}
	}

	public static class CrushingWheelPoint extends DepositOnlyArmInteractionPoint {
		public CrushingWheelPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos)
				.add(.5f, 1, .5f);
		}
	}
}
