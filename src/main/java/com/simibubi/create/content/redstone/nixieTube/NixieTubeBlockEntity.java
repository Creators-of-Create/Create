package com.simibubi.create.content.redstone.nixieTube;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.Create;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.DynamicComponent;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NixieTubeBlockEntity extends SmartBlockEntity {

	public static final class ComputerSignal {
		public static final class TubeDisplay {
			public static final int ENCODED_SIZE = 7;

			public byte r = 63, g = 63, b = 63;
			public byte blinkPeriod = 0, blinkOffTime = 0;
			public byte glowWidth = 1, glowHeight = 1;

			public void decode(byte[] data, int offset) {
				r = data[offset];
				g = data[offset + 1];
				b = data[offset + 2];
				blinkPeriod = data[offset + 3];
				blinkOffTime = data[offset + 4];
				glowWidth = data[offset + 5];
				glowHeight = data[offset + 6];
			}

			public void encode(byte[] data, int offset) {
				data[offset] = r;
				data[offset + 1] = g;
				data[offset + 2] = b;
				data[offset + 3] = blinkPeriod;
				data[offset + 4] = blinkOffTime;
				data[offset + 5] = glowWidth;
				data[offset + 6] = glowHeight;
			}
		}

		public @NotNull TubeDisplay first = new TubeDisplay();
		public @NotNull TubeDisplay second = new TubeDisplay();

		public void decode(byte[] encoded) {
			first.decode(encoded, 0);
			second.decode(encoded, TubeDisplay.ENCODED_SIZE);
		}

		public byte[] encode() {
			byte[] encoded = new byte[TubeDisplay.ENCODED_SIZE * 2];
			first.encode(encoded, 0);
			second.encode(encoded, TubeDisplay.ENCODED_SIZE);
			return encoded;
		}
	}

	private static final Couple<String> EMPTY = Couple.create("", "");

	private int redstoneStrength;
	private Optional<DynamicComponent> customText;
	private int nixieIndex;
	private Couple<String> displayedStrings;
	public AbstractComputerBehaviour computerBehaviour;

	private WeakReference<SignalBlockEntity> cachedSignalTE;
	public @Nullable SignalState signalState;
	public @Nullable ComputerSignal computerSignal;

	public NixieTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		customText = Optional.empty();
		redstoneStrength = 0;
		cachedSignalTE = new WeakReference<>(null);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide)
			return;

		signalState = null;
		if (computerBehaviour.hasAttachedComputer()) {
			if (level.isClientSide && cachedSignalTE.get() != null) {
				cachedSignalTE = new WeakReference<>(null);
			}
			return;
		}

		SignalBlockEntity signalBlockEntity = cachedSignalTE.get();

		if (signalBlockEntity == null || signalBlockEntity.isRemoved()) {
			Direction facing = NixieTubeBlock.getFacing(getBlockState());
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
			if (blockEntity instanceof SignalBlockEntity signal) {
				signalState = signal.getState();
				cachedSignalTE = new WeakReference<>(signal);
			}
			return;
		}

		signalState = signalBlockEntity.getState();
	}

	@Override
	public void initialize() {
		if (level.isClientSide)
			updateDisplayedStrings();
	}

	//

	public boolean reactsToRedstone() {
		return !computerBehaviour.hasAttachedComputer() && customText.isEmpty();
	}

	public Couple<String> getDisplayedStrings() {
		if (displayedStrings == null)
			return EMPTY;
		return displayedStrings;
	}

	public MutableComponent getFullText() {
		return customText.map(DynamicComponent::get)
			.orElse(Components.literal("" + redstoneStrength));
	}

	public void updateRedstoneStrength(int signalStrength) {
		clearCustomText();
		redstoneStrength = signalStrength;
		DisplayLinkBlock.notifyGatherers(level, worldPosition);
		notifyUpdate();
	}

	public void displayCustomText(String tagElement, int nixiePositionInRow) {
		if (tagElement == null)
			return;
		if (customText.filter(d -> d.sameAs(tagElement))
			.isPresent())
			return;

		DynamicComponent component = customText.orElseGet(DynamicComponent::new);
		component.displayCustomText(level, worldPosition, tagElement);
		customText = Optional.of(component);
		nixieIndex = nixiePositionInRow;
		DisplayLinkBlock.notifyGatherers(level, worldPosition);
		notifyUpdate();
	}

	public void updateDisplayedStrings() {
		if (signalState != null || computerSignal != null)
			return;
		customText.map(DynamicComponent::resolve)
			.ifPresentOrElse(
				fullText -> displayedStrings =
					Couple.create(charOrEmpty(fullText, nixieIndex * 2), charOrEmpty(fullText, nixieIndex * 2 + 1)),
				() -> displayedStrings =
					Couple.create(redstoneStrength < 10 ? "0" : "1", String.valueOf(redstoneStrength % 10)));
	}

	public void clearCustomText() {
		nixieIndex = 0;
		customText = Optional.empty();
	}

	public int getRedstoneStrength() {
		return redstoneStrength;
	}

	//

	@Override
	protected void read(CompoundTag nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);

		if (nbt.contains("CustomText")) {
			DynamicComponent component = customText.orElseGet(DynamicComponent::new);
			component.read(level, worldPosition, nbt);

			if (component.isValid()) {
				customText = Optional.of(component);
				nixieIndex = nbt.getInt("CustomTextIndex");
			} else {
				customText = Optional.empty();
				nixieIndex = 0;
			}
		} else {
			customText = Optional.empty();
			nixieIndex = 0;
		}

		if (customText.isEmpty())
			redstoneStrength = nbt.getInt("RedstoneStrength");
		if (clientPacket) {
			if (nbt.contains("ComputerSignal")) {
				byte[] encodedComputerSignal = nbt.getByteArray("ComputerSignal");
				if (computerSignal == null)
					computerSignal = new ComputerSignal();
				computerSignal.decode(encodedComputerSignal);
			} else {
				computerSignal = null;
			}

			updateDisplayedStrings();
		}
	}

	@Override
	protected void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);

		if (customText.isPresent()) {
			nbt.putInt("CustomTextIndex", nixieIndex);
			customText.get()
				.write(nbt);
		} else
			nbt.putInt("RedstoneStrength", redstoneStrength);
		if (clientPacket && computerSignal != null)
			nbt.putByteArray("ComputerSignal", computerSignal.encode());
	}

	private String charOrEmpty(String string, int index) {
		return string.length() <= index ? " " : string.substring(index, index + 1);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		if (computerBehaviour.isPeripheralCap(cap))
			return computerBehaviour.getPeripheralCapability();
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		computerBehaviour.removePeripheral();
	}

}
