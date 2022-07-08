package com.simibubi.create.content.logistics.block.redstone;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.logistics.block.display.DisplayLinkBlock;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalTileEntity;
import com.simibubi.create.content.logistics.trains.management.edgePoint.signal.SignalTileEntity.SignalState;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.DynamicComponent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NixieTubeTileEntity extends SmartTileEntity {

	private static final Couple<String> EMPTY = Couple.create("", "");

	private int redstoneStrength;
	private Optional<DynamicComponent> customText;
	private int nixieIndex;
	private Couple<String> displayedStrings;

	private WeakReference<SignalTileEntity> cachedSignalTE;
	public SignalState signalState;

	public NixieTubeTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
		SignalTileEntity signalTileEntity = cachedSignalTE.get();

		if (signalTileEntity == null || signalTileEntity.isRemoved()) {
			Direction facing = NixieTubeBlock.getFacing(getBlockState());
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
			if (blockEntity instanceof SignalTileEntity signal) {
				signalState = signal.getState();
				cachedSignalTE = new WeakReference<>(signal);
			}
			return;
		}

		signalState = signalTileEntity.getState();
	}

	@Override
	public void initialize() {
		if (level.isClientSide)
			updateDisplayedStrings();
	}

	//

	public boolean reactsToRedstone() {
		return customText.isEmpty();
	}

	public Couple<String> getDisplayedStrings() {
		if (displayedStrings == null)
			return EMPTY;
		return displayedStrings;
	}

	public MutableComponent getFullText() {
		return customText.map(DynamicComponent::get)
			.orElse(new TextComponent("" + redstoneStrength));
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
		if (signalState != null)
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
		}

		if (customText.isEmpty())
			redstoneStrength = nbt.getInt("RedstoneStrength");
		if (clientPacket)
			updateDisplayedStrings();
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
	}

	private String charOrEmpty(String string, int index) {
		return string.length() <= index ? " " : string.substring(index, index + 1);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

}
