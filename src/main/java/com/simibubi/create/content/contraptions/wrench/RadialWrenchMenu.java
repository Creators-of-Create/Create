package com.simibubi.create.content.contraptions.wrench;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.RadialMenu;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;

public class RadialWrenchMenu extends AbstractSimiScreen {

	private static final List<Property<?>> properties = List.of(
			RotatedPillarKineticBlock.AXIS,
			DirectionalKineticBlock.FACING,
			HorizontalAxisKineticBlock.HORIZONTAL_AXIS,
			HorizontalKineticBlock.HORIZONTAL_FACING
	);

	private final BlockState state;
	private List<BlockState> allStates = List.of();
	private RadialMenu radialMenu;
	private int ticksOpen;

	public RadialWrenchMenu(BlockState state) {
		this.state = state;

		if (!(state.getBlock() instanceof IWrenchable wrenchable))
				return;

		allStates = getRotatedStates(state);
		/*allStates = Stream.concat(Arrays.stream(Iterate.directions)
				.map(dir -> wrenchable.getRotatedBlockState(state, dir)),
				Stream.of(state)
		).distinct().toList();*/

		radialMenu = new RadialMenu(allStates.size());
	}

	public static List<BlockState> getRotatedStates(BlockState state) {
		List<BlockState> states = new ArrayList<>();
		states.add(state);

		for (Property<?> property : properties) {
			if (state.hasProperty(property)) {
				cycleAllPropertyValues(property, states);
			}
		}

		return states;
	}

	private static void cycleAllPropertyValues(Property<?> property, List<BlockState> states) {
		while (true) {
			BlockState lastState = states.get(states.size() - 1);
			BlockState cycledState = lastState.cycle(property);

			if (states.contains(cycledState))
				break;

			states.add(cycledState);
		}
	}

	@Override
	public void tick() {
		ticksOpen++;

		super.tick();
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = this.width / 2;
		int y = this.height / 2;

		PoseStack ms = graphics.pose();

		LocalPlayer player = Minecraft.getInstance().player;

		ms.pushPose();
		ms.translate(x, y, 0);

		ms.pushPose();

		radialMenu.draw(graphics, allStates
				.stream()
				.map(state -> GuiGameElement.of(state)
						.rotateBlock(player.getXRot(), player.getYRot() + 180, 0f)
						.scale(24)
				).toList()
		);

		ms.popPose();

		if (allStates.size() <= 1) {
			GuiGameElement.of(state)
					.rotateBlock(player.getXRot(), player.getYRot(), 0f)
					.scale(24)
					.render(graphics);
		}

		ms.popPose();

	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		Color color = new Color(0x50_101010)
				.scaleAlpha(Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f));

		graphics.fillGradient(0, 0, this.width, this.height, color.getRGB(), color.getRGB());
	}

	@Override
	public boolean keyReleased(int code, int scanCode, int modifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(code, scanCode);
		if (AllKeys.ROTATE_MENU.getKeybind().isActiveAndMatches(mouseKey)) {
			onClose();
			return true;
		}
		return super.keyReleased(code, scanCode, modifiers);
	}

}
