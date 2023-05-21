package com.simibubi.create.content.decoration.slidingDoor;

import java.util.Arrays;
import java.util.function.Consumer;

import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum DoorControl {

	ALL, NORTH, EAST, SOUTH, WEST, NONE;

	private static String[] valuesAsString() {
		DoorControl[] values = values();
		return Arrays.stream(values)
			.map(dc -> Lang.asId(dc.name()))
			.toList()
			.toArray(new String[values.length]);
	}

	public boolean matches(Direction doorDirection) {
		return switch (this) {
		case ALL -> true;
		case NORTH -> doorDirection == Direction.NORTH;
		case EAST -> doorDirection == Direction.EAST;
		case SOUTH -> doorDirection == Direction.SOUTH;
		case WEST -> doorDirection == Direction.WEST;
		default -> false;
		};
	}

	@OnlyIn(Dist.CLIENT)
	public static Pair<ScrollInput, Label> createWidget(int x, int y, Consumer<DoorControl> callback,
		DoorControl initial) {

		DoorControl playerFacing = NONE;
		Entity cameraEntity = Minecraft.getInstance().cameraEntity;
		if (cameraEntity != null) {
			Direction direction = cameraEntity.getDirection();
			if (direction == Direction.EAST)
				playerFacing = EAST;
			if (direction == Direction.WEST)
				playerFacing = WEST;
			if (direction == Direction.NORTH)
				playerFacing = NORTH;
			if (direction == Direction.SOUTH)
				playerFacing = SOUTH;
		}

		Label label = new Label(x + 4, y + 6, Components.empty()).withShadow();
		ScrollInput input = new SelectionScrollInput(x, y, 53, 16)
			.forOptions(Lang.translatedOptions("contraption.door_control", valuesAsString()))
			.titled(Lang.translateDirect("contraption.door_control"))
			.calling(s -> {
				DoorControl mode = values()[s];
				label.text = Lang.translateDirect("contraption.door_control." + Lang.asId(mode.name()) + ".short");
				callback.accept(mode);
			})
			.addHint(Lang.translateDirect("contraption.door_control.player_facing",
				Lang.translateDirect("contraption.door_control." + Lang.asId(playerFacing.name()) + ".short")))
			.setState(initial.ordinal());
		input.onChanged();
		return Pair.of(input, label);
	}

}
