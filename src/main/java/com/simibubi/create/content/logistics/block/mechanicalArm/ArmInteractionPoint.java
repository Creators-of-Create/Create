package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.fabricators_of_create.porting_lib.extensions.LevelExtensions;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.server.level.ServerLevel;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ArmInteractionPoint {

	protected final ArmInteractionPointType type;
	protected Level level;
	protected final BlockPos pos;
	protected Mode mode = Mode.DEPOSIT;

	protected BlockState cachedState;
	protected Map<Level, BlockApiCache<Storage<ItemVariant>, Direction>> handlerCaches = new IdentityHashMap<>();
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

	public void keepAlive() {}

	@Nullable
	protected Storage<ItemVariant> getHandler() {
		if (!cachedHandler.isPresent()) {
			BlockEntity te = level.getBlockEntity(pos);
			if (te == null)
				return null;
			cachedHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
		}
		return cachedHandler.orElse(null);
	}

	// a
	@Nullable
	protected Storage<ItemVariant> getHandler(Level world) {
		BlockApiCache<Storage<ItemVariant>, Direction> cache = getHandlerCache(world);
		return cache == null ? null : cache.find(Direction.UP);
	}

	protected BlockApiCache<Storage<ItemVariant>, Direction> getHandlerCache(Level level) {
		return handlerCaches.computeIfAbsent(level, $ -> TransferUtil.getItemCache(level, pos));
	}

	public ItemStack insert(ItemStack stack, TransactionContext ctx) {
		Storage<ItemVariant> handler = getHandler();
		if (handler == null)
			return stack;
		long inserted = handler.insert(ItemVariant.of(stack), stack.getCount(), ctx);
		return ItemHandlerHelper.copyStackWithSize(stack, (int) (stack.getCount() - inserted));
	}

	public ItemStack extract(int amount, TransactionContext ctx) {
		Storage<ItemVariant> handler = getHandler();
		if (handler == null)
			return ItemStack.EMPTY;
		return TransferUtil.extractAnyItem(handler, amount);
	}

	public ItemStack extract(Level world, int slot, boolean simulate) {
		return extract(world, slot, 64, simulate);
	}

	protected ItemStack extract(Level world, TransactionContext ctx) {
		return extract(world, 64, ctx);
	}

	protected void serialize(CompoundTag nbt, BlockPos anchor) {
		NBTHelper.writeEnum(nbt, "Mode", mode);
	}

	protected void deserialize(CompoundTag nbt, BlockPos anchor) {
		mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
	}

	public final CompoundTag serialize(BlockPos anchor) {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("Type", type.getId().toString());
		nbt.put("Pos", NbtUtils.writeBlockPos(pos.subtract(anchor)));
		serialize(nbt, anchor);
		return nbt;
	}

	@Nullable
	public static ArmInteractionPoint deserialize(CompoundTag nbt, Level level, BlockPos anchor) {
		ResourceLocation id = ResourceLocation.tryParse(nbt.getString("Type"));
		if (id == null)
			return null;
		ArmInteractionPointType type = ArmInteractionPointType.get(id);
		if (type == null)
			return null;
		BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("Pos")).offset(anchor);
		ArmInteractionPoint point = type.createPoint(level, pos, level.getBlockState(pos));
		if (point == null)
			return null;
		point.deserialize(nbt, anchor);
		return point;
	}

	public static void transformPos(CompoundTag nbt, StructureTransform transform) {
		BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("Pos"));
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
		DEPOSIT("mechanical_arm.deposit_to", ChatFormatting.GOLD, 0xFFCB74),
		TAKE("mechanical_arm.extract_from", ChatFormatting.AQUA, 0x4F8A8B);

		private final String translationKey;
		private final ChatFormatting chatColor;
		private final int color;

		Mode(String translationKey, ChatFormatting chatColor, int color) {
			this.translationKey = translationKey;
			this.chatColor = chatColor;
			this.color = color;
		}

		public String getTranslationKey() {
			return translationKey;
		}

		public ChatFormatting getChatColor() {
			return chatColor;
		}

		public int getColor() {
			return color;
		}
	}

}
