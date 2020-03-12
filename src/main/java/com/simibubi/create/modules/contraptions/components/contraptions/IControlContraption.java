package com.simibubi.create.modules.contraptions.components.contraptions;

import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.utility.Lang;

public interface IControlContraption {

	public void attach(ContraptionEntity contraption);

	public void onStall();

	public boolean isValid();

	static enum MovementMode implements INamedIconOptions {

		MOVE_PLACE(ScreenResources.I_MOVE_PLACE),
		MOVE_PLACE_RETURNED(ScreenResources.I_MOVE_PLACE_RETURNED),
		MOVE_NEVER_PLACE(ScreenResources.I_MOVE_NEVER_PLACE),

		;

		private String translationKey;
		private ScreenResources icon;

		private MovementMode(ScreenResources icon) {
			this.icon = icon;
			translationKey = "contraptions.movement_mode." + Lang.asId(name());
		}

		@Override
		public ScreenResources getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}

	}

	static enum RotationMode implements INamedIconOptions {

		ROTATE_PLACE(ScreenResources.I_ROTATE_PLACE),
		ROTATE_PLACE_RETURNED(ScreenResources.I_ROTATE_PLACE_RETURNED),
		ROTATE_NEVER_PLACE(ScreenResources.I_ROTATE_NEVER_PLACE),

		;

		private String translationKey;
		private ScreenResources icon;

		private RotationMode(ScreenResources icon) {
			this.icon = icon;
			translationKey = "contraptions.movement_mode." + Lang.asId(name());
		}

		@Override
		public ScreenResources getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}

	}

	public void collided();

}
