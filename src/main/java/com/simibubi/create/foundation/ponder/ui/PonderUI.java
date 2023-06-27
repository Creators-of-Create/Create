package com.simibubi.create.foundation.ponder.ui;

import static com.simibubi.create.foundation.ponder.PonderLocalization.LANG_PREFIX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.BoxElement;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.ponder.PonderChapter;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderScene.SceneTransform;
import com.simibubi.create.foundation.ponder.PonderStoryBoardEntry;
import com.simibubi.create.foundation.ponder.PonderTag;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.element.TextWindowElement;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.FontHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.ponder.DebugScenes;
import com.simibubi.create.infrastructure.ponder.PonderIndex;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.ScreenUtils;
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
	public static final String NEXT_UP = LANG_PREFIX + "next_up";
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

	private ClipboardManager clipboardHelper;
	private BlockPos copiedBlockPos;

	private LerpedFloat finishingFlash;
	private LerpedFloat nextUp;
	private int finishingFlashWarmup = 0;
	private int nextUpWarmup = 0;

	private LerpedFloat lazyIndex;
	private int index = 0;
	private PonderTag referredToByTag;

	private PonderButton left, right, scan, chap, userMode, close, replay, slowMode;
	private int skipCooling = 0;

	private int extendedTickLength = 0;
	private int extendedTickTimer = 0;

	public static PonderUI of(ResourceLocation id) {
		return new PonderUI(PonderRegistry.compile(id));
	}

	public static PonderUI of(ItemStack item) {
		return new PonderUI(PonderRegistry.compile(RegisteredObjects.getKeyOrThrow(item.getItem())));
	}

	public static PonderUI of(ItemStack item, PonderTag tag) {
		PonderUI ponderUI = new PonderUI(PonderRegistry.compile(RegisteredObjects.getKeyOrThrow(item.getItem())));
		ponderUI.referredToByTag = tag;
		return ponderUI;
	}

	public static PonderUI of(PonderChapter chapter) {
		PonderUI ui = new PonderUI(PonderRegistry.compile(chapter));
		ui.chapter = chapter;
		return ui;
	}

	protected PonderUI(List<PonderScene> scenes) {
		ResourceLocation component = scenes.get(0)
			.getComponent();
		if (ForgeRegistries.ITEMS.containsKey(component))
			stack = new ItemStack(ForgeRegistries.ITEMS.getValue(component));
		else
			stack = new ItemStack(ForgeRegistries.BLOCKS.getValue(component));

		tags = new ArrayList<>(PonderRegistry.TAGS.getTags(component));
		this.scenes = scenes;
		if (scenes.isEmpty()) {
			List<PonderStoryBoardEntry> l = Collections.singletonList(new PonderStoryBoardEntry(DebugScenes::empty,
				Create.ID, "debug/scene_1", new ResourceLocation("minecraft", "stick")));
			scenes.addAll(PonderRegistry.compile(l));
		}
		lazyIndex = LerpedFloat.linear()
			.startWithValue(index);
		fadeIn = LerpedFloat.linear()
			.startWithValue(0)
			.chase(1, .1f, Chaser.EXP);
		clipboardHelper = new ClipboardManager();
		finishingFlash = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, .1f, Chaser.EXP);
		nextUp = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, .4f, Chaser.EXP);
	}

	@Override
	protected void init() {
		super.init();

		tagButtons = new ArrayList<>();
		tagFades = new ArrayList<>();

		tags.forEach(t -> {
			int i = tagButtons.size();
			int x = 31;
			int y = 81 + i * 30;

			PonderButton b2 = new PonderButton(x, y).showing(t)
				.withCallback((mX, mY) -> {
					centerScalingOn(mX, mY);
					ScreenOpener.transitionTo(new PonderTagScreen(t));
				});

			addRenderableWidget(b2);
			tagButtons.add(b2);

			LerpedFloat chase = LerpedFloat.linear()
				.startWithValue(0)
				.chase(0, .05f, Chaser.exp(.1));
			tagFades.add(chase);

		});

		/*
		 * if (chapter != null) { widgets.add(chap = new PonderButton(width - 31 - 24,
		 * 31, () -> { }).showing(chapter)); }
		 */

		Options bindings = minecraft.options;
		int spacing = 8;
		int bX = (width - 20) / 2 - (70 + 2 * spacing);
		int bY = height - 20 - 31;

		{
			int pX = (width / 2) - 110;
			int pY = bY + 20 + 4;
			int pW = width - 2 * pX;
			addRenderableWidget(new PonderProgressBar(this, pX, pY, pW, 1));
		}

		addRenderableWidget(scan = new PonderButton(bX, bY).withShortcut(bindings.keyDrop)
			.showing(AllIcons.I_MTD_SCAN)
			.enableFade(0, 5)
			.withCallback(() -> {
				identifyMode = !identifyMode;
				if (!identifyMode)
					scenes.get(index)
						.deselect();
				else
					ponderPartialTicksPaused = minecraft.getFrameTime();
			}));
		scan.atZLevel(600);

		addRenderableWidget(slowMode = new PonderButton(width - 20 - 31, bY).showing(AllIcons.I_MTD_SLOW_MODE)
			.enableFade(0, 5)
			.withCallback(() -> setComfyReadingEnabled(!isComfyReadingEnabled())));

		if (PonderIndex.editingModeActive()) {
			addRenderableWidget(userMode = new PonderButton(width - 50 - 31, bY).showing(AllIcons.I_MTD_USER_MODE)
				.enableFade(0, 5)
				.withCallback(() -> userViewMode = !userViewMode));
		}

		bX += 50 + spacing;
		addRenderableWidget(left = new PonderButton(bX, bY).withShortcut(bindings.keyLeft)
			.showing(AllIcons.I_MTD_LEFT)
			.enableFade(0, 5)
			.withCallback(() -> this.scroll(false)));

		bX += 20 + spacing;
		addRenderableWidget(close = new PonderButton(bX, bY).withShortcut(bindings.keyInventory)
			.showing(AllIcons.I_MTD_CLOSE)
			.enableFade(0, 5)
			.withCallback(this::onClose));

		bX += 20 + spacing;
		addRenderableWidget(right = new PonderButton(bX, bY).withShortcut(bindings.keyRight)
			.showing(AllIcons.I_MTD_RIGHT)
			.enableFade(0, 5)
			.withCallback(() -> this.scroll(true)));

		bX += 50 + spacing;
		addRenderableWidget(replay = new PonderButton(bX, bY).withShortcut(bindings.keyDown)
			.showing(AllIcons.I_MTD_REPLAY)
			.enableFade(0, 5)
			.withCallback(this::replay));
	}

	@Override
	protected void initBackTrackIcon(PonderButton backTrack) {
		backTrack.showing(stack);
	}

	@Override
	public void tick() {
		super.tick();

		if (skipCooling > 0)
			skipCooling--;

		if (referredToByTag != null) {
			for (int i = 0; i < scenes.size(); i++) {
				PonderScene ponderScene = scenes.get(i);
				if (!ponderScene.getTags()
					.contains(referredToByTag))
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
		nextUp.tickChaser();
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

		if (activeScene.getCurrentTime() == activeScene.getTotalTime() - 1) {
			finishingFlashWarmup = 30;
			nextUpWarmup = 50;
		}

		if (finishingFlashWarmup > 0) {
			finishingFlashWarmup--;
			if (finishingFlashWarmup == 0) {
				finishingFlash.setValue(1);
				finishingFlash.setValue(1);
			}
		}

		if (nextUpWarmup > 0) {
			nextUpWarmup--;
			if (nextUpWarmup == 0)
				nextUp.updateChaseTarget(1);
		}

		updateIdentifiedItem(activeScene);
	}

	public PonderScene getActiveScene() {
		return scenes.get(index);
	}

	public void seekToTime(int time) {
		if (getActiveScene().getCurrentTime() > time)
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

		Window w = minecraft.getWindow();
		double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
		double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
		SceneTransform t = activeScene.getTransform();
		Vec3 vec1 = t.screenToScene(mouseX, mouseY, 1000, 0);
		Vec3 vec2 = t.screenToScene(mouseX, mouseY, -100, 0);
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
			List<PonderStoryBoardEntry> list = PonderRegistry.ALL.get(scene.getComponent());
			PonderStoryBoardEntry sb = list.get(index);
			StructureTemplate activeTemplate = PonderRegistry.loadSchematic(sb.getSchematicLocation());
			PonderWorld world = new PonderWorld(BlockPos.ZERO, Minecraft.getInstance().level);
			activeTemplate.placeInWorld(world, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings(), RandomSource.create(),
				Block.UPDATE_CLIENTS);
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
		index = Mth.clamp(index, 0, scenes.size() - 1);
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
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		partialTicks = getPartialTicks();
		RenderSystem.enableBlend();
		renderVisibleScenes(ms, mouseX, mouseY,
			skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		renderWidgets(ms, mouseX, mouseY, identifyMode ? ponderPartialTicksPaused : partialTicks);
	}

	@Override
	public void renderBackground(PoseStack ms) {
		super.renderBackground(ms);
	}

	protected void renderVisibleScenes(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		renderScene(ms, mouseX, mouseY, index, partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			renderScene(ms, mouseX, mouseY, lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
	}

	protected void renderScene(PoseStack ms, int mouseX, int mouseY, int i, float partialTicks) {
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
		PonderScene scene = scenes.get(i);
		double value = lazyIndex.getValue(minecraft.getFrameTime());
		double diff = i - value;
		double slide = Mth.lerp(diff * diff, 200, 600) * diff;

		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		RenderSystem.backupProjectionMatrix();

		// has to be outside of MS transforms, important for vertex sorting
		Matrix4f matrix4f = new Matrix4f(RenderSystem.getProjectionMatrix());
		matrix4f.translate(0, 0, 800);
		RenderSystem.setProjectionMatrix(matrix4f);

		ms.pushPose();
		ms.translate(0, 0, -800);
		
		scene.getTransform()
			.updateScreenParams(width, height, slide);
		scene.getTransform()
			.apply(ms, partialTicks);

//		ms.translate(-story.getBasePlateOffsetX() * .5, 0, -story.getBasePlateOffsetZ() * .5);

		scene.getTransform()
			.updateSceneRVE(partialTicks);
		
		scene.renderScene(buffer, ms, partialTicks);
		buffer.draw();

		BoundingBox bounds = scene.getBounds();
		ms.pushPose();

		// kool shadow fx
		if (!scene.shouldHidePlatformShadow()) {
			RenderSystem.enableCull();
			RenderSystem.enableDepthTest();
			ms.pushPose();
			ms.translate(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ());
			UIRenderHelper.flipForGuiRender(ms);

			float flash = finishingFlash.getValue(partialTicks) * .9f;
			float alpha = flash;
			flash *= flash;
			flash = ((flash * 2) - 1);
			flash *= flash;
			flash = 1 - flash;

			for (int f = 0; f < 4; f++) {
				ms.translate(scene.getBasePlateSize(), 0, 0);
				ms.pushPose();
				ms.translate(0, 0, -1 / 1024f);
				if (flash > 0) {
					ms.pushPose();
					ms.scale(1, .5f + flash * .75f, 1);
					ScreenUtils.drawGradientRect(ms.last()
						.pose(), 0, 0, -1, -scene.getBasePlateSize(), 0, 0x00_c6ffc9,
						new Color(0xaa_c6ffc9).scaleAlpha(alpha)
							.getRGB());
					ms.popPose();
				}
				ms.translate(0, 0, 2 / 1024f);
				ScreenUtils.drawGradientRect(ms.last()
					.pose(), 0, 0, 0, -scene.getBasePlateSize(), 4, 0x66_000000, 0x00_000000);
				ms.popPose();
				ms.mulPose(Axis.YP.rotationDegrees(-90));
			}
			ms.popPose();
			RenderSystem.disableCull();
			RenderSystem.disableDepthTest();
		}

		// coords for debug
		if (PonderIndex.editingModeActive() && !userViewMode) {

			ms.scale(-1, -1, 1);
			ms.scale(1 / 16f, 1 / 16f, 1 / 16f);
			ms.translate(1, -8, -1 / 64f);

			// X AXIS
			ms.pushPose();
			ms.translate(4, -3, 0);
			ms.translate(0, 0, -2 / 1024f);
			for (int x = 0; x <= bounds.getXSpan(); x++) {
				ms.translate(-16, 0, 0);
				font.draw(ms, x == bounds.getXSpan() ? "x" : "" + x, 0, 0, 0xFFFFFFFF);
			}
			ms.popPose();

			// Z AXIS
			ms.pushPose();
			ms.scale(-1, 1, 1);
			ms.translate(0, -3, -4);
			ms.mulPose(Axis.YP.rotationDegrees(-90));
			ms.translate(-8, -2, 2 / 64f);
			for (int z = 0; z <= bounds.getZSpan(); z++) {
				ms.translate(16, 0, 0);
				font.draw(ms, z == bounds.getZSpan() ? "z" : "" + z, 0, 0, 0xFFFFFFFF);
			}
			ms.popPose();

			// DIRECTIONS
			ms.pushPose();
			ms.translate(bounds.getXSpan() * -8, 0, bounds.getZSpan() * 8);
			ms.mulPose(Axis.YP.rotationDegrees(-90));
			for (Direction d : Iterate.horizontalDirections) {
				ms.mulPose(Axis.YP.rotationDegrees(90));
				ms.pushPose();
				ms.translate(0, 0, bounds.getZSpan() * 16);
				ms.mulPose(Axis.XP.rotationDegrees(-90));
				font.draw(ms, d.name()
					.substring(0, 1), 0, 0, 0x66FFFFFF);
				font.draw(ms, "|", 2, 10, 0x44FFFFFF);
				font.draw(ms, ".", 2, 14, 0x22FFFFFF);
				ms.popPose();
			}
			ms.popPose();
			buffer.draw();
		}

		ms.popPose();
		ms.popPose();
		RenderSystem.restoreProjectionMatrix();
	}

	protected void renderWidgets(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.disableDepthTest();

		float fade = fadeIn.getValue(partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		float indexDiff = Math.abs(lazyIndexValue - index);
		PonderScene activeScene = scenes.get(index);
		PonderScene nextScene = scenes.size() > index + 1 ? scenes.get(index + 1) : null;

		boolean noWidgetsHovered = true;
		for (GuiEventListener child : children())
			noWidgetsHovered &= !child.isMouseOver(mouseX, mouseY);

		int tooltipColor = Theme.i(Theme.Key.TEXT_DARKER);
		renderChapterTitle(ms, fade, indexDiff, activeScene, tooltipColor);
		renderNavigationMenu(ms);

		if (identifyMode) {
			if (noWidgetsHovered && mouseY < height - 80) {
				ms.pushPose();
				ms.translate(mouseX, mouseY, 100);
				if (hoveredTooltipItem.isEmpty()) {
					MutableComponent text = Lang
						.translateDirect(IDENTIFY_MODE,
							((MutableComponent) minecraft.options.keyDrop.getTranslatedKeyMessage())
								.withStyle(ChatFormatting.WHITE))
						.withStyle(ChatFormatting.GRAY);
					renderComponentTooltip(ms, font.getSplitter()
						.splitLines(text, width / 3, Style.EMPTY), 0, 0, font);
				} else
					renderTooltip(ms, hoveredTooltipItem, 0, 0);
				if (hoveredBlockPos != null && PonderIndex.editingModeActive() && !userViewMode) {
					ms.translate(0, -15, 0);
					boolean copied = copiedBlockPos != null && hoveredBlockPos.equals(copiedBlockPos);
					MutableComponent coords = Components.literal(
						hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ())
							.withStyle(copied ? ChatFormatting.GREEN : ChatFormatting.GOLD);
					renderTooltip(ms, coords, 0, 0);
				}
				ms.popPose();
			}
			scan.flash();
		} else {
			scan.dim();
		}

		if (PonderIndex.editingModeActive()) {
			if (userViewMode)
				userMode.flash();
			else
				userMode.dim();
		}

		if (isComfyReadingEnabled())
			slowMode.flash();
		else
			slowMode.dim();

		renderSceneOverlay(ms, partialTicks, lazyIndexValue, indexDiff);

		boolean finished = activeScene.isFinished();

		if (finished) {
			jumpToNextScene(ms, partialTicks, nextScene);
		}

		// Widgets
		renderables.forEach(w -> {
			if (w instanceof PonderButton button) {
				button.fade()
					.startWithValue(fade);
			}
		});

		if (index == 0 || index == 1 && lazyIndexValue < index)
			left.fade()
				.startWithValue(lazyIndexValue);
		if (index == scenes.size() - 1 || index == scenes.size() - 2 && lazyIndexValue > index)
			right.fade()
				.startWithValue(scenes.size() - lazyIndexValue - 1);

		if (finished)
			right.flash();
		else {
			right.dim();
			nextUp.updateChaseTarget(0);
		}

		renderPonderTags(ms, mouseX, mouseY, partialTicks, fade, activeScene);

		renderHoverTooltips(ms, tooltipColor);
		RenderSystem.enableDepthTest();
	}

	protected void renderPonderTags(PoseStack ms, int mouseX, int mouseY, float partialTicks, float fade, PonderScene activeScene) {
		// Tags
		List<PonderTag> sceneTags = activeScene.getTags();
		boolean highlightAll = sceneTags.contains(PonderTag.HIGHLIGHT_ALL);
		double s = Minecraft.getInstance()
			.getWindow()
			.getGuiScale();
		IntStream.range(0, tagButtons.size())
			.forEach(i -> {
				ms.pushPose();
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

				int x = button.getX() + button.getWidth() + 4;
				int y = button.getY() - 2;
				ms.translate(x, y + 5 * (1 - fade), 800);

				float fadedWidth = 200 * chase.getValue(partialTicks);
				UIRenderHelper.streak(ms, 0, 0, 12, 26, (int) fadedWidth);

				RenderSystem.enableScissor((int) (x * s), 0, (int) (fadedWidth * s), (int) (height * s));

				String tagName = this.tags.get(i)
					.getTitle();
				font.draw(ms, tagName, 3, 8, Theme.i(Theme.Key.TEXT_ACCENT_SLIGHT));

				RenderSystem.disableScissor();

				ms.popPose();
			});
	}

	protected void renderSceneOverlay(PoseStack ms, float partialTicks, float lazyIndexValue, float indexDiff) {
		// Scene overlay
		float scenePT = skipCooling > 0 ? 0 : partialTicks;
		ms.pushPose();
		ms.translate(0, 0, 100);
		renderOverlay(ms, index, scenePT);
		if (indexDiff > 1 / 512f)
			renderOverlay(ms, lazyIndexValue < index ? index - 1 : index + 1, scenePT);
		ms.popPose();
	}

	protected void jumpToNextScene(PoseStack ms, float partialTicks, PonderScene nextScene) {
		if (nextScene != null && nextUp.getValue() > 1 / 16f && !nextScene.getId()
				.equals(Create.asResource("creative_motor_mojang"))) {
			ms.pushPose();
			ms.translate(right.getX() + 10, right.getY() - 6 + nextUp.getValue(partialTicks) * 5, 400);
			int boxWidth = (Math.max(font.width(nextScene.getTitle()), font.width(Lang.translateDirect(NEXT_UP))) + 5);
			renderSpeechBox(ms, 0, 0, boxWidth, 20, right.isHoveredOrFocused(), Pointing.DOWN, false);
			ms.translate(0, -29, 100);
			drawCenteredString(ms, font, Lang.translateDirect(NEXT_UP), 0, 0, Theme.i(Theme.Key.TEXT_DARKER));
			drawCenteredString(ms, font, nextScene.getTitle(), 0, 10, Theme.i(Theme.Key.TEXT));
			ms.popPose();
		}
	}

	protected void renderHoverTooltips(PoseStack ms, int tooltipColor) {
		ms.pushPose();
		ms.translate(0, 0, 500);
		int tooltipY = height - 16;
		if (scan.isHoveredOrFocused())
			drawCenteredString(ms, font, Lang.translateDirect(IDENTIFY), scan.getX() + 10, tooltipY, tooltipColor);
		if (index != 0 && left.isHoveredOrFocused())
			drawCenteredString(ms, font, Lang.translateDirect(PREVIOUS), left.getX() + 10, tooltipY, tooltipColor);
		if (close.isHoveredOrFocused())
			drawCenteredString(ms, font, Lang.translateDirect(CLOSE), close.getX() + 10, tooltipY, tooltipColor);
		if (index != scenes.size() - 1 && right.isHoveredOrFocused())
			drawCenteredString(ms, font, Lang.translateDirect(NEXT), right.getX() + 10, tooltipY, tooltipColor);
		if (replay.isHoveredOrFocused())
			drawCenteredString(ms, font, Lang.translateDirect(REPLAY), replay.getX() + 10, tooltipY, tooltipColor);
		if (slowMode.isHoveredOrFocused())
			drawCenteredString(ms, font, Lang.translateDirect(SLOW_TEXT), slowMode.getX() + 5, tooltipY, tooltipColor);
		if (PonderIndex.editingModeActive() && userMode.isHoveredOrFocused())
			drawCenteredString(ms, font, "Editor View", userMode.getX() + 10, tooltipY, tooltipColor);
		ms.popPose();
	}

	protected void renderChapterTitle(PoseStack ms, float fade, float indexDiff, PonderScene activeScene, int tooltipColor) {
		// Chapter title
		ms.pushPose();
		ms.translate(0, 0, 400);
		int x = 31 + 20 + 8;
		int y = 31;

		String title = activeScene.getTitle();
		int wordWrappedHeight = font.wordWrapHeight(title, left.getX() - 51);

		int streakHeight = 35 - 9 + wordWrappedHeight;
		UIRenderHelper.streak(ms, 0, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (150 * fade));
		UIRenderHelper.streak(ms, 180, x - 4, y - 12 + streakHeight / 2, streakHeight, (int) (30 * fade));
		new BoxElement().withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
				.gradientBorder(Theme.p(Theme.Key.PONDER_IDLE))
				.at(21, 21, 100)
				.withBounds(30, 30)
				.render(ms);

		GuiGameElement.of(stack)
				.scale(2)
				.at(x - 39f, y - 11f)
				.render(ms);

		font.draw(ms, Lang.translateDirect(PONDERING), x, y - 6, tooltipColor);
		y += 8;
		x += 0;
		ms.translate(x, y, 0);
		ms.mulPose(Axis.XN.rotationDegrees(indexDiff * -75));
		ms.translate(0, 0, 5);
		FontHelper.drawSplitString(ms, font, title, 0, 0, left.getX() - 51, Theme.c(Theme.Key.TEXT)
				.scaleAlpha(1 - indexDiff)
				.getRGB());
		ms.popPose();
		if (chapter != null) {
			ms.pushPose();

			ms.translate(chap.getX() - 8, chap.getY(), 0);
			UIRenderHelper.streak(ms, 180, 4, 10, 26, (int) (150 * fade));

			drawRightAlignedString(font, ms, Lang.translateDirect(IN_CHAPTER)
					.getString(), 0, 0, tooltipColor);
			drawRightAlignedString(font, ms, chapter.getTitle(), 0, 12, Theme.i(Theme.Key.TEXT));

			ms.popPose();
		}
	}

	protected void renderNavigationMenu(PoseStack ms) {
		Color c1 = Theme.c(Theme.Key.PONDER_BACK_ARROW)
			.setAlpha(0x40);
		Color c2 = Theme.c(Theme.Key.PONDER_BACK_ARROW)
			.setAlpha(0x20);
		Color c3 = Theme.c(Theme.Key.PONDER_BACK_ARROW)
			.setAlpha(0x10);
		UIRenderHelper.breadcrumbArrow(ms, width / 2 - 20, height - 51, 0, 20, 20, 5, c1, c2);
		UIRenderHelper.breadcrumbArrow(ms, width / 2 + 20, height - 51, 0, -20, 20, -5, c1, c2);
		UIRenderHelper.breadcrumbArrow(ms, width / 2 - 90, height - 51, 0, 70, 20, 5, c1, c3);
		UIRenderHelper.breadcrumbArrow(ms, width / 2 + 90, height - 51, 0, -70, 20, -5, c1, c3);
	}

	private void renderOverlay(PoseStack ms, int i, float partialTicks) {
		if (identifyMode)
			return;
		ms.pushPose();
		PonderScene story = scenes.get(i);
		story.renderOverlay(this, ms, skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks);
		ms.popPose();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (identifyMode && hoveredBlockPos != null && PonderIndex.editingModeActive()) {
			long handle = minecraft.getWindow()
				.getWindow();
			if (copiedBlockPos != null && button == 1) {
				clipboardHelper.setClipboard(handle,
					"util.select.fromTo(" + copiedBlockPos.getX() + ", " + copiedBlockPos.getY() + ", "
						+ copiedBlockPos.getZ() + ", " + hoveredBlockPos.getX() + ", " + hoveredBlockPos.getY() + ", "
						+ hoveredBlockPos.getZ() + ")");
				copiedBlockPos = hoveredBlockPos;
				return true;
			}

			if (hasShiftDown())
				clipboardHelper.setClipboard(handle, "util.select.position(" + hoveredBlockPos.getX() + ", "
					+ hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")");
			else
				clipboardHelper.setClipboard(handle, "util.grid.at(" + hoveredBlockPos.getX() + ", "
					+ hoveredBlockPos.getY() + ", " + hoveredBlockPos.getZ() + ")");
			copiedBlockPos = hoveredBlockPos;
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		Options settings = Minecraft.getInstance().options;
		int sCode = settings.keyDown.getKey()
			.getValue();
		int aCode = settings.keyLeft.getKey()
			.getValue();
		int dCode = settings.keyRight.getKey()
			.getValue();
		int qCode = settings.keyDrop.getKey()
			.getValue();

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
			return chapter.getTitle();

		return stack.getItem()
			.getDescription()
			.getString();
	}

	public Font getFontRenderer() {
		return font;
	}

	protected boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
		boolean hovered = !(mouseX < x || mouseX > x + w);
		hovered &= !(mouseY < y || mouseY > y + h);
		return hovered;
	}

	public static void renderSpeechBox(PoseStack ms, int x, int y, int w, int h, boolean highlighted, Pointing pointing,
		boolean returnWithLocalTransform) {
		if (!returnWithLocalTransform)
			ms.pushPose();

		int boxX = x;
		int boxY = y;
		int divotX = x;
		int divotY = y;
		int divotRotation = 0;
		int divotSize = 8;
		int distance = 1;
		int divotRadius = divotSize / 2;
		Couple<Color> borderColors = Theme.p(highlighted ? Theme.Key.PONDER_BUTTON_HOVER : Theme.Key.PONDER_IDLE);
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
			c = Color.mixColors(borderColors, 0.5f);
			break;
		case RIGHT:
			divotRotation = 270;
			boxX -= w + divotSize + 1 + distance;
			boxY -= h / 2;
			divotX -= divotSize + distance;
			divotY -= divotRadius;
			c = Color.mixColors(borderColors, 0.5f);
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

		new BoxElement().withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
			.gradientBorder(borderColors)
			.at(boxX, boxY, 100)
			.withBounds(w, h)
			.render(ms);

		ms.pushPose();
		ms.translate(divotX + divotRadius, divotY + divotRadius, 10);
		ms.mulPose(Axis.ZP.rotationDegrees(divotRotation));
		ms.translate(-divotRadius, -divotRadius, 0);
		AllGuiTextures.SPEECH_TOOLTIP_BACKGROUND.render(ms, 0, 0);
		AllGuiTextures.SPEECH_TOOLTIP_COLOR.render(ms, 0, 0, c);
		ms.popPose();

		if (returnWithLocalTransform) {
			ms.translate(boxX, boxY, 0);
			return;
		}

		ms.popPose();

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
			return stack.sameItem(((PonderUI) other).stack);
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
			.getFrameTime();

		if (Minecraft.getInstance().screen instanceof PonderUI) {
			PonderUI ui = (PonderUI) Minecraft.getInstance().screen;
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

	public void drawRightAlignedString(Font fontRenderer, PoseStack ms, String string, int x, int y, int color) {
		fontRenderer.draw(ms, string, (float) (x - fontRenderer.width(string)), (float) y, color);
	}

	public boolean isComfyReadingEnabled() {
		return AllConfigs.client().comfyReading.get();
	}

	public void setComfyReadingEnabled(boolean slowTextMode) {
		AllConfigs.client().comfyReading.set(slowTextMode);
	}

}
