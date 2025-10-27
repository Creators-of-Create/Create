package com.simibubi.create.api.contraption.storage.item;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.api.contraption.storage.item.menu.MountedStorageMenus;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

public abstract class MountedItemStorage implements IItemHandlerModifiable {
	public static final Codec<MountedItemStorage> CODEC = MountedItemStorageType.CODEC.dispatch(
		storage -> storage.type, type -> type.codec
	);

	@SuppressWarnings("deprecation")
	public static final StreamCodec<RegistryFriendlyByteBuf, MountedItemStorage> STREAM_CODEC = StreamCodec.of(
		(b, t) -> b.writeWithCodec(RegistryOps.create(NbtOps.INSTANCE, b.registryAccess()), CODEC, t),
		b -> b.readWithCodecTrusted(RegistryOps.create(NbtOps.INSTANCE, b.registryAccess()), CODEC)
	);

	public final MountedItemStorageType<? extends MountedItemStorage> type;

	protected MountedItemStorage(MountedItemStorageType<?> type) {
		this.type = Objects.requireNonNull(type);
	}

	/**
	 * Un-mount this storage back into the world. The expected storage type of the target
	 * block has already been checked to make sure it matches this storage's type.
	 */
	public abstract void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be);

	/**
	 * Handle a player clicking on this mounted storage. This is always called on the server.
	 * The default implementation will try to open a generic GUI for standard inventories.
	 * For this to work, this storage must have 1-6 complete rows of 9 slots.
	 * @return true if the interaction was successful
	 */
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
		ServerLevel level = player.serverLevel();
		BlockPos localPos = info.pos();
		Vec3 localPosVec = Vec3.atCenterOf(localPos);
		Predicate<Player> stillValid = p -> {
			Vec3 currentPos = contraption.entity.toGlobalVector(localPosVec, 0);
			return this.isMenuValid(player, contraption, currentPos);
		};
		Component menuName = this.getMenuName(info, contraption);
		IItemHandlerModifiable handler = this.getHandlerForMenu(info, contraption);
		Consumer<Player> onClose = p -> {
			Vec3 newPos = contraption.entity.toGlobalVector(localPosVec, 0);
			this.playClosingSound(level, newPos);
		};

		OptionalInt id = player.openMenu(this.createMenuProvider(menuName, handler, stillValid, onClose));
		if (id.isPresent()) {
			Vec3 globalPos = contraption.entity.toGlobalVector(localPosVec, 0);
			this.playOpeningSound(level, globalPos);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the item handler that will be used by this storage's menu. This is useful for
	 * handling multi-blocks, such as double chests.
	 */
	protected IItemHandlerModifiable getHandlerForMenu(StructureBlockInfo info, Contraption contraption) {
		return this;
	}

	/**
	 * @param player the player who opened the menu
	 * @param pos the center of this storage in-world
	 * @return true if a GUI opened for this storage is still valid
	 */
	protected boolean isMenuValid(ServerPlayer player, Contraption contraption, Vec3 pos) {
		return contraption.entity.isAlive() && player.distanceToSqr(pos) < (8 * 8);
	}

	/**
	 * @return the title to be shown in the GUI when this storage is opened
	 */
	protected Component getMenuName(StructureBlockInfo info, Contraption contraption) {
		MutableComponent blockName = info.state().getBlock().getName();
		return CreateLang.translateDirect("contraptions.moving_container", blockName);
	}

	/**
	 * @return a MenuProvider that provides the menu players will see when opening this storage
	 */
	@Nullable
	protected MenuProvider createMenuProvider(Component name, IItemHandlerModifiable handler,
											  Predicate<Player> stillValid, Consumer<Player> onClose) {
		return MountedStorageMenus.createGeneric(name, handler, stillValid, onClose);
	}

	/**
	 * Play the sound made by opening this storage's GUI.
	 */
	protected void playOpeningSound(ServerLevel level, Vec3 pos) {
		level.playSound(
			null, BlockPos.containing(pos),
			SoundEvents.BARREL_OPEN, SoundSource.BLOCKS,
			0.75f, 1f
		);
	}

	/**
	 * Play the sound made by closing this storage's GUI.
	 */
	protected void playClosingSound(ServerLevel level, Vec3 pos) {
	}
}
