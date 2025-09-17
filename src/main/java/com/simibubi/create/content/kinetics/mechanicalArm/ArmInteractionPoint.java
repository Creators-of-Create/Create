package com.simibubi.create.content.kinetics.mechanicalArm;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.contraptions.StructureTransform;

import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ArmInteractionPoint {

	protected final ArmInteractionPointType type;
	protected Level level;
	protected final BlockPos pos;
	protected Mode mode = Mode.DEPOSIT;

	protected BlockState cachedState;
	protected BlockCapabilityCache<IItemHandler, Direction> cachedHandler;
	protected ArmAngleTarget cachedAngles;

	public ArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
		this.type = type;
		this.level = level;
		this.pos = pos;
		this.cachedState = state;
	}

	public ArmInteractionPointType getType() {
		return type;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public BlockPos getPos() {
		return pos;
	}

	public Mode getMode() {
		return mode;
	}

	public void cycleMode() {
		mode = mode == Mode.DEPOSIT ? Mode.TAKE : Mode.DEPOSIT;
	}

	protected Vec3 getInteractionPositionVector() {
		return VecHelper.getCenterOf(pos);
	}

	protected Direction getInteractionDirection() {
		return Direction.DOWN;
	}

	public ArmAngleTarget getTargetAngles(BlockPos armPos, boolean ceiling) {
		if (cachedAngles == null)
			cachedAngles =
				new ArmAngleTarget(armPos, getInteractionPositionVector(), getInteractionDirection(), ceiling);

		return cachedAngles;
	}

	public void updateCachedState() {
		cachedState = level.getBlockState(pos);
	}

	public boolean isValid() {
		updateCachedState();
		return type.canCreatePoint(level, pos, cachedState);
	}

	public void keepAlive() {
	}

	@Nullable
	protected IItemHandler getHandler(ArmBlockEntity armBlockEntity) {
		if (cachedHandler == null && level instanceof ServerLevel serverLevel) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be == null)
				return null;
			cachedHandler = BlockCapabilityCache.create(
				Capabilities.ItemHandler.BLOCK,
				serverLevel,
				pos,
				Direction.UP,
				() -> !armBlockEntity.isRemoved(),
				() -> cachedHandler = null
			);
		}
		return cachedHandler.getCapability();
	}

	public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
		IItemHandler handler = getHandler(armBlockEntity);
		if (handler == null)
			return stack;
		return ItemHandlerHelper.insertItem(handler, stack, simulate);
	}

	public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
		IItemHandler handler = getHandler(armBlockEntity);
		if (handler == null)
			return ItemStack.EMPTY;
		return handler.extractItem(slot, amount, simulate);
	}

	public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, boolean simulate) {
		return extract(armBlockEntity, slot, 64, simulate);
	}

	public int getSlotCount(ArmBlockEntity armBlockEntity) {
		IItemHandler handler = getHandler(armBlockEntity);
		if (handler == null)
			return 0;
		return handler.getSlots();
	}

	protected void serialize(CompoundTag nbt, BlockPos anchor) {
		NBTHelper.writeEnum(nbt, "Mode", mode);
	}

	protected void deserialize(CompoundTag nbt, BlockPos anchor) {
		mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
	}

	public final CompoundTag serialize(BlockPos anchor) {
		ResourceLocation key = CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.getKey(type);
		if (key == null)
			throw new IllegalArgumentException("Could not get id for ArmInteractionPointType " + type + "!");

		CompoundTag nbt = new CompoundTag();
		nbt.putString("Type", key.toString());
		nbt.put("Pos", NbtUtils.writeBlockPos(pos.subtract(anchor)));
		serialize(nbt, anchor);
		return nbt;
	}

	@Nullable
	public static ArmInteractionPoint deserialize(CompoundTag nbt, Level level, BlockPos anchor) {
		ResourceLocation id = ResourceLocation.tryParse(nbt.getString("Type"));
		if (id == null)
			return null;
		ArmInteractionPointType type = CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.get(id);
		if (type == null)
			return null;
		BlockPos pos = NBTHelper.readBlockPos(nbt, "Pos").offset(anchor);
		BlockState state = level.getBlockState(pos);
		if (!type.canCreatePoint(level, pos, state))
			return null;
		ArmInteractionPoint point = type.createPoint(level, pos, state);
		if (point == null)
			return null;
		point.deserialize(nbt, anchor);
		return point;
	}

	public static void transformPos(CompoundTag nbt, StructureTransform transform) {
		BlockPos pos = NBTHelper.readBlockPos(nbt, "Pos");
		pos = transform.applyWithoutOffset(pos);
		nbt.put("Pos", NbtUtils.writeBlockPos(pos));
	}

	public static boolean isInteractable(Level level, BlockPos pos, BlockState state) {
		return ArmInteractionPointType.getPrimaryType(level, pos, state) != null;
	}

	@Nullable
	public static ArmInteractionPoint create(Level level, BlockPos pos, BlockState state) {
		ArmInteractionPointType type = ArmInteractionPointType.getPrimaryType(level, pos, state);
		if (type == null)
			return null;
		return type.createPoint(level, pos, state);
	}

	public enum Mode {
		DEPOSIT("mechanical_arm.deposit_to", 0xDDC166),
		TAKE("mechanical_arm.extract_from", 0x7FCDE0);

		private final String translationKey;
		private final int color;

		Mode(String translationKey, int color) {
			this.translationKey = translationKey;
			this.color = color;
		}

		public String getTranslationKey() {
			return translationKey;
		}

		public int getColor() {
			return color;
		}
	}

}
