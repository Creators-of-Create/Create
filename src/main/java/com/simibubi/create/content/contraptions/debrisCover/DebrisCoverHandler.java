package com.simibubi.create.content.contraptions.debrisCover;

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

public class DebrisCoverHandler<Owner extends SmartTileEntity & IDebrisCovered> {
	private final WeakReference<Owner> owner;
	private ScrollOptionBehaviour<SelectionMode> selectionMode;

	public DebrisCoverHandler(Owner owner) {
		if (owner == null) {
			throw new NullPointerException("owner");
		}

		this.owner = new WeakReference<>(owner);
	}

	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(selectionMode = new ScrollOptionBehaviour<>(SelectionMode.class,
				Lang.translate("logistics.has_debris_cover"), owner.get(),
				new CenteredSideValueBoxTransform((state, d) -> d == Direction.UP || d == Direction.DOWN)));
		selectionMode.requiresDebrisCover();
		selectionMode.withCallback(value -> coverNeighbors(SelectionMode.fromOrdinal(value)));
	}

	public void write(CompoundTag compound) {
		NBTHelper.writeEnum(compound, "IsCovered", getCovered());
	}

	public void read(CompoundTag compound) {
		SelectionMode selectionMode = NBTHelper.readEnum(compound, "IsCovered", SelectionMode.class);
		setCovered(selectionMode);
	}

	public SelectionMode getCovered() {
		return selectionMode != null
				? selectionMode.get()
				: SelectionMode.UNCOVERED;
	}

	public void setCovered(Integer modeOrdinal) {
		selectionMode.setValue(modeOrdinal);
	}

	public void setCovered(SelectionMode mode) {
		if (mode == null)
			mode = SelectionMode.UNCOVERED;

		setCovered(mode.ordinal());
	}

	public void setCovered(DebrisCoverHandler<Owner> other) {
		setCovered(other.selectionMode.get().ordinal());
	}

	public SelectionMode toggle() {
		SelectionMode mode = getCovered() == SelectionMode.UNCOVERED
				? SelectionMode.COVERED
				: SelectionMode.UNCOVERED;

		setCovered(mode.ordinal());
		return mode;
	}

	public boolean isCovered() {
		return getCovered() == SelectionMode.COVERED;
	}

	private void coverNeighbors(SelectionMode mode) {
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
			if (segmentState != null && owner.canBlockBeCovered(segmentState) &&
					segmentTE != null && !segmentTE.isRemoved() && segmentTE instanceof IDebrisCovered)
				((IDebrisCovered)segmentTE).setCovered(mode);
		}
	}

	public enum SelectionMode implements INamedIconOptions {
		UNCOVERED(AllIcons.I_CONFIRM),
		COVERED(AllIcons.I_DISABLE),

		;

		private final String translationKey;
		private final AllIcons icon;

		SelectionMode(AllIcons icon) {
			this.icon = icon;
			this.translationKey = "debris_cover.selection_mode." + Lang.asId(name());
		}

		public static SelectionMode fromOrdinal(Integer ordinal) {
			if (ordinal == UNCOVERED.ordinal()) {
				return UNCOVERED;
			} else if (ordinal == COVERED.ordinal()) {
				return COVERED;
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
