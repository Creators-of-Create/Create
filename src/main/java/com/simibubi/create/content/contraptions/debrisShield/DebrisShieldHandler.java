package com.simibubi.create.content.contraptions.debrisShield;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.ref.WeakReference;
import java.util.List;

public class DebrisShieldHandler<Owner extends SmartTileEntity & IDebrisShielded> {
	private final WeakReference<Owner> owner;
	private ScrollOptionBehaviour<SelectionMode> selectionMode;

	public DebrisShieldHandler(Owner owner) {
		if (owner == null) {
			throw new NullPointerException("owner");
		}

		this.owner = new WeakReference<>(owner);
	}

	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(selectionMode = new ScrollOptionBehaviour<>(SelectionMode.class,
				Lang.translate("logistics.has_debris_shield"), owner.get(),
				new CenteredSideValueBoxTransform((state, d) -> d == Direction.UP || d == Direction.DOWN)));
		selectionMode.requiresDebrisShield();
		selectionMode.withCallback(value -> shieldNeighbors(SelectionMode.fromOrdinal(value)));
	}

	public void write(CompoundTag compound) {
		NBTHelper.writeEnum(compound, "IsShielded", getShielded());
	}

	public void read(CompoundTag compound) {
		SelectionMode selectionMode = NBTHelper.readEnum(compound, "IsShielded", SelectionMode.class);
		setShielded(selectionMode);
	}

	public SelectionMode getShielded() {
		return selectionMode != null
				? selectionMode.get()
				: SelectionMode.OPEN;
	}

	public void setShielded(Integer modeOrdinal) {
		selectionMode.setValue(modeOrdinal);
	}

	public void setShielded(SelectionMode mode) {
		if (mode == null)
			mode = SelectionMode.OPEN;

		setShielded(mode.ordinal());
	}

	public void setShielded(DebrisShieldHandler<Owner> other) {
		setShielded(other.selectionMode.get().ordinal());
	}

	public SelectionMode toggle() {
		SelectionMode mode = getShielded() == SelectionMode.OPEN
				? SelectionMode.SHIELDED
				: SelectionMode.OPEN;

		setShielded(mode.ordinal());
		return mode;
	}

	public boolean isShielded() {
		return getShielded() == SelectionMode.SHIELDED;
	}

	private void shieldNeighbors(SelectionMode mode) {
		Owner owner = this.owner.get();
		if (owner == null)
			return;

		Level level = owner.getLevel();
		if (level == null)
			return;

		for (BlockPos segment : owner.getNeighbours()) {
			if (segment == null)
				continue;

			BlockState segmentState = owner.getBlockState();
			BlockEntity segmentTE = level.getBlockEntity(segment);
			if (segmentState != null && owner.canBlockBeShielded(segmentState) &&
					segmentTE != null && !segmentTE.isRemoved() && segmentTE instanceof IDebrisShielded)
				((IDebrisShielded)segmentTE).setShielded(mode);
		}
	}

	public enum SelectionMode implements INamedIconOptions {
		OPEN(AllIcons.I_CONFIRM),
		SHIELDED(AllIcons.I_DISABLE),

		;

		private final String translationKey;
		private final AllIcons icon;

		SelectionMode(AllIcons icon) {
			this.icon = icon;
			this.translationKey = "debris_shield.selection_mode." + Lang.asId(name());
		}

		public static SelectionMode fromOrdinal(Integer ordinal) {
			if (ordinal == OPEN.ordinal()) {
				return OPEN;
			} else if (ordinal == SHIELDED.ordinal()) {
				return SHIELDED;
			}

			throw new IllegalArgumentException("ordinal");
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}
	}
}
