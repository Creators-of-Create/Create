package com.simibubi.create.foundation.ponder;

import static com.simibubi.create.foundation.ponder.PonderLocalization.LANG_PREFIX;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.BoxElement;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.Theme;
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
import com.simibubi.create.foundation.utility.Couple;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
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

			PonderButton b2 = new PonderButton(x, y)
					.showing(t)
					.withCallback((mX, mY) -> {
						centerScalingOn(mX, mY);
						ScreenOpener.transitionTo(new PonderTagScreen(t));
					});

			widgets.add(b2);
			tagButtons.add(b2);

			LerpedFloat chase = LerpedFloat.linear()
				.startWithValue(0)
				.chase(0, .05f, Chaser.exp(.1));
			tagFades.add(chase);

		});

		/*if (chapter != null) {
			widgets.add(chap = new PonderButton(width - 31 - 24, 31, () -> {
			}).showing(chapter));
		}*/

		GameSettings bindings = client.gameSettings;
		int spacing = 8;
		int bX = (width - 20) / 2 - (70 + 2 * spacing);
		int bY = height - 20 - 31;

		{
			int pX = (width / 2) - 110;
			int pY = bY + 20 + 4;
			int pW = width - 2 * pX;
			widgets.add(progressBar = new PonderProgressBar(this, pX, pY, pW, 1));
		}

		widgets.add(scan = new PonderButton(bX, bY)
				.withShortcut(bindings.keyBindDrop)
				.showing(AllIcons.I_MTD_SCAN)
				.enableFade(0, 5)
				.withCallback(() -> {
					identifyMode = !identifyMode;
					if (!identifyMode)
						scenes.get(index)
								.deselect();
					else
						ponderPartialTicksPaused = client.getRenderPartialTicks();
				}));

		widgets.add(slowMode = new PonderButton(width - 20 - 31, bY)
				.showing(AllIcons.I_MTD_SLOW_MODE)
				.enableFade(0, 5)
				.withCallback(() -> setComfyReadingEnabled(!isComfyReadingEnabled())));

		if (PonderIndex.EDITOR_MODE) {
			widgets.add(userMode = new PonderButton(width - 50 - 31, bY)
					.showing(AllIcons.I_MTD_USER_MODE)
					.enableFade(0, 5)
					.withCallback(() -> userViewMode = !userViewMode));
		}

		bX += 50 + spacing;
		widgets.add(left = new PonderButton(bX, bY)
				.withShortcut(bindings.keyBindLeft)
				.showing(AllIcons.I_MTD_LEFT)
				.enableFade(0, 5)
				.withCallback(() -> this.scroll(false)));

		bX += 20 + spacing;
		widgets.add(close = new PonderButton(bX, bY)
				.withShortcut(bindings.keyBindInventory)
				.showing(AllIcons.I_MTD_CLOSE)
				.enableFade(0, 5)
				.withCallback(this::onClose));

		bX += 20 + spacing;
		widgets.add(right = new PonderButton(bX, bY)
				.withShortcut(bindings.keyBindRight)
				.showing(AllIcons.I_MTD_RIGHT)
				.enableFade(0, 5)
				.withCallback(() -> this.scroll(true)));

		bX += 50 + spacing;
		widgets.add(replay = new PonderButton(bX, bY)
				.withShortcut(bindings.keyBindBack)
				.showing(AllIcons.I_MTD_REPLAY)
				.enableFade(0, 5)
				.withCallback(this::replay));
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
		if (isComfyReadingEnabled())
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

		MainWindow w = client.getWindow();
		double mouseX = client.mouseHelper.getMouseX() * w.getScaledWidth() / w.getWidth();
		double mouseY = client.mouseHelper.getMouseY() * w.getScaledHeight() / w.getHeight();
		SceneTransform t = activeScene.getTransform();
		Vector3d vec1 = t.screenToScene(mouseX, mouseY, 1000, 0);
		Vector3d vec2 = t.screenToScene(mouseX, mouseY, -100, 0);
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
			activeTemplate.placeAndNotifyListeners(world, BlockPos.ZERO, new PlacementSettings(), new Random());
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
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		partialTicks = getPartialTicks();
		RenderSystem.enableBlend();
		renderVisibleScenes(ms, mouseX, mouseY,
			skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		renderWidgets(ms, mouseX, mouseY, identifyMode ? ponderPartialTicksPaused : partialTicks);
	}

	@Override
	public void renderBackground(MatrixStack ms) {
		super.renderBackground(ms);
	}

	protected void renderVisibleScenes(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		renderScene(ms, mouseX, mouseY, index, partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			renderScene(ms, mouseX, mouseY, lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
	}

	protected void renderScene(MatrixStack ms, int mouseX, int mouseY, int i, float partialTicks) {
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
		PonderScene story = scenes.get(i);
		double value = lazyIndex.getValue(client.getRenderPartialTicks());
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
		ms.push();

		// kool shadow fx
		{
			RenderSystem.enableCull();
			RenderSystem.enableDepthTest();
			ms.push();
			ms.translate(story.basePlateOffsetX, 0, story.basePlateOffsetZ);
			ms.scale(1, -1, 1);

			float flash = finishingFlash.getValue(partialTicks) * .9f;
			float alpha = flash;
			flash *= flash;
			flash = ((flash * 2) - 1);
			flash *= flash;
			flash = 1 - flash;

			for (int f = 0; f < 4; f++) {
				ms.translate(story.basePlateSize, 0, 0);
				ms.push();
				ms.translate(0, 0, -1 / 1024f);
				if (flash > 0) {
					ms.push();
					ms.scale(1, .5f + flash * .75f, 1);
					GuiUtils.drawGradientRect(ms.peek()
						.getModel(), 0, 0, -1, -story.basePlateSize, 0, 0x00_c6ffc9,
						ColorHelper.applyAlpha(0xaa_c6ffc9, alpha));
					ms.pop();
				}
				ms.translate(0, 0, 2 / 1024f);
				GuiUtils.drawGradientRect(ms.peek()
					.getModel(), 0, 0, 0, -story.basePlateSize, 4, 0x66_000000, 0x00_000000);
				ms.pop();
				ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90));
			}
			ms.pop();
			RenderSystem.disableCull();
			RenderSystem.disableDepthTest();
		}

		// coords for debug
		if (PonderIndex.EDITOR_MODE && !userViewMode) {

			ms.scale(-1, -1, 1);
			ms.scale(1 / 16f, 1 / 16f, 1 / 16f);
			ms.translate(1, -8, -1 / 64f);

			// X AXIS
			ms.push();
			ms.translate(4, -3, 0);
			ms.translate(0, 0, -2 / 1024f);
			for (int x = 0; x <= bounds.getXSize(); x++) {
				ms.translate(-16, 0, 0);
				textRenderer.draw(ms, x == bounds.getXSize() ? "x" : "" + x, 0, 0, 0xFFFFFFFF);
			}
			ms.pop();

			// Z AXIS
			ms.push();
			ms.scale(-1, 1, 1);
			ms.translate(0, -3, -4);
			ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90));
			ms.translate(-8, -2, 2 / 64f);
			for (int z = 0; z <= bounds.getZSize(); z++) {
				ms.translate(16, 0, 0);
				textRenderer.draw(ms, z == bounds.getZSize() ? "z" : "" + z, 0, 0, 0xFFFFFFFF);
			}
			ms.pop();

			// DIRECTIONS
			ms.push();
			ms.translate(bounds.getXSize() * -8, 0, bounds.getZSize() * 8);
			ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90));
			for (Direction d : Iterate.horizontalDirections) {
				ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
				ms.push();
				ms.translate(0, 0, bounds.getZSize() * 16);
				ms.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90));
				textRenderer.draw(ms, d.name()
					.substring(0, 1), 0, 0, 0x66FFFFFF);
				textRenderer.draw(ms, "|", 2, 10, 0x44FFFFFF);
				textRenderer.draw(ms, ".", 2, 14, 0x22FFFFFF);
				ms.pop();
			}
			ms.pop();
			buffer.draw();
		}

		ms.pop();
		ms.pop();
		RenderSystem.popMatrix();
	}

	protected void renderWidgets(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		float fade = fadeIn.getValue(partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		float indexDiff = Math.abs(lazyIndexValue - index);
		PonderScene activeScene = scenes.get(index);

		boolean noWidgetsHovered = true;
		for (Widget widget : widgets)
			noWidgetsHovered &= !widget.isMouseOver(mouseX, mouseY);

		int tooltipColor = Theme.i(Theme.Key.TEXT_DARKER);
		{
			// Chapter title
			ms.push();
			ms.translate(0, 0, 100);
			int x = 31 + 20 + 8;
			int y = 31;

			String title = activeScene.getTitle();
			int wordWrappedHeight = textRenderer.getWordWrappedHeight(title, left.x - 51);

			int streakHeight = 35 - 9 + wordWrappedHeight;
			UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (150 * fade));
			UIRenderHelper.streak(ms, 180, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (30 * fade));
			new BoxElement()
					.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
					.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
					.at(21, 21, 100)
					.withBounds(30, 30)
					.render(ms);


			GuiGameElement.of(stack)
					.scale(2)
					.at(x - 39, y - 11)
					.render(ms);

			textRenderer.draw(ms, Lang.translate(PONDERING), x, y - 6, tooltipColor);
			y += 8;
			x += 0;
			ms.translate(x, y, 0);
			ms.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(indexDiff * -75));
			ms.translate(0, 0, 5);
			FontHelper.drawSplitString(ms, textRenderer, title, 0, 0, left.x - 51,
				ColorHelper.applyAlpha(Theme.i(Theme.Key.TEXT), 1 - indexDiff));
			ms.pop();

			if (chapter != null) {
				ms.push();

				ms.translate(chap.x - 4 - 4, chap.y, 0);
				UIRenderHelper.streak(ms, 180, 4, 10, 26, (int) (150 * fade));

				drawRightAlignedString(textRenderer, ms, Lang.translate(IN_CHAPTER).getString(), 0, 0, tooltipColor);
				drawRightAlignedString(textRenderer, ms,
					Lang.translate(LANG_PREFIX + "chapter." + chapter.getId()).getString(), 0, 12, Theme.i(Theme.Key.TEXT));

				ms.pop();
			}

			UIRenderHelper.breadcrumbArrow(ms, width / 2 - 20, height - 51, 0, 20, 20, 5, 0x40aa9999, 0x20aa9999);
			UIRenderHelper.breadcrumbArrow(ms, width / 2 + 20, height - 51, 0, -20, 20, -5, 0x40aa9999, 0x20aa9999);
			UIRenderHelper.breadcrumbArrow(ms, width / 2 - 90, height - 51, 0, 70, 20, 5, 0x40aa9999, 0x10aa9999);
			UIRenderHelper.breadcrumbArrow(ms, width / 2 + 90, height - 51, 0, -70, 20, -5, 0x40aa9999, 0x10aa9999);
		}

		if (identifyMode) {
			if (noWidgetsHovered && mouseY < height - 80) {
				ms.push();
				ms.translate(mouseX, mouseY, 100);
				if (hoveredTooltipItem.isEmpty()) {
					IFormattableTextComponent text = Lang.translate(
							IDENTIFY_MODE,
							((IFormattableTextComponent) client.gameSettings.keyBindDrop.getBoundKeyLocalizedText()).formatted(TextFormatting.WHITE)
					).formatted(TextFormatting.GRAY);

					//renderOrderedTooltip(ms, textRenderer.wrapLines(text, width / 3), 0, 0);
					renderWrappedToolTip(ms, textRenderer.getTextHandler().wrapLines(text, width / 3, Style.EMPTY), 0, 0, textRenderer);
					/*String tooltip = Lang
						.createTranslationTextComponent(IDENTIFY_MODE, client.gameSettings.keyBindDrop.getBoundKeyLocalizedText().applyTextStyle(TextFormatting.WHITE))
						.applyTextStyle(TextFormatting.GRAY)
						.getFormattedText();
					renderTooltip(font.listFormattedStringToWidth(tooltip, width / 3), 0, 0);*/
				} else
					renderTooltip(ms, hoveredTooltipItem, 0, 0);
				if (hoveredBlockPos != null && PonderIndex.EDITOR_MODE && !userViewMode) {
					ms.translate(0, -15, 0);
					boolean copied = copiedBlockPos != null && hoveredBlockPos.equals(copiedBlockPos);
					IFormattableTextComponent coords = new StringTextComponent(hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ())
							.formatted(copied ? TextFormatting.GREEN : TextFormatting.GOLD);
					renderTooltip(ms, coords, 0, 0);
				}
				ms.pop();
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

		if (isComfyReadingEnabled())
			slowMode.flash();
		else
			slowMode.dim();

		{
			// Scene overlay
			float scenePT = skipCooling > 0 ? 0 : partialTicks;
			ms.push();
			ms.translate(0, 0, 100);
			renderOverlay(ms, index, scenePT);
			if (indexDiff > 1 / 512f)
				renderOverlay(ms, lazyIndexValue < index ? index - 1 : index + 1, scenePT);
			ms.pop();
		}

		// Widgets
		widgets.forEach(w -> {
			if (w instanceof PonderButton) {
				((PonderButton) w).fade().startWithValue(fade);
			}
		});

		if (index == 0 || index == 1 && lazyIndexValue < index)
			left.fade().startWithValue(lazyIndexValue);
		if (index == scenes.size() - 1 || index == scenes.size() - 2 && lazyIndexValue > index)
			right.fade().startWithValue(scenes.size() - lazyIndexValue - 1);

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
				ms.push();
				LerpedFloat chase = tagFades.get(i);
				PonderButton button = tagButtons.get(i);
				if (button.isMouseOver(mouseX, mouseY)) {
					chase.updateChaseTarget(1);
				} else
					chase.updateChaseTarget(0);

				chase.tickChaser();

				if (highlightAll)
					button.flash();
				else
					button.dim();

				int x = button.x + button.getWidth() + 4;
				int y = button.y - 2;
				ms.translate(x, y + 5 * (1 - fade), 800);

				float fadedWidth = 200 * chase.getValue(partialTicks);
				UIRenderHelper.streak(ms, 0, 0, 12, 26, (int) fadedWidth);

				GL11.glScissor((int) (x * s), 0, (int) (fadedWidth * s), (int) (height * s));
				GL11.glEnable(GL11.GL_SCISSOR_TEST);

				String tagName = this.tags.get(i)
					.getTitle();
				textRenderer.draw(ms, tagName, 3, 8, Theme.i(Theme.Key.TEXT_ACCENT_SLIGHT));

				GL11.glDisable(GL11.GL_SCISSOR_TEST);

				ms.pop();
			});

		ms.push();
		ms.translate(0, 0, 500);
		int tooltipY = height - 16;
		if (scan.isHovered())
			drawCenteredText(ms, textRenderer, Lang.translate(IDENTIFY), scan.x + 10, tooltipY, tooltipColor);
		if (index != 0 && left.isHovered())
			drawCenteredText(ms, textRenderer, Lang.translate(PREVIOUS), left.x + 10, tooltipY, tooltipColor);
		if (close.isHovered())
			drawCenteredText(ms, textRenderer, Lang.translate(CLOSE), close.x + 10, tooltipY, tooltipColor);
		if (index != scenes.size() - 1 && right.isHovered())
			drawCenteredText(ms, textRenderer, Lang.translate(NEXT), right.x + 10, tooltipY, tooltipColor);
		if (replay.isHovered())
			drawCenteredText(ms, textRenderer, Lang.translate(REPLAY), replay.x + 10, tooltipY, tooltipColor);
		if (slowMode.isHovered())
			drawCenteredText(ms, textRenderer, Lang.translate(SLOW_TEXT), slowMode.x + 5, tooltipY, tooltipColor);
		if (PonderIndex.EDITOR_MODE && userMode.isHovered())
			drawCenteredString(ms, textRenderer, "Editor View", userMode.x + 10, tooltipY, tooltipColor);
		ms.pop();
	}

	private void renderOverlay(MatrixStack ms, int i, float partialTicks) {
		if (identifyMode)
			return;
		ms.push();
		PonderScene story = scenes.get(i);
		story.renderOverlay(this, ms, skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		ms.pop();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (identifyMode && hoveredBlockPos != null && PonderIndex.EDITOR_MODE) {
			long handle = client.getWindow()
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
			return Lang.translate(LANG_PREFIX + "chapter." + chapter.getId())
				.getString();

		return stack.getItem()
				.getName()
				.getString();
	}

	public FontRenderer getFontRenderer() {
		return textRenderer;
	}

	protected boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
		boolean hovered = !(mouseX < x || mouseX > x + w);
		hovered &= !(mouseY < y || mouseY > y + h);
		return hovered;
	}

	public static void renderSpeechBox(MatrixStack ms, int x, int y, int w, int h, boolean highlighted, Pointing pointing,
		boolean returnWithLocalTransform) {
		if (!returnWithLocalTransform)
			ms.push();

		int boxX = x;
		int boxY = y;
		int divotX = x;
		int divotY = y;
		int divotRotation = 0;
		int divotSize = 8;
		int distance = 1;
		int divotRadius = divotSize / 2;
		Couple<Color> borderColors = Theme.p(highlighted ? Theme.Key.PONDER_HIGHLIGHT : Theme.Key.PONDER_IDLE);
		Color c;

		switch (pointing) {
		default:
		case DOWN:
			divotRotation = 0;
			boxX -= w / 2;
			boxY -= h + divotSize + 1 + distance;
			divotX -= divotRadius;
			divotY -= divotSize + distance;
			c = borderColors.getSecond();
			break;
		case LEFT:
			divotRotation = 90;
			boxX += divotSize + 1 + distance;
			boxY -= h / 2;
			divotX += distance;
			divotY -= divotRadius;
			c = ColorHelper.mixColors(borderColors, 0.5f);
			break;
		case RIGHT:
			divotRotation = 270;
			boxX -= w + divotSize + 1 + distance;
			boxY -= h / 2;
			divotX -= divotSize + distance;
			divotY -= divotRadius;
			c = ColorHelper.mixColors(borderColors, 0.5f);
			break;
		case UP:
			divotRotation = 180;
			boxX -= w / 2;
			boxY += divotSize + 1 + distance;
			divotX -= divotRadius;
			divotY += distance;
			c = borderColors.getFirst();
			break;
		}

		new BoxElement()
				.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(borderColors)
				.at(boxX, boxY, 100)
				.withBounds(w, h)
				.render(ms);

		ms.push();
		ms.translate(divotX + divotRadius, divotY + divotRadius, 10);
		ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(divotRotation));
		ms.translate(-divotRadius, -divotRadius, 0);
		AllGuiTextures.SPEECH_TOOLTIP_BACKGROUND.draw(ms, 0, 0);
		AllGuiTextures.SPEECH_TOOLTIP_COLOR.draw(ms, 0, 0, c);
		ms.pop();

		if (returnWithLocalTransform) {
			ms.translate(boxX, boxY, 0);
			return;
		}

		ms.pop();

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

	public void drawRightAlignedString(FontRenderer fontRenderer, MatrixStack ms, String string, int x, int y,
		int color) {
		fontRenderer.draw(ms, string, (float) (x - fontRenderer.getStringWidth(string)), (float) y, color);
	}

	public boolean isComfyReadingEnabled() {
		return AllConfigs.CLIENT.comfyReading.get();
	}

	public void setComfyReadingEnabled(boolean slowTextMode) {
		AllConfigs.CLIENT.comfyReading.set(slowTextMode);
	}

}
