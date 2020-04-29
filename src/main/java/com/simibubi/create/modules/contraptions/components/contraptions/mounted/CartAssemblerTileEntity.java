package com.simibubi.create.modules.contraptions.components.contraptions.mounted;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.Direction;

public class CartAssemblerTileEntity extends SmartTileEntity {

	protected ScrollOptionBehaviour<CartMovementMode> movementMode;

	public CartAssemblerTileEntity() {
		super(AllTileEntities.CART_ASSEMBLER.type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		movementMode = new ScrollOptionBehaviour<>(CartMovementMode.class,
				Lang.translate("contraptions.cart_movement_mode"), this, getMovementModeSlot());
		movementMode.requiresWrench();
		behaviours.add(movementMode);
	}

	protected ValueBoxTransform getMovementModeSlot() {
		return new CenteredSideValueBoxTransform((state, d) -> d == Direction.UP);
	}

	public static enum CartMovementMode implements INamedIconOptions {

		ROTATE(ScreenResources.I_CART_ROTATE),
		ROTATE_PAUSED(ScreenResources.I_CART_ROTATE_PAUSED),
		ROTATION_LOCKED(ScreenResources.I_CART_ROTATE_LOCKED),

		;

		private String translationKey;
		private ScreenResources icon;

		private CartMovementMode(ScreenResources icon) {
			this.icon = icon;
			translationKey = "contraptions.cart_movement_mode." + Lang.asId(name());
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

}
