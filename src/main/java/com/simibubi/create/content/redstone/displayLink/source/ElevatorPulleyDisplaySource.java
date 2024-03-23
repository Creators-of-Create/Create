package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.simibubi.create.content.contraptions.piston.LinearActuatorBlockEntity;
import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CallbackI;

import java.util.Objects;

public class ElevatorPulleyDisplaySource extends PercentOrProgressBarDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof ElevatorPulleyBlockEntity epbe))
			return null;
		if (epbe.movedContraption == null)
			return new TextComponent("");
		Contraption movedContraption = epbe.movedContraption.getContraption();
		if (!(movedContraption instanceof ElevatorContraption ec))
			return null;
		int mode = context.sourceConfig().getInt("Mode");
		return switch (mode) {
			case 0 -> new TextComponent(String.valueOf(floorCount(ec)));
			case 1 -> new TextComponent(String.valueOf(targetFloor(epbe, ec)));
			case 2 -> new TextComponent(String.valueOf(targetFloorDescription(epbe, ec)));
			case 3 -> new TextComponent(String.valueOf(distanceToTop(epbe, ec)));
			case 6 -> new TextComponent(String.valueOf(distanceToBottom(epbe, ec)));
			default -> super.provideLine(context, stats);
		};
	}

	private float shaftHeight(ElevatorPulleyBlockEntity epbe,ElevatorContraption ec) {
		return (float)topFloorY(epbe, ec) - bottomFloorY(epbe, ec);
	}

	private float distanceToTop(ElevatorPulleyBlockEntity epbe, ElevatorContraption ec) {
		int contraptionY = epbe.getBlockPosition().getY() - (int)epbe.offset;
		contraptionY --;
		return (float)Math.abs(topFloorY(epbe, ec) - contraptionY);
	}

	private float distanceToBottom(ElevatorPulleyBlockEntity epbe, ElevatorContraption ec) {
		int contraptionY = epbe.getBlockPosition().getY() - (int)epbe.offset;
		contraptionY --;
		return (float)Math.abs(contraptionY - bottomFloorY(epbe, ec));
	}

	private float topFloorY(ElevatorPulleyBlockEntity epbe, ElevatorContraption ec) {
		int topFloorY = epbe.getLevel().getMinBuildHeight();
		for (IntAttached<Couple<String>> floor : ec.namesList) {
			topFloorY = Math.max(floor.getFirst(), topFloorY);
		}
		return topFloorY;
	}

	private float bottomFloorY(ElevatorPulleyBlockEntity epbe, ElevatorContraption ec) {
		int bottomFloorY = epbe.getLevel().getMaxBuildHeight();
		for (IntAttached<Couple<String>> floor : ec.namesList) {
			bottomFloorY = Math.min(floor.getFirst(), bottomFloorY);
		}
		return (float)bottomFloorY;
	}

	private int floorCount(ElevatorContraption ec) {
		return ec.namesList.size();
	}

	private String targetFloor(ElevatorPulleyBlockEntity epbe, ElevatorContraption ec) {
		IntAttached<Couple<String>> targetFloor = null;
		for (IntAttached<Couple<String>> floor : ec.namesList) {
			if (Objects.equals(floor.getFirst(), ec.getCurrentTargetY(epbe.getLevel()))) {
				targetFloor = floor;
			}
		}
		return targetFloor != null ? targetFloor.getValue().getFirst() : "";
	}

	private String targetFloorDescription(ElevatorPulleyBlockEntity epbe, ElevatorContraption ec) {
		IntAttached<Couple<String>> targetFloor = null;
		for (IntAttached<Couple<String>> floor : ec.namesList) {
			if (Objects.equals(floor.getFirst(), ec.getCurrentTargetY(epbe.getLevel()))) {
				targetFloor = floor;
			}
		}
		return targetFloor != null ? targetFloor.getValue().getSecond() : "";
	}

	@Nullable
	@Override
	protected Float getProgress(DisplayLinkContext context) {
		BlockEntity be = context.getSourceBlockEntity();
		if (!(be instanceof ElevatorPulleyBlockEntity epbe))
			return null;
		Contraption movedContraption = epbe.movedContraption.getContraption();
		if (!(movedContraption instanceof ElevatorContraption ec))
			return null;
		int mode = context.sourceConfig().getInt("Mode");
		return switch (mode) {
			case 4,5 -> distanceToBottom(epbe, ec)/shaftHeight(epbe, ec);
			case 7,8 -> distanceToTop(epbe, ec)/shaftHeight(epbe, ec);
			default -> 0f;
		};
	}

	@Override
	protected boolean progressBarActive(DisplayLinkContext context) {
		int mode = context.sourceConfig().getInt("Mode");
		return mode == 5 || mode == 8;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;
		builder.addSelectionScrollInput(0, 138,
				(si, l) -> si.forOptions(Lang.translatedOptions("display_source.elevator_pulley",
								"floor_count", "current_floor", "current_floor_description", "top_distance_int", "top_distance_percent", "top_distance_progress_bar",
								"bottom_distance_int", "bottom_distance_percent", "bottom_distance_progress_bar"))
						.titled(Lang.translateDirect("display_source.elevator_pulley.display")),
				"Mode");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}
}
