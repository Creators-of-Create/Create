package com.simibubi.create.foundation.ponder;

import static com.simibubi.create.foundation.ponder.PonderLocalization.LANG_PREFIX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.PonderScene.SceneTransform;
import com.simibubi.create.foundation.ponder.content.DebugScenes;
import com.simibubi.create.foundation.ponder.content.PonderChapter;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.content.PonderTag;
import com.simibubi.create.foundation.ponder.content.PonderTagScreen;
import com.simibubi.create.foundation.ponder.elements.TextWindowElement;
import com.simibubi.create.foundation.ponder.ui.PonderButton;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.FontHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.ClipboardHelper;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.registries.ForgeRegistries;

public class PonderUI extends NavigatableSimiScreen {

	public static int ponderTicks;
	public static float ponderPartialTicksPaused;

	public static final String PONDERING = LANG_PREFIX + "pondering";
	public static final String IDENTIFY_MODE = LANG_PREFIX + "identify_mode";
	public static final String IN_CHAPTER = LANG_PREFIX + "in_chapter";
	public static final String IDENTIFY = LANG_PREFIX + "identify";
	public static final String PREVIOUS = LANG_PREFIX + "previous";
	public static final String CLOSE = LANG_PREFIX + "close";
	public static final String NEXT = LANG_PREFIX + "next";
	public static final String REPLAY = LANG_PREFIX + "replay";
	public static final String SLOW_TEXT = LANG_PREFIX + "slow_text";

	private List<PonderScene> scenes;
	private List<PonderTag> tags;
	private List<PonderButton> tagButtons;
	private List<LerpedFloat> tagFades;
	private LerpedFloat fadeIn;
	ItemStack stack;
	PonderChapter chapter = null;

	private boolean userViewMode;
	private boolean slowTextMode;
	private boolean identifyMode;
	private ItemStack hoveredTooltipItem;
	private BlockPos hoveredBlockPos;

	private ClipboardHelper clipboardHelper;
	private BlockPos copiedBlockPos;

	private LerpedFloat finishingFlash;
	private int finishingFlashWarmup = 0;

	private LerpedFloat lazyIndex;
	private int index = 0;
	private PonderTag referredToByTag;

	private PonderButton left, right, scan, chap, userMode, close, replay, slowMode;
	private PonderProgressBar progressBar;
	private int skipCooling = 0;

	private int extendedTickLength = 0;
	private int extendedTickTimer = 0;

	public static PonderUI of(ResourceLocation id) {
		return new PonderUI(PonderRegistry.compile(id));
	}

	public static PonderUI of(ItemStack item) {
		return new PonderUI(PonderRegistry.compile(item.getItem()
			.getRegistryName()));
	}

	public static PonderUI of(ItemStack item, PonderTag tag) {
		PonderUI ponderUI = new PonderUI(PonderRegistry.compile(item.getItem()
			.getRegistryName()));
		ponderUI.referredToByTag = tag;
		return ponderUI;
	}

	public static PonderUI of(PonderChapter chapter) {
		PonderUI ui = new PonderUI(PonderRegistry.compile(chapter));
		ui.chapter = chapter;
		return ui;
	}

	PonderUI(List<PonderScene> scenes) {
		ResourceLocation component = scenes.get(0).component;
		if (ForgeRegistries.ITEMS.containsKey(component))
			stack = new ItemStack(ForgeRegistries.ITEMS.getValue(component));
		else
			stack = new ItemStack(ForgeRegistries.BLOCKS.getValue(component));

		tags = new ArrayList<>(PonderRegistry.tags.getTags(component));
		this.scenes = scenes;
		if (scenes.isEmpty()) {
			List<PonderStoryBoardEntry> l = Collections.singletonList(new PonderStoryBoardEntry(DebugScenes::empty,
				"debug/scene_1", new ResourceLocation("minecraft", "stick")));
			scenes.addAll(PonderRegistry.compile(l));
		}
		lazyIndex = LerpedFloat.linear()
			.startWithValue(index);
		fadeIn = LerpedFloat.linear()
			.startWithValue(0)
			.chase(1, .1f, Chaser.EXP);
		clipboardHelper = new ClipboardHelper();
		finishingFlash = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, .1f, Chaser.EXP);
	}

	@Override
	protected void init() {
		widgets.clear();
		super.init();

		tagButtons = new ArrayList<>();
		tagFades = new ArrayList<>();

		tags.forEach(t -> {
			int i = tagButtons.size();
			int x = 31;
			int y = 81 + i * 30;
			PonderButton b = new PonderButton(x, y, (mouseX, mouseY) -> {
				centerScalingOn(mouseX, mouseY);
				ScreenOpener.transitionTo(new PonderTagScreen(t));
			}).showing(t);

			widgets.add(b);
			tagButtons.add(b);

			LerpedFloat chase = LerpedFloat.linear()
				.startWithValue(0)
				.chase(0, .05f, Chaser.exp(.1));
			tagFades.add(chase);

		});

		if (chapter != null) {
			widgets.add(chap = new PonderButton(width - 31 - 24, 31, () -> {
			}).showing(chapter));
		}

		GameSettings bindings = minecraft.gameSettings;
		int spacing = 8;
		int bX = (width - 20) / 2 - (70 + 2 * spacing);
		int bY = height - 20 - 31;

		{
			int pX = (width / 2) - 110;
			int pY = bY + PonderButton.SIZE + 4;
			int pW = width - 2 * pX;
			widgets.add(progressBar = new PonderProgressBar(this, pX, pY, pW, 1));
		}

		widgets.add(scan = new PonderButton(bX, bY, () -> {
			identifyMode = !identifyMode;
			if (!identifyMode)
				scenes.get(index)
					.deselect();
			else
				ponderPartialTicksPaused = minecraft.getRenderPartialTicks();
		}).showing(AllIcons.I_MTD_SCAN)
			.shortcut(bindings.keyBindDrop)
			.fade(0, -1));

		widgets.add(slowMode = new PonderButton(width - 20 - 31, bY, () -> {
			slowTextMode = !slowTextMode;
		}).showing(AllIcons.I_MTD_SLOW_MODE)
			.fade(0, -1));
		
		if (PonderIndex.EDITOR_MODE) {
			widgets.add(userMode = new PonderButton(width - 50 - 31, bY, () -> {
				userViewMode = !userViewMode;
			}).showing(AllIcons.I_MTD_USER_MODE)
				.fade(0, -1));
		}

		bX += 50 + spacing;
		widgets.add(left = new PonderButton(bX, bY, () -> this.scroll(false)).showing(AllIcons.I_MTD_LEFT)
			.shortcut(bindings.keyBindLeft)
			.fade(0, -1));

		bX += 20 + spacing;
		widgets.add(close = new PonderButton(bX, bY, this::onClose).showing(AllIcons.I_MTD_CLOSE)
			.shortcut(bindings.keyBindInventory)
			.fade(0, -1));

		bX += 20 + spacing;
		widgets.add(right = new PonderButton(bX, bY, () -> this.scroll(true)).showing(AllIcons.I_MTD_RIGHT)
			.shortcut(bindings.keyBindRight)
			.fade(0, -1));

		bX += 50 + spacing;
		widgets.add(replay = new PonderButton(bX, bY, this::replay).showing(AllIcons.I_MTD_REPLAY)
			.shortcut(bindings.keyBindBack)
			.fade(0, -1));
	}

	@Override
	public void tick() {
		super.tick();

		if (skipCooling > 0)
			skipCooling--;

		if (referredToByTag != null) {
			for (int i = 0; i < scenes.size(); i++) {
				PonderScene ponderScene = scenes.get(i);
				if (!ponderScene.tags.contains(referredToByTag))
					continue;
				if (i == index)
					break;
				scenes.get(index)
					.fadeOut();
				index = i;
				scenes.get(index)
					.begin();
				lazyIndex.chase(index, 1 / 4f, Chaser.EXP);
				identifyMode = false;
				break;
			}
			referredToByTag = null;
		}

		lazyIndex.tickChaser();
		fadeIn.tickChaser();
		finishingFlash.tickChaser();
		PonderScene activeScene = scenes.get(index);

		extendedTickLength = 0;
		if (slowTextMode) 
			activeScene.forEachVisible(TextWindowElement.class, twe -> extendedTickLength = 2);
		
		if (extendedTickTimer == 0) {
			if (!identifyMode) {
				ponderTicks++;
				if (skipCooling == 0)
					activeScene.tick();
			}

			if (!identifyMode) {
				float lazyIndexValue = lazyIndex.getValue();
				if (Math.abs(lazyIndexValue - index) > 1 / 512f)
					scenes.get(lazyIndexValue < index ? index - 1 : index + 1)
						.tick();
			}
			extendedTickTimer = extendedTickLength;
		} else
			extendedTickTimer--;

		progressBar.tick();

		if (activeScene.currentTime == activeScene.totalTime - 1)
			finishingFlashWarmup = 30;
		if (finishingFlashWarmup > 0) {
			finishingFlashWarmup--;
			if (finishingFlashWarmup == 0) {
				finishingFlash.setValue(1);
				finishingFlash.setValue(1);
			}
		}

		updateIdentifiedItem(activeScene);
	}

	public PonderScene getActiveScene() {
		return scenes.get(index);
	}

	public void seekToTime(int time) {
		if (getActiveScene().currentTime > time)
			replay();

		getActiveScene().seekToTime(time);
		if (time != 0)
			coolDownAfterSkip();
	}

	public void updateIdentifiedItem(PonderScene activeScene) {
		hoveredTooltipItem = ItemStack.EMPTY;
		hoveredBlockPos = null;
		if (!identifyMode)
			return;

		MainWindow w = minecraft.getWindow();
		double mouseX = minecraft.mouseHelper.getMouseX() * w.getScaledWidth() / w.getWidth();
		double mouseY = minecraft.mouseHelper.getMouseY() * w.getScaledHeight() / w.getHeight();
		SceneTransform t = activeScene.getTransform();
		Vec3d vec1 = t.screenToScene(mouseX, mouseY, 1000, 0);
		Vec3d vec2 = t.screenToScene(mouseX, mouseY, -100, 0);
		Pair<ItemStack, BlockPos> pair = activeScene.rayTraceScene(vec1, vec2);
		hoveredTooltipItem = pair.getFirst();
		hoveredBlockPos = pair.getSecond();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (scroll(delta > 0))
			return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	protected void replay() {
		identifyMode = false;
		PonderScene scene = scenes.get(index);

		if (hasShiftDown()) {
			List<PonderStoryBoardEntry> list = PonderRegistry.all.get(scene.component);
			PonderStoryBoardEntry sb = list.get(index);
			Template activeTemplate = PonderRegistry.loadSchematic(sb.getSchematicName());
			PonderWorld world = new PonderWorld(BlockPos.ZERO, Minecraft.getInstance().world);
			activeTemplate.addBlocksToWorld(world, BlockPos.ZERO, new PlacementSettings());
			world.createBackup();
			scene = PonderRegistry.compileScene(index, sb, world);
			scene.begin();
			scenes.set(index, scene);
		}

		scene.begin();
	}

	protected boolean scroll(boolean forward) {
		int prevIndex = index;
		index = forward ? index + 1 : index - 1;
		index = MathHelper.clamp(index, 0, scenes.size() - 1);
		if (prevIndex != index) {// && Math.abs(index - lazyIndex.getValue()) < 1.5f) {
			scenes.get(prevIndex)
				.fadeOut();
			scenes.get(index)
				.begin();
			lazyIndex.chase(index, 1 / 4f, Chaser.EXP);
			identifyMode = false;
			return true;
		} else
			index = prevIndex;
		return false;
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		partialTicks = getPartialTicks();
		RenderSystem.enableBlend();
		renderVisibleScenes(mouseX, mouseY,
			skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		renderWidgets(mouseX, mouseY, identifyMode ? ponderPartialTicksPaused : partialTicks);
	}

	@Override
	public void renderBackground() {
		super.renderBackground();
	}

	protected void renderVisibleScenes(int mouseX, int mouseY, float partialTicks) {
		renderScene(mouseX, mouseY, index, partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			renderScene(mouseX, mouseY, lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
	}

	protected void renderScene(int mouseX, int mouseY, int i, float partialTicks) {
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
		PonderScene story = scenes.get(i);
		MatrixStack ms = new MatrixStack();
		double value = lazyIndex.getValue(minecraft.getRenderPartialTicks());
		double diff = i - value;
		double slide = MathHelper.lerp(diff * diff, 200, 600) * diff;

		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();

		RenderSystem.pushMatrix();

		// has to be outside of MS transforms, important for vertex sorting
		RenderSystem.translated(0, 0, 800);

		ms.push();
		ms.translate(0, 0, -800);
		story.transform.updateScreenParams(width, height, slide);
		story.transform.apply(ms, partialTicks, false);
		story.transform.updateSceneRVE(partialTicks);
		story.renderScene(buffer, ms, partialTicks);
		buffer.draw();

		MutableBoundingBox bounds = story.getBounds();
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(ms.peek()
			.getModel());

		// kool shadow fx
		{
			RenderSystem.enableCull();
			RenderSystem.enableDepthTest();
			RenderSystem.pushMatrix();
			RenderSystem.translated(story.basePlateOffsetX, 0, story.basePlateOffsetZ);
			RenderSystem.scaled(1, -1, 1);

			float flash = finishingFlash.getValue(partialTicks) * .9f;
			float alpha = flash;
			flash *= flash;
			flash = ((flash * 2) - 1);
			flash *= flash;
			flash = 1 - flash;

			for (int f = 0; f < 4; f++) {
				RenderSystem.translated(story.basePlateSize, 0, 0);
				RenderSystem.pushMatrix();
				RenderSystem.translated(0, 0, -1 / 1024f);
				if (flash > 0) {
					RenderSystem.pushMatrix();
					RenderSystem.scaled(1, .5 + flash * .75, 1);
					GuiUtils.drawGradientRect(0, 0, -1, -story.basePlateSize, 0, 0x00_c6ffc9,
						ColorHelper.applyAlpha(0xaa_c6ffc9, alpha));
					RenderSystem.popMatrix();
				}
				RenderSystem.translated(0, 0, 2 / 1024f);
				GuiUtils.drawGradientRect(0, 0, 0, -story.basePlateSize, 4, 0x66_000000, 0x00_000000);
				RenderSystem.popMatrix();
				RenderSystem.rotatef(-90, 0, 1, 0);
			}
			RenderSystem.popMatrix();
			RenderSystem.disableCull();
			RenderSystem.disableDepthTest();
		}

		// coords for debug
		if (PonderIndex.EDITOR_MODE && !userViewMode) {

			RenderSystem.scaled(-1, -1, 1);
			RenderSystem.scaled(1 / 16d, 1 / 16d, 1 / 16d);
			RenderSystem.translated(1, -8, -1 / 64f);

			// X AXIS
			RenderSystem.pushMatrix();
			RenderSystem.translated(4, -3, 0);
			RenderSystem.translated(0, 0, -2 / 1024f);
			for (int x = 0; x <= bounds.getXSize(); x++) {
				RenderSystem.translated(-16, 0, 0);
				font.drawString(x == bounds.getXSize() ? "x" : "" + x, 0, 0, 0xFFFFFFFF);
			}
			RenderSystem.popMatrix();

			// Z AXIS
			RenderSystem.pushMatrix();
			RenderSystem.scaled(-1, 1, 1);
			RenderSystem.translated(0, -3, -4);
			RenderSystem.rotatef(-90, 0, 1, 0);
			RenderSystem.translated(-8, -2, 2 / 64f);
			for (int z = 0; z <= bounds.getZSize(); z++) {
				RenderSystem.translated(16, 0, 0);
				font.drawString(z == bounds.getZSize() ? "z" : "" + z, 0, 0, 0xFFFFFFFF);
			}
			RenderSystem.popMatrix();

			// DIRECTIONS
			RenderSystem.pushMatrix();
			RenderSystem.translated(bounds.getXSize() * -8, 0, bounds.getZSize() * 8);
			RenderSystem.rotatef(-90, 0, 1, 0);
			for (Direction d : Iterate.horizontalDirections) {
				RenderSystem.rotatef(90, 0, 1, 0);
				RenderSystem.pushMatrix();
				RenderSystem.translated(0, 0, bounds.getZSize() * 16);
				RenderSystem.rotatef(-90, 1, 0, 0);
				font.drawString(d.name()
					.substring(0, 1), 0, 0, 0x66FFFFFF);
				font.drawString("|", 2, 10, 0x44FFFFFF);
				font.drawString(".", 2, 14, 0x22FFFFFF);
				RenderSystem.popMatrix();
			}
			RenderSystem.popMatrix();
			buffer.draw();
		}

		RenderSystem.popMatrix();
		ms.pop();
		RenderSystem.popMatrix();
	}

	protected void renderWidgets(int mouseX, int mouseY, float partialTicks) {
		float fade = fadeIn.getValue(partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		float indexDiff = Math.abs(lazyIndexValue - index);
		PonderScene activeScene = scenes.get(index);
		int textColor = 0xeeeeee;

		boolean noWidgetsHovered = true;
		for (Widget widget : widgets)
			noWidgetsHovered &= !widget.isMouseOver(mouseX, mouseY);

		int tooltipColor = 0xffa3a3a3;
		{
			// Chapter title
			RenderSystem.pushMatrix();
			RenderSystem.translated(0, 0, 800);
			int x = 31 + 20 + 8;
			int y = 31;

			String title = activeScene.getTitle();
			int wordWrappedHeight = font.getWordWrappedHeight(title, left.x - 51);

			int streakHeight = 35 - 9 + wordWrappedHeight;
			UIRenderHelper.streak(0, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (150 * fade), 0x101010);
			UIRenderHelper.streak(180, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (30 * fade), 0x101010);
			renderBox(21, 21, 30, 30, false);

			GuiGameElement.of(stack)
				.at(x - 39, y - 11)
				.scale(2)
				.render();

			drawString(font, Lang.translate(PONDERING), x, y - 6, tooltipColor);
			y += 8;
			x += 0;
			// RenderSystem.translated(0, 3 * (indexDiff), 0);
			RenderSystem.translated(x, y, 0);
			RenderSystem.rotatef(indexDiff * -75, 1, 0, 0);
			RenderSystem.translated(0, 0, 5);
			FontHelper.drawSplitString(font, title, 0, 0, left.x - 51,
				ColorHelper.applyAlpha(textColor, 1 - indexDiff));
			RenderSystem.popMatrix();

			if (chapter != null) {
				RenderSystem.pushMatrix();

				RenderSystem.translated(chap.x - 4 - 4, chap.y, 0);
				UIRenderHelper.streak(180, 4, 10, 26, (int) (150 * fade), 0x101010);

				drawRightAlignedString(font, Lang.translate(IN_CHAPTER), 0, 0, tooltipColor);
				drawRightAlignedString(font,
					Lang.translate(LANG_PREFIX + "chapter." + chapter.getId()), 0, 12, 0xffeeeeee);

				RenderSystem.popMatrix();
			}

			UIRenderHelper.breadcrumbArrow(width / 2 - 20, height - 51, 20, 20, 5, 0x40aa9999, 0x20aa9999);
			UIRenderHelper.breadcrumbArrow(width / 2 + 20, height - 51, -20, 20, -5, 0x40aa9999, 0x20aa9999);
			UIRenderHelper.breadcrumbArrow(width / 2 - 90, height - 51, 70, 20, 5, 0x40aa9999, 0x10aa9999);
			UIRenderHelper.breadcrumbArrow(width / 2 + 90, height - 51, -70, 20, -5, 0x40aa9999, 0x10aa9999);
		}

		if (identifyMode) {
			if (noWidgetsHovered && mouseY < height - 80) {
				RenderSystem.pushMatrix();
				RenderSystem.translated(mouseX, mouseY, 100);
				if (hoveredTooltipItem.isEmpty()) {
					String tooltip = Lang
						.createTranslationTextComponent(IDENTIFY_MODE,
							new StringTextComponent(minecraft.gameSettings.keyBindDrop.getKeyBinding()
								.getLocalizedName()).applyTextStyle(TextFormatting.WHITE))
						.applyTextStyle(TextFormatting.GRAY)
						.getFormattedText();
					renderTooltip(font.listFormattedStringToWidth(tooltip, width / 3), 0, 0);
				} else
					renderTooltip(hoveredTooltipItem, 0, 0);
				if (hoveredBlockPos != null && PonderIndex.EDITOR_MODE && !userViewMode) {
					RenderSystem.translated(0, -15, 0);
					boolean copied = copiedBlockPos != null && hoveredBlockPos.equals(copiedBlockPos);
					String coords = new StringTextComponent(
						hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ())
							.applyTextStyles(copied ? TextFormatting.GREEN : TextFormatting.GOLD)
							.getFormattedText();
					renderTooltip(coords, 0, 0);
				}
				RenderSystem.popMatrix();
			}
			scan.flash();
		} else {
			scan.dim();
		}

		if (PonderIndex.EDITOR_MODE) {
			if (userViewMode)
				userMode.flash();
			else
				userMode.dim();
		}
		
		if (slowTextMode)
			slowMode.flash();
		else
			slowMode.dim();

		{
			// Scene overlay
			float scenePT = skipCooling > 0 ? 0 : partialTicks;
			RenderSystem.pushMatrix();
			RenderSystem.translated(0, 0, 100);
			renderOverlay(index, scenePT);
			if (indexDiff > 1 / 512f)
				renderOverlay(lazyIndexValue < index ? index - 1 : index + 1, scenePT);
			RenderSystem.popMatrix();
		}

		// Widgets
		widgets.forEach(w -> {
			if (w instanceof PonderButton) {
				PonderButton mtdButton = (PonderButton) w;
				mtdButton.fade(fade);
			}
		});

		if (index == 0 || index == 1 && lazyIndexValue < index)
			left.fade(lazyIndexValue);
		if (index == scenes.size() - 1 || index == scenes.size() - 2 && lazyIndexValue > index)
			right.fade(scenes.size() - lazyIndexValue - 1);

		boolean finished = activeScene.isFinished();
		if (finished)
			right.flash();
		else
			right.dim();

		// Tags
		List<PonderTag> sceneTags = activeScene.tags;
		boolean highlightAll = sceneTags.contains(PonderTag.Highlight.ALL);
		double s = Minecraft.getInstance()
			.getWindow()
			.getGuiScaleFactor();
		IntStream.range(0, tagButtons.size())
			.forEach(i -> {
				RenderSystem.pushMatrix();
				LerpedFloat chase = tagFades.get(i);
				PonderButton button = tagButtons.get(i);
				if (button.isMouseOver(mouseX, mouseY)) {
					chase.updateChaseTarget(1);
				} else
					chase.updateChaseTarget(0);

				chase.tickChaser();

				if (highlightAll || sceneTags.contains(this.tags.get(i)))
					button.flash();
				else
					button.dim();

				int x = button.x + button.getWidth() + 4;
				int y = button.y - 2;
				RenderSystem.translated(x, y + 5 * (1 - fade), 800);

				float fadedWidth = 200 * chase.getValue(partialTicks);
				UIRenderHelper.streak(0, 0, 12, 26, (int) fadedWidth, 0x101010);

				GL11.glScissor((int) (x * s), 0, (int) (fadedWidth * s), (int) (height * s));
				GL11.glEnable(GL11.GL_SCISSOR_TEST);

				String tagName = this.tags.get(i)
					.getTitle();
				drawString(tagName, 3, 8, 0xffeedd);

				GL11.glDisable(GL11.GL_SCISSOR_TEST);

				RenderSystem.popMatrix();
			});

		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 0, 500);
		int tooltipY = height - 16;
		if (scan.isHovered())
			drawCenteredString(font, Lang.translate(IDENTIFY), scan.x + 10, tooltipY, tooltipColor);
		if (index != 0 && left.isHovered())
			drawCenteredString(font, Lang.translate(PREVIOUS), left.x + 10, tooltipY, tooltipColor);
		if (close.isHovered())
			drawCenteredString(font, Lang.translate(CLOSE), close.x + 10, tooltipY, tooltipColor);
		if (index != scenes.size() - 1 && right.isHovered())
			drawCenteredString(font, Lang.translate(NEXT), right.x + 10, tooltipY, tooltipColor);
		if (replay.isHovered())
			drawCenteredString(font, Lang.translate(REPLAY), replay.x + 10, tooltipY, tooltipColor);
		if (slowMode.isHovered())
			drawCenteredString(font, Lang.translate(SLOW_TEXT), slowMode.x + 5, tooltipY, tooltipColor);
		if (PonderIndex.EDITOR_MODE && userMode.isHovered())
			drawCenteredString(font, "Editor View", userMode.x + 10, tooltipY, tooltipColor);
		RenderSystem.popMatrix();
	}

	protected void lowerButtonGroup(int index, int mouseX, int mouseY, float fade, AllIcons icon, KeyBinding key) {
		int bWidth = 20;
		int bHeight = 20;
		int bX = (width - bWidth) / 2 + (index - 1) * (bWidth + 8);
		int bY = height - bHeight - 31;

		RenderSystem.pushMatrix();
		if (fade < fadeIn.getChaseTarget())
			RenderSystem.translated(0, (1 - fade) * 5, 0);
		boolean hovered = isMouseOver(mouseX, mouseY, bX, bY, bWidth, bHeight);
		renderBox(bX, bY, bWidth, bHeight, hovered);
		icon.draw(bX + 2, bY + 2);
		drawCenteredString(font, key.getLocalizedName(), bX + bWidth / 2 + 8, bY + bHeight - 6, 0xff606060);
		RenderSystem.popMatrix();
	}

	private void renderOverlay(int i, float partialTicks) {
		if (identifyMode)
			return;
		RenderSystem.pushMatrix();
		PonderScene story = scenes.get(i);
		MatrixStack ms = new MatrixStack();
		story.renderOverlay(this, ms, skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		RenderSystem.popMatrix();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		MutableBoolean handled = new MutableBoolean(false);
		widgets.forEach(w -> {
			if (handled.booleanValue())
				return;
			if (!w.isMouseOver(x, y))
				return;
			if (w instanceof PonderButton) {
				PonderButton mtdButton = (PonderButton) w;
				mtdButton.runCallback(x, y);
				handled.setTrue();
				return;
			}
		});

		if (handled.booleanValue())
			return true;

		if (identifyMode && hoveredBlockPos != null && PonderIndex.EDITOR_MODE) {
			long handle = minecraft.getWindow()
				.getHandle();
			if (copiedBlockPos != null && button == 1) {
				clipboardHelper.setClipboardString(handle,
					"util.select.fromTo(" + copiedBlockPos.getX() + ", " + copiedBlockPos.getY() + ", "
						+ copiedBlockPos.getZ() + ", " + hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", "
						+ hoveredBlockPos.getZ() + ")");
				copiedBlockPos = hoveredBlockPos;
				return true;
			}

			if (hasShiftDown())
				clipboardHelper.setClipboardString(handle, "util.select.position(" + hoveredBlockPos.getX() + ", "
					+ hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")");
			else
				clipboardHelper.setClipboardString(handle, "util.grid.at(" + hoveredBlockPos.getX() + ", "
					+ hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")");
			copiedBlockPos = hoveredBlockPos;
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		GameSettings settings = Minecraft.getInstance().gameSettings;
		int sCode = settings.keyBindBack.getKey()
			.getKeyCode();
		int aCode = settings.keyBindLeft.getKey()
			.getKeyCode();
		int dCode = settings.keyBindRight.getKey()
			.getKeyCode();
		int qCode = settings.keyBindDrop.getKey()
			.getKeyCode();

		if (code == sCode) {
			replay();
			return true;
		}

		if (code == aCode) {
			scroll(false);
			return true;
		}

		if (code == dCode) {
			scroll(true);
			return true;
		}

		if (code == qCode) {
			identifyMode = !identifyMode;
			if (!identifyMode)
				scenes.get(index)
					.deselect();
			return true;
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	protected String getBreadcrumbTitle() {
		if (chapter != null)
			return Lang.translate(LANG_PREFIX + "chapter." + chapter.getId());

		return stack.getItem()
			.getName()
			.getFormattedText();
	}

	public FontRenderer getFontRenderer() {
		return font;
	}

	protected boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
		boolean hovered = !(mouseX < x || mouseX > x + w);
		hovered &= !(mouseY < y || mouseY > y + h);
		return hovered;
	}

	public void drawString(String s, int x, int y, int color) {
		drawString(font, s, x, y, color);
	}

	public static void renderBox(int x, int y, int w, int h, boolean highlighted) {
		renderBox(x, y, w, h, 0xff000000, highlighted ? 0xf0ffeedd : 0x40ffeedd, highlighted ? 0x60ffeedd : 0x20ffeedd);
	}

	public static void renderSpeechBox(int x, int y, int w, int h, boolean highlighted, Pointing pointing,
		boolean returnWithLocalTransform) {
		if (!returnWithLocalTransform)
			RenderSystem.pushMatrix();

		int boxX = x;
		int boxY = y;
		int divotX = x;
		int divotY = y;
		int divotRotation = 0;
		int divotSize = 8;
		int distance = 1;
		int divotRadius = divotSize / 2;

		switch (pointing) {
		default:
		case DOWN:
			divotRotation = 0;
			boxX -= w / 2;
			boxY -= h + divotSize + 1 + distance;
			divotX -= divotRadius;
			divotY -= divotSize + distance;
			break;
		case LEFT:
			divotRotation = 90;
			boxX += divotSize + 1 + distance;
			boxY -= h / 2;
			divotX += distance;
			divotY -= divotRadius;
			break;
		case RIGHT:
			divotRotation = 270;
			boxX -= w + divotSize + 1 + distance;
			boxY -= h / 2;
			divotX -= divotSize + distance;
			divotY -= divotRadius;
			break;
		case UP:
			divotRotation = 180;
			boxX -= w / 2;
			boxY += divotSize + 1 + distance;
			divotX -= divotRadius;
			divotY += distance;
			break;
		}

		renderBox(boxX, boxY, w, h, highlighted);

		RenderSystem.pushMatrix();
		AllGuiTextures toRender = highlighted ? AllGuiTextures.SPEECH_TOOLTIP_HIGHLIGHT : AllGuiTextures.SPEECH_TOOLTIP;
		RenderSystem.translated(divotX + divotRadius, divotY + divotRadius, 10);
		RenderSystem.rotatef(divotRotation, 0, 0, 1);
		RenderSystem.translated(-divotRadius, -divotRadius, 0);
		toRender.draw(0, 0);
		RenderSystem.popMatrix();

		if (returnWithLocalTransform) {
			RenderSystem.translated(boxX, boxY, 0);
			return;
		}

		RenderSystem.popMatrix();

	}

	public static void renderBox(int x, int y, int w, int h, int backgroundColor, int borderColorStart,
		int borderColorEnd) {
		int z = 100;
		GuiUtils.drawGradientRect(z, x - 3, y - 4, x + w + 3, y - 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(z, x - 3, y + h + 3, x + w + 3, y + h + 4, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(z, x - 3, y - 3, x + w + 3, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(z, x - 4, y - 3, x - 3, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(z, x + w + 3, y - 3, x + w + 4, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(z, x - 3, y - 3 + 1, x - 3 + 1, y + h + 3 - 1, borderColorStart, borderColorEnd);
		GuiUtils.drawGradientRect(z, x + w + 2, y - 3 + 1, x + w + 3, y + h + 3 - 1, borderColorStart, borderColorEnd);
		GuiUtils.drawGradientRect(z, x - 3, y - 3, x + w + 3, y - 3 + 1, borderColorStart, borderColorStart);
		GuiUtils.drawGradientRect(z, x - 3, y + h + 2, x + w + 3, y + h + 3, borderColorEnd, borderColorEnd);
	}

	public ItemStack getHoveredTooltipItem() {
		return hoveredTooltipItem;
	}

	public ItemStack getSubject() {
		return stack;
	}

	@Override
	public boolean isEquivalentTo(NavigatableSimiScreen other) {
		if (other instanceof PonderUI)
			return stack.isItemEqual(((PonderUI) other).stack);
		return super.isEquivalentTo(other);
	}

	@Override
	public void shareContextWith(NavigatableSimiScreen other) {
		if (other instanceof PonderUI) {
			PonderUI ponderUI = (PonderUI) other;
			ponderUI.referredToByTag = referredToByTag;
		}
	}

	public static float getPartialTicks() {
		float renderPartialTicks = Minecraft.getInstance()
			.getRenderPartialTicks();

		if (Minecraft.getInstance().currentScreen instanceof PonderUI) {
			PonderUI ui = (PonderUI) Minecraft.getInstance().currentScreen;
			if (ui.identifyMode)
				return ponderPartialTicksPaused;

			return (renderPartialTicks + (ui.extendedTickLength - ui.extendedTickTimer)) / (ui.extendedTickLength + 1);
		}

		return renderPartialTicks;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	public void coolDownAfterSkip() {
		skipCooling = 15;
	}
	
	@Override
	public void removed() {
		super.removed();
		hoveredTooltipItem = ItemStack.EMPTY;
	}

}
