package com.simibubi.create.content.logistics.packagerLink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

public class PackagerLinkBlockEntity extends LinkWithBulbBlockEntity {

	public LogisticallyLinkedBehaviour behaviour;
	public UUID placedBy;

	public PackagerLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(10);
		placedBy = null;
	}

	public InventorySummary fetchSummaryFromPackager(@Nullable IdentifiedInventory ignoredHandler) {
		PackagerBlockEntity packager = getPackager();
		if (packager == null)
			return InventorySummary.EMPTY;
		if (packager.isTargetingSameInventory(ignoredHandler))
			return InventorySummary.EMPTY;
		return packager.getAvailableItems();
	}

	public void playEffect() {
		AllSoundEvents.STOCK_LINK.playAt(level, worldPosition, 0.75f, 1.25f, false);
		Vec3 vec3 = Vec3.atCenterOf(worldPosition);

		BlockState state = getBlockState();
		float f = 1;

		AttachFace face = state.getOptionalValue(PackagerLinkBlock.FACE)
			.orElse(AttachFace.FLOOR);
		if (face != AttachFace.FLOOR)
			f = -1;
		if (face == AttachFace.WALL)
			vec3 = vec3.add(0, 0.25, 0);

		vec3 = vec3.add(Vec3.atLowerCornerOf(state.getOptionalValue(PackagerLinkBlock.FACING)
				.orElse(Direction.SOUTH)
				.getNormal())
			.scale(f * 0.125));

		pulse();
		level.addParticle(new WiFiParticle.Data(), vec3.x, vec3.y, vec3.z, 1, face == AttachFace.CEILING ? -1 : 1, 1);
	}

	public Pair<PackagerBlockEntity, PackagingRequest> processRequest(ItemStack stack, int amount, String address,
		int linkIndex, MutableBoolean finalLink, int orderId, @Nullable PackageOrderWithCrafts context,
		@Nullable IdentifiedInventory ignoredHandler) {
		PackagerBlockEntity packager = getPackager();
		if (packager == null)
			return null;
		if (packager.isTargetingSameInventory(ignoredHandler))
			return null;

		InventorySummary summary = packager.getAvailableItems();
		int availableCount = summary.getCountOf(stack);
		if (availableCount == 0)
			return null;
		int toWithdraw = Math.min(amount, availableCount);
		return Pair.of(packager,
			PackagingRequest.create(stack, toWithdraw, address, linkIndex, finalLink, 0, orderId, context));
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		if (placedBy != null)
			tag.putUUID("PlacedBy", placedBy);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		placedBy = tag.contains("PlacedBy") ? tag.getUUID("PlacedBy") : null;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(behaviour = new LogisticallyLinkedBehaviour(this, true));
	}

	@Override
	public void initialize() {
		super.initialize();
		behaviour.redstonePowerChanged(PackagerLinkBlock.getPower(getBlockState(), level, worldPosition));
		PackagerBlockEntity packager = getPackager();
		if (packager != null)
			packager.recheckIfLinksPresent();
	}

	@Nullable
	public PackagerBlockEntity getPackager() {
		BlockState blockState = getBlockState();
		if (behaviour.redstonePower == 15)
			return null;
		BlockPos source = worldPosition.relative(PackagerLinkBlock.getConnectedDirection(blockState)
			.getOpposite());
		if (!(level.getBlockEntity(source) instanceof PackagerBlockEntity packager))
			return null;
		if (packager instanceof RepackagerBlockEntity)
			return null;
		return packager;
	}

	@Override
	public Direction getBulbFacing(BlockState state) {
		return PackagerLinkBlock.getConnectedDirection(state);
	}

	private static final Map<BlockState, Vec3> bulbOffsets = new HashMap<>();

	@Override
	public Vec3 getBulbOffset(BlockState state) {
		return bulbOffsets.computeIfAbsent(state, s -> {
			Vec3 offset = VecHelper.voxelSpace(5, 6, 11);
			Vec3 wallOffset = VecHelper.voxelSpace(11, 6, 5);
			AttachFace face = s.getValue(PackagerLinkBlock.FACE);
			Vec3 vec = face == AttachFace.WALL ? wallOffset : offset;
			float angle = AngleHelper.horizontalAngle(s.getValue(PackagerLinkBlock.FACING));
			if (face == AttachFace.CEILING)
				angle = -angle;
			if (face == AttachFace.WALL)
				angle = 0;
			return VecHelper.rotateCentered(vec, angle, Axis.Y);
		});
	}

}
