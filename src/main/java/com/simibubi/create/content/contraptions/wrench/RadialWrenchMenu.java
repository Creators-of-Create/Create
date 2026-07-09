package com.simibubi.create.content.contraptions.wrench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllKeys;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlock;
import com.simibubi.create.content.redstone.DirectedDirectionalBlock;
import com.simibubi.create.foundation.gui.AllIcons;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.client.gui.element.RenderElement;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.platform.CatnipServices;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class RadialWrenchMenu extends AbstractSimiScreen {

	public static final Map<Property<?>, String> VALID_PROPERTIES = new HashMap<>();

	static {
		registerRotationProperty(RotatedPillarKineticBlock.AXIS, "Axis");
		registerRotationProperty(DirectionalKineticBlock.FACING, "Facing");
		registerRotationProperty(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, "Axis");
		registerRotationProperty(HorizontalKineticBlock.HORIZONTAL_FACING, "Facing");
		registerRotationProperty(HopperBlock.FACING, "Facing");
		registerRotationProperty(DirectedDirectionalBlock.TARGET, "Target");

		registerRotationProperty(SequencedGearshiftBlock.VERTICAL, "Vertical");
	}

	public static final Set<Identifier> BLOCK_BLACKLIST = new HashSet<>();

	static {
		registerBlacklistedBlock(AllBlocks.LARGE_WATER_WHEEL.getId());
		registerBlacklistedBlock(AllBlocks.WATER_WHEEL_STRUCTURAL.getId());
	}

	public static void registerRotationProperty(Property<?> property, String label) {
		if (VALID_PROPERTIES.containsKey(property))
			return;

		VALID_PROPERTIES.put(property, label);
	}

	public static void registerBlacklistedBlock(Identifier location) {
		if (BLOCK_BLACKLIST.contains(location))
			return;

		BLOCK_BLACKLIST.add(location);
	}

	private final BlockState state;
	private final BlockPos pos;
	@Nullable
	private final BlockEntity blockEntity;
	private final Level level;
	private final NonVisualizationLevel nonVisualizationLevel;
	private final List<Map.Entry<Property<?>, String>> propertiesForState;
	private final int innerRadius = 50;
	private final int outerRadius = 110;

	private int selectedPropertyIndex = 0;
	private List<BlockState> allStates = List.of();
	private String propertyLabel = "";
	private int ticksOpen;
	private int selectedStateIndex = 0;

	private final RenderElement iconScroll = RenderElement.of(AllIcons.I_SCROLL);
	private final RenderElement iconUp = RenderElement.of(AllIcons.I_PRIORITY_HIGH);
	private final RenderElement iconDown = RenderElement.of(AllIcons.I_PRIORITY_LOW);

	public static Optional<RadialWrenchMenu> tryCreateFor(BlockState state, BlockPos pos, Level level) {
		if (BLOCK_BLACKLIST.contains(RegisteredObjectsHelper.getKeyOrThrow(state.getBlock())))
			return Optional.empty();

		var propertiesForState = VALID_PROPERTIES.entrySet().stream().filter(entry -> state.hasProperty(entry.getKey())).toList();

		if (propertiesForState.isEmpty())
			return Optional.empty();

		return Optional.of(new RadialWrenchMenu(state, pos, level, propertiesForState));
	}

	private RadialWrenchMenu(BlockState state, BlockPos pos, Level level, List<Map.Entry<Property<?>, String>> properties) {
		this.state = state;
		this.pos = pos;
		this.level = level;
		this.nonVisualizationLevel = new NonVisualizationLevel(level);
		this.blockEntity = level.getBlockEntity(pos);
		this.propertiesForState = properties;

		initForSelectedProperty();
	}

	private void initForSelectedProperty() {
		Map.Entry<Property<?>, String> entry = propertiesForState.get(selectedPropertyIndex);

		allStates = new ArrayList<>();
		//allStates.add(state);
		cycleAllPropertyValues(state, entry.getKey(), allStates);

		propertyLabel = entry.getValue();
	}

	private void cycleAllPropertyValues(BlockState state, Property<?> property, List<BlockState> states) {
		Optional<? extends Comparable<?>> first = property.getPossibleValues().stream().findFirst();
		if (first.isEmpty())
			return;

		int offset = 0;
		int safety = 100;
		while (safety-- > 0) {
			if (state.getValue(property).equals(first.get())) {
				offset = 99 - safety;
				break;
			}

			state = state.cycle(property);
		}

		safety = 100;
		while (safety-- > 0) {
			if (states.contains(state))
				break;

			states.add(state);

			state = state.cycle(property);
		}

		offset = Mth.clamp(offset, 0, states.size() - 1);
		selectedStateIndex = (offset == 0) ? 0 : (states.size() - offset);
	}

	@Override
	public void tick() {
		ticksOpen++;
		if (!level.getBlockState(pos).is(state.getBlock()))
			Minecraft.getInstance().setScreenAndShow(null);
		super.tick();
	}

	@Override
	protected void renderWindow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		int x = this.width / 2;
		int y = this.height / 2;

		Matrix3x2fStack ms = graphics.pose();

		ms.pushMatrix();
		ms.translate(x, y);

		LegacyRenderSystemBridge.enableBlend();
		LegacyRenderSystemBridge.defaultBlendFunc();

		int mouseOffsetX = mouseX - this.width / 2;
		int mouseOffsetY = mouseY - this.height / 2;

		if (Mth.length(mouseOffsetX, mouseOffsetY) > innerRadius - 5) {
			double theta = Mth.atan2(mouseOffsetX, mouseOffsetY);

			float sectorSize = 360f / allStates.size();

			selectedStateIndex = (int) Math.floor(
				((-AngleHelper.deg(Mth.atan2(mouseOffsetX, mouseOffsetY)) + 180 + sectorSize / 2) % 360)
					/ sectorSize
			);

			renderDirectionIndicator(graphics, theta);
		}

		renderRadialSectors(graphics);

		UIRenderHelper.streak(graphics, 0, 0, 0, 32, 65, Color.BLACK.setAlpha(0.8f));
		UIRenderHelper.streak(graphics, 180, 0, 0, 32, 65, Color.BLACK.setAlpha(0.8f));

		if (selectedPropertyIndex > 0) {
			iconScroll.at(-14, -46).render(graphics, 0, 0);
			iconUp.at(-1, -46).render(graphics, 0, 0);
			graphics.centeredText(font, propertiesForState.get(selectedPropertyIndex - 1).getValue(), 0, -30, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
		}

		if (selectedPropertyIndex < propertiesForState.size() - 1) {
			iconScroll.at(-14, 30).render(graphics, 0, 0);
			iconDown.at(-1, 30).render(graphics, 0, 0);
			graphics.centeredText(font, propertiesForState.get(selectedPropertyIndex + 1).getValue(), 0, 22, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
		}

		graphics.centeredText(font, "Currently", 0, -13, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
		graphics.centeredText(font, "Changing:", 0, -3, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
		graphics.centeredText(font, propertyLabel, 0, 7, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());

		ms.popMatrix();

	}

	private void renderRadialSectors(GuiGraphicsExtractor graphics) {
		int sectors = allStates.size();
		if (sectors < 2)
			return;

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;

		float sectorAngle = 360f / sectors;

		for (int i = 0; i < sectors; i++) {
			Color innerColor = Color.WHITE.setAlpha(0.05f);
			Color outerColor = Color.WHITE.setAlpha(0.3f);

			if (i == selectedStateIndex) {
				innerColor.mixWith(new Color(0.8f, 0.8f, 0.2f, 0.2f), 0.5f);
				outerColor.mixWith(new Color(0.8f, 0.8f, 0.2f, 0.6f), 0.5f);

				UIRenderHelper.drawRadialSector(graphics, outerRadius + 2, outerRadius + 3, -(sectorAngle / 2 + 90), sectorAngle, outerColor, outerColor);
			}

			UIRenderHelper.drawRadialSector(graphics, innerRadius, outerRadius, -(sectorAngle / 2 + 90), sectorAngle, innerColor, outerColor);
			Color c = innerColor.copy().setAlpha(0.5f);
			UIRenderHelper.drawRadialSector(graphics, innerRadius - 3, innerRadius - 2, -(sectorAngle / 2 + 90), sectorAngle, c, c);
		}

		BlockState blockState = allStates.get(selectedStateIndex);
		Property<?> property = propertiesForState.get(selectedPropertyIndex).getKey();
		graphics.centeredText(font, blockState.getValue(property).toString(), 0, 15, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());

	}

	private void renderDirectionIndicator(GuiGraphicsExtractor graphics, double theta) {
		// TODO 26.2: Rebuild this tiny immediate-mode indicator with the GUI submit pipeline.
	}

	private void submitChange() {
		BlockState selectedState = allStates.get(selectedStateIndex);
		if (selectedState != state) {
			net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new RadialWrenchMenuSubmitPacket(pos, selectedState));
		}

		onClose();
	}

	private void withLevel(@Nullable BlockEntity blockEntity, Level newLevel, Runnable action) {
		boolean hasBlockEntity = blockEntity != null;

		Level originalLevel = null;
		if (hasBlockEntity) {
			originalLevel = blockEntity.getLevel();
			blockEntity.setLevel(newLevel);
		}

		try {
			action.run();
		} finally {
			if (hasBlockEntity) {
				//noinspection DataFlowIssue
				blockEntity.setLevel(originalLevel);
			}
		}
	}

	@Override
	public void renderBackground(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
		Color color = BACKGROUND_COLOR
			.scaleAlpha(Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f));

		GuiGraphicsExtractor.fillGradient(0, 0, this.width, this.height, color.getRGB(), color.getRGB());
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		InputConstants.Key mouseKey = InputConstants.getKey(event);
		if (AllKeys.ROTATE_MENU.getKeybind().isActiveAndMatches(mouseKey)) {
			submitChange();
			return true;
		}
		return super.keyReleased(event);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (pButton == InputConstants.MOUSE_BUTTON_LEFT) {
			submitChange();
			return true;
		} else if (pButton == InputConstants.MOUSE_BUTTON_RIGHT) {
			onClose();
			return true;
		}

		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (propertiesForState.size() < 2)
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		int indexDelta = (int) Math.round(Math.signum(-scrollY));

		int newIndex = selectedPropertyIndex + indexDelta;
		if (newIndex < 0)
			return false;

		if (newIndex >= propertiesForState.size())
			return false;

		selectedPropertyIndex = newIndex;
		initForSelectedProperty();

		return true;
	}

	@Override
	public void removed() {
		RadialWrenchHandler.COOLDOWN = 2;

		super.removed();
	}
}
