package com.simibubi.create.content.contraptions.minecart.capability;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.minecart.CouplingHandler;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Extended code for Minecarts, this allows for handling stalled carts and
 * coupled trains
 */
public class MinecartController implements INBTSerializable<CompoundTag> {
	public static final MinecartController EMPTY = new MinecartController.Empty();

	public static final IAttachmentSerializer<CompoundTag, MinecartController> SERIALIZER = Type.SERIALIZER;

	private boolean needsEntryRefresh;
	private WeakReference<AbstractMinecart> weakRef;

	/*
	 * Stall information, <Internal (waiting couplings), External (stalled
	 * contraptions)>
	 */
	private Couple<Optional<StallData>> stallData;

	/*
	 * Coupling information, <Main (helmed by this cart), Connected (handled by
	 * other cart)>
	 */
	private Couple<Optional<CouplingData>> couplings;

	public MinecartController(AbstractMinecart minecart) {
		weakRef = new WeakReference<>(minecart);
		stallData = Couple.create(Optional::empty);
		couplings = Couple.create(Optional::empty);
		needsEntryRefresh = true;
	}

	public final boolean isEmpty() {
		return getType() == Type.EMPTY;
	}

	@NotNull
	protected Type getType() {
		return Type.NORMAL;
	}

	public void tick() {
		AbstractMinecart cart = cart();
		Level world = getWorld();

		if (cart == null || world == null)
			return;

		if (needsEntryRefresh) {
			CapabilityMinecartController.queuedAdditions.get(world).add(cart);
			needsEntryRefresh = false;
		}

		stallData.forEach(opt -> opt.ifPresent(sd -> sd.tick(cart)));

		MutableBoolean internalStall = new MutableBoolean(false);
		couplings.forEachWithContext((opt, main) -> opt.ifPresent(cd -> {

			UUID idOfOther = cd.idOfCart(!main);
			MinecartController otherCart = CapabilityMinecartController.getIfPresent(world, idOfOther);
			internalStall.setValue(
				internalStall.booleanValue() || otherCart == null || !otherCart.isPresent() || otherCart.isStalled(false));

		}));
		if (!world.isClientSide) {
			setStalled(internalStall.booleanValue(), true);
			disassemble(cart);
		}
	}

	private void disassemble(AbstractMinecart cart) {
		if (cart instanceof Minecart) {
			return;
		}
		List<Entity> passengers = cart.getPassengers();
		if (passengers.isEmpty() || !(passengers.getFirst() instanceof AbstractContraptionEntity)) {
			return;
		}
		Level world = cart.level();
		int i = Mth.floor(cart.getX());
		int j = Mth.floor(cart.getY());
		int k = Mth.floor(cart.getZ());
		if (world.getBlockState(new BlockPos(i, j - 1, k))
			.is(BlockTags.RAILS)) {
			--j;
		}
		BlockPos blockpos = new BlockPos(i, j, k);
		BlockState blockstate = world.getBlockState(blockpos);
		if (cart.canUseRail() && blockstate.is(BlockTags.RAILS)
			&& blockstate.getBlock() instanceof PoweredRailBlock
			&& ((PoweredRailBlock) blockstate.getBlock())
			.isActivatorRail()) {
			if (cart.isVehicle()) {
				cart.ejectPassengers();
			}

			if (cart.getHurtTime() == 0) {
				cart.setHurtDir(-cart.getHurtDir());
				cart.setHurtTime(10);
				cart.setDamage(50.0F);
				cart.hurtMarked = true;
			}
		}
	}

	public boolean isFullyCoupled() {
		return isLeadingCoupling() && isConnectedToCoupling();
	}

	public boolean isLeadingCoupling() {
		return couplings.get(true)
			.isPresent();
	}

	public boolean isConnectedToCoupling() {
		return couplings.get(false)
			.isPresent();
	}

	public boolean isCoupledThroughContraption() {
		for (boolean current : Iterate.trueAndFalse)
			if (hasContraptionCoupling(current))
				return true;
		return false;
	}

	public boolean hasContraptionCoupling(boolean current) {
		Optional<CouplingData> optional = couplings.get(current);
		return optional.isPresent() && optional.get().contraption;
	}

	public float getCouplingLength(boolean leading) {
		Optional<CouplingData> optional = couplings.get(leading);
		return optional.map(couplingData -> couplingData.length).orElse(0F);
	}

	public void decouple() {
		couplings.forEachWithContext((opt, main) -> opt.ifPresent(cd -> {
			UUID idOfOther = cd.idOfCart(!main);
			MinecartController otherCart = CapabilityMinecartController.getIfPresent(getWorld(), idOfOther);
			if (otherCart == null)
				return;

			removeConnection(main);
			otherCart.removeConnection(!main);
		}));
	}

	public void removeConnection(boolean main) {
		if (hasContraptionCoupling(main) && getWorld() != null && !getWorld().isClientSide) {
			List<Entity> passengers = cart().getPassengers();
			if (!passengers.isEmpty()) {
				Entity entity = passengers.getFirst();
				if (entity instanceof AbstractContraptionEntity)
					((AbstractContraptionEntity) entity).disassemble();
			}
		}

		couplings.set(main, Optional.empty());
		needsEntryRefresh |= main;
		sendData();
	}

	public void prepareForCoupling(boolean isLeading) {
		// reverse existing chain if necessary
		if (isLeading && isLeadingCoupling() || !isLeading && isConnectedToCoupling()) {

			List<MinecartController> cartsToFlip = new ArrayList<>();
			MinecartController current = this;
			boolean forward = current.isLeadingCoupling();
			int safetyCount = 1000;

			do {
				if (safetyCount-- <= 0) {
					Create.LOGGER.warn("Infinite loop in coupling iteration");
					return;
				}
				cartsToFlip.add(current);
				current = CouplingHandler.getNextInCouplingChain(getWorld(), current, forward);
			} while (current != null && current != MinecartController.EMPTY);

			for (MinecartController minecartController : cartsToFlip) {
				minecartController.couplings.forEachWithContext((opt, leading) -> opt.ifPresent(cd -> {
					cd.flip();
					if (!cd.contraption)
						return;
					List<Entity> passengers = minecartController.cart()
						.getPassengers();
					if (passengers.isEmpty())
						return;
					Entity entity = passengers.getFirst();
					if (!(entity instanceof OrientedContraptionEntity contraption))
						return;
					UUID couplingId = contraption.getCouplingId();
					if (couplingId == cd.mainCartID) {
						contraption.setCouplingId(cd.connectedCartID);
						return;
					}
					if (couplingId == cd.connectedCartID) {
						contraption.setCouplingId(cd.mainCartID);
						return;
					}
				}));
				minecartController.couplings = minecartController.couplings.swap();
				minecartController.needsEntryRefresh = true;
				if (minecartController == this)
					continue;
				minecartController.sendData();
			}
		}
	}

	public void coupleWith(boolean isLeading, UUID coupled, float length, boolean contraption) {
		UUID mainID = isLeading ? cart().getUUID() : coupled;
		UUID connectedID = isLeading ? coupled : cart().getUUID();
		couplings.set(isLeading, Optional.of(new CouplingData(mainID, connectedID, length, contraption)));
		needsEntryRefresh |= isLeading;
		sendData();
	}

	@Nullable
	public UUID getCoupledCart(boolean asMain) {
		Optional<CouplingData> optional = couplings.get(asMain);
		if (optional.isEmpty())
			return null;
		CouplingData couplingData = optional.get();
		return asMain ? couplingData.connectedCartID : couplingData.mainCartID;
	}

	public boolean isStalled() {
		return isStalled(true) || isStalled(false);
	}

	private boolean isStalled(boolean internal) {
		return stallData.get(internal)
			.isPresent();
	}

	public void setStalledExternally(boolean stall) {
		setStalled(stall, false);
	}

	private void setStalled(boolean stall, boolean internal) {
		if (isStalled(internal) == stall)
			return;

		@Nullable AbstractMinecart cart = cart();
		if (cart == null)
			return;

		if (stall && cart != null) {
			stallData.set(internal, Optional.of(new StallData(cart)));
			sendData();
			return;
		}

		if (!isStalled(!internal) && cart != null)
			stallData.get(internal)
					.ifPresent(data -> data.release(cart));
		stallData.set(internal, Optional.empty());

		sendData();
	}

	public void sendData() {
		sendData(null);
	}

	public void sendData(@Nullable AbstractMinecart cart) {
		if (cart != null) {
			this.weakRef = new WeakReference<>(cart);
			needsEntryRefresh = true;
		}

		if (getWorld() == null || getWorld().isClientSide)
			return;
		CatnipServices.NETWORK.sendToClientsTrackingEntity(this.cart(), new MinecartControllerUpdatePacket(this, getWorld().registryAccess()));
	}

	@Override
	public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
		CompoundTag compoundNBT = new CompoundTag();

		stallData.forEachWithContext((opt, internal) -> opt
			.ifPresent(sd -> compoundNBT.put(internal ? "InternalStallData" : "StallData", sd.serialize())));
		couplings.forEachWithContext((opt, main) -> opt
			.ifPresent(cd -> compoundNBT.put(main ? "MainCoupling" : "ConnectedCoupling", cd.serialize())));

		return compoundNBT;
	}

	@Override
	public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag nbt) {
		Optional<StallData> internalSD = Optional.empty();
		Optional<StallData> externalSD = Optional.empty();
		Optional<CouplingData> mainCD = Optional.empty();
		Optional<CouplingData> connectedCD = Optional.empty();

		if (nbt.contains("InternalStallData"))
			internalSD = Optional.of(StallData.read(nbt.getCompound("InternalStallData")));
		if (nbt.contains("StallData"))
			externalSD = Optional.of(StallData.read(nbt.getCompound("StallData")));
		if (nbt.contains("MainCoupling"))
			mainCD = Optional.of(CouplingData.read(nbt.getCompound("MainCoupling")));
		if (nbt.contains("ConnectedCoupling"))
			connectedCD = Optional.of(CouplingData.read(nbt.getCompound("ConnectedCoupling")));

		stallData = Couple.create(internalSD, externalSD);
		couplings = Couple.create(mainCD, connectedCD);
		needsEntryRefresh = true;
	}

	public boolean isPresent() {
		return weakRef.get() != null && cart().isAlive();
	}

	public AbstractMinecart cart() {
		return weakRef.get();
	}

	private @Nullable Level getWorld() {
		if (cart() == null)
			return null;
		return cart().level();
	}

	private static class CouplingData {

		private UUID mainCartID;
		private UUID connectedCartID;
		private float length;
		private boolean contraption;

		public CouplingData(UUID mainCartID, UUID connectedCartID, float length, boolean contraption) {
			this.mainCartID = mainCartID;
			this.connectedCartID = connectedCartID;
			this.length = length;
			this.contraption = contraption;
		}

		void flip() {
			UUID swap = mainCartID;
			mainCartID = connectedCartID;
			connectedCartID = swap;
		}

		CompoundTag serialize() {
			CompoundTag nbt = new CompoundTag();
			nbt.put("Main", NbtUtils.createUUID(mainCartID));
			nbt.put("Connected", NbtUtils.createUUID(connectedCartID));
			nbt.putFloat("Length", length);
			nbt.putBoolean("Contraption", contraption);
			return nbt;
		}

		static CouplingData read(CompoundTag nbt) {
			UUID mainCartID = NbtUtils.loadUUID(NBTHelper.getINBT(nbt, "Main"));
			UUID connectedCartID = NbtUtils.loadUUID(NBTHelper.getINBT(nbt, "Connected"));
			float length = nbt.getFloat("Length");
			boolean contraption = nbt.getBoolean("Contraption");
			return new CouplingData(mainCartID, connectedCartID, length, contraption);
		}

		public UUID idOfCart(boolean main) {
			return main ? mainCartID : connectedCartID;
		}

	}

	private static class StallData {
		Vec3 position;
		Vec3 motion;
		float yaw, pitch;

		private StallData() {
		}

		StallData(AbstractMinecart entity) {
			position = entity.position();
			motion = entity.getDeltaMovement();
			yaw = entity.getYRot();
			pitch = entity.getXRot();
			tick(entity);
		}

		void tick(AbstractMinecart entity) {
//			entity.setPos(position.x, position.y, position.z);
			entity.setDeltaMovement(Vec3.ZERO);
			entity.setYRot(yaw);
			entity.setXRot(pitch);
		}

		void release(AbstractMinecart entity) {
			entity.setDeltaMovement(motion);
		}

		CompoundTag serialize() {
			CompoundTag nbt = new CompoundTag();
			nbt.put("Pos", VecHelper.writeNBT(position));
			nbt.put("Motion", VecHelper.writeNBT(motion));
			nbt.putFloat("Yaw", yaw);
			nbt.putFloat("Pitch", pitch);
			return nbt;
		}

		static StallData read(CompoundTag nbt) {
			StallData stallData = new StallData();
			stallData.position = VecHelper.readNBT(nbt.getList("Pos", Tag.TAG_DOUBLE));
			stallData.motion = VecHelper.readNBT(nbt.getList("Motion", Tag.TAG_DOUBLE));
			stallData.yaw = nbt.getFloat("Yaw");
			stallData.pitch = nbt.getFloat("Pitch");
			return stallData;
		}
	}

	private static class Empty extends MinecartController {

		private Empty() {
			super(null);
		}

		public Empty(AbstractMinecart minecart) {
			super(minecart);
		}

		@Override
		@NotNull
		protected Type getType() {
			return Type.EMPTY;
		}

		private static void warn() {
			Create.LOGGER.warn("Method called on EMPTY MinecartController", new Exception());
		}

		@Override
		public void tick() {
			warn();
		}

		@Override
		public boolean isFullyCoupled() {
			warn();
			return false;
		}

		@Override
		public boolean isLeadingCoupling() {
			warn();
			return false;
		}

		@Override
		public boolean isConnectedToCoupling() {
			warn();
			return false;
		}

		@Override
		public boolean isCoupledThroughContraption() {
			warn();
			return false;
		}

		@Override
		public boolean hasContraptionCoupling(boolean current) {
			warn();
			return false;
		}

		@Override
		public float getCouplingLength(boolean leading) {
			warn();
			return 0.0f;
		}

		@Override
		public void decouple() {
			warn();
		}

		@Override
		public void removeConnection(boolean main) {
			warn();
		}

		@Override
		public void prepareForCoupling(boolean isLeading) {
			warn();
		}

		@Override
		public void coupleWith(boolean isLeading, UUID coupled, float length, boolean contraption) {
			warn();
		}

		@Nullable
		@Override
		public UUID getCoupledCart(boolean asMain) {
			warn();
			return null;
		}

		@Override
		public boolean isStalled() {
			warn();
			return false;
		}

		@Override
		public void setStalledExternally(boolean stall) {
			warn();
		}

		@Override
		public void sendData() {
			super.sendData();
		}

		@Override
		public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
			return super.serializeNBT(provider);
		}

		@Override
		public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag nbt) {
			super.deserializeNBT(provider, nbt);
		}

		@Override
		public boolean isPresent() {
			return super.isPresent();
		}

		@Override
		public AbstractMinecart cart() {
			return super.cart();
		}
	}

	protected enum Type implements StringRepresentable {
		EMPTY(new IAttachmentSerializer<>() {
			@Override
			public @NotNull MinecartController read(@NotNull IAttachmentHolder holder, @NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
				return MinecartController.EMPTY;
			}

			@Override
			public CompoundTag write(@NotNull MinecartController attachment, @NotNull HolderLookup.Provider provider) {
				return attachment.serializeNBT(provider);
			}
		}),
		NORMAL(new IAttachmentSerializer<>() {
			@Override
			public @NotNull MinecartController read(@NotNull IAttachmentHolder holder, @NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
				MinecartController controller = new MinecartController(null);
				controller.deserializeNBT(provider, tag);
				return controller;
			}

			@Override
			public @Nullable CompoundTag write(@NotNull MinecartController attachment, @NotNull HolderLookup.Provider provider) {
				return attachment.serializeNBT(provider);
			}
		});

		public static final Codec<Type> CODEC = StringRepresentable.fromValues(Type::values);

		private final IAttachmentSerializer<CompoundTag, MinecartController> serializer;

		private static final IAttachmentSerializer<CompoundTag, MinecartController> SERIALIZER = new IAttachmentSerializer<>() {
			@Override
			public @NotNull MinecartController read(@NotNull IAttachmentHolder holder, @NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
				return Type.valueOf(tag.getString("Type")).getSerializer().read(holder, tag, provider);
			}

			@Override
			public @Nullable CompoundTag write(MinecartController attachment, @NotNull HolderLookup.Provider provider) {
				CompoundTag tag = attachment.serializeNBT(provider);
				if (tag != null) {
					tag.putString("Type", attachment.getType().name());
				}
				return tag;
			}
		};

		Type(IAttachmentSerializer<CompoundTag, MinecartController> serializer) {
			this.serializer = serializer;
		}

		public IAttachmentSerializer<CompoundTag, MinecartController> getSerializer() {
			return serializer;
		}

		@Override
		public @NotNull String getSerializedName() {
			return Lang.asId(name());
		}
	}

}
