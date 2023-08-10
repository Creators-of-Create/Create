package com.simibubi.create.content.contraptions;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;

import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.core.BlockPos;

public interface IControlContraption {

	public boolean isAttachedTo(AbstractContraptionEntity contraption);

	public void attach(ControlledContraptionEntity contraption);

	public void onStall();

	public boolean isValid();

	public BlockPos getBlockPosition();

	static enum MovementMode implements INamedIconOptions {

		MOVE_PLACE(AllIcons.I_MOVE_PLACE),
		MOVE_PLACE_RETURNED(AllIcons.I_MOVE_PLACE_RETURNED),
		MOVE_NEVER_PLACE(AllIcons.I_MOVE_NEVER_PLACE),

		;

		private String translationKey;
		private AllIcons icon;

		private MovementMode(AllIcons icon) {
			this.icon = icon;
			translationKey = "contraptions.movement_mode." + Lang.asId(name());
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

	static enum RotationMode implements INamedIconOptions {

		ROTATE_PLACE(AllIcons.I_ROTATE_PLACE),
		ROTATE_PLACE_RETURNED(AllIcons.I_ROTATE_PLACE_RETURNED),
		ROTATE_NEVER_PLACE(AllIcons.I_ROTATE_NEVER_PLACE),

		;

		private String translationKey;
		private AllIcons icon;

		private RotationMode(AllIcons icon) {
			this.icon = icon;
			translationKey = "contraptions.movement_mode." + Lang.asId(name());
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
