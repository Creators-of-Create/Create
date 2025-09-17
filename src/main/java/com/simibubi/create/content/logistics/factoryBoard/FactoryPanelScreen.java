package com.simibubi.create.content.logistics.factoryBoard;

import static com.simibubi.create.foundation.gui.AllGuiTextures.FACTORY_GAUGE_BOTTOM;
import static com.simibubi.create.foundation.gui.AllGuiTextures.FACTORY_GAUGE_RECIPE;
import static com.simibubi.create.foundation.gui.AllGuiTextures.FACTORY_GAUGE_RESTOCK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.world.item.crafting.ShapedRecipe;

public class FactoryPanelScreen extends AbstractSimiScreen {

	private AddressEditBox addressBox;
	private IconButton confirmButton;
	private IconButton deleteButton;
	private IconButton newInputButton;
	private IconButton relocateButton;
	private IconButton activateCraftingButton;
	private ScrollInput promiseExpiration;
	private FactoryPanelBehaviour behaviour;
	private boolean restocker;
	private boolean sendReset;
	private boolean sendRedstoneReset;

	private BigItemStack outputConfig;
	private List<BigItemStack> inputConfig;
	private List<FactoryPanelConnection> connections;

	private CraftingRecipe availableCraftingRecipe;
	private boolean craftingActive;
	private List<BigItemStack> craftingIngredients;

	public FactoryPanelScreen(FactoryPanelBehaviour behaviour) {
		this.behaviour = behaviour;
		minecraft = Minecraft.getInstance();
		restocker = behaviour.panelBE().restocker;
		availableCraftingRecipe = null;
		craftingActive = !behaviour.activeCraftingArrangement.isEmpty();
		updateConfigs();
	}

	private void updateConfigs() {
		connections = new ArrayList<>(behaviour.targetedBy.values());
		outputConfig = new BigItemStack(behaviour.getFilter(), behaviour.recipeOutput);
		inputConfig = connections.stream()
			.map(c -> {
				FactoryPanelBehaviour b = FactoryPanelBehaviour.at(minecraft.level, c.from);
				return b == null ? new BigItemStack(ItemStack.EMPTY, 0) : new BigItemStack(b.getFilter(), c.amount);
			})
			.toList();

		searchForCraftingRecipe();

		if (availableCraftingRecipe == null) {
			craftingActive = false;
			return;
		}

		craftingIngredients = convertRecipeToPackageOrderContext(availableCraftingRecipe, inputConfig, false);
	}

	public static List<BigItemStack> convertRecipeToPackageOrderContext(CraftingRecipe availableCraftingRecipe, List<BigItemStack> inputs, boolean respectAmounts) {
		List<BigItemStack> craftingIngredients = new ArrayList<>();
		BigItemStack emptyIngredient = new BigItemStack(ItemStack.EMPTY, 1);
		NonNullList<Ingredient> ingredients = availableCraftingRecipe.getIngredients();
		List<BigItemStack> mutableInputs = BigItemStack.duplicateWrappers(inputs);

		int width = Math.min(3, ingredients.size());
		int height = Math.min(3, ingredients.size() / 3 + 1);

		if (availableCraftingRecipe instanceof ShapedRecipe shaped) {
			width = shaped.getWidth();
			height = shaped.getHeight();
		}

		if (height == 1)
			for (int i = 0; i < 3; i++)
				craftingIngredients.add(emptyIngredient);
		if (width == 1)
			craftingIngredients.add(emptyIngredient);

		for (int i = 0; i < ingredients.size(); i++) {
			Ingredient ingredient = ingredients.get(i);
			BigItemStack craftingIngredient = emptyIngredient;

			if (!ingredient.isEmpty())
				for (BigItemStack bigItemStack : mutableInputs)
					if (bigItemStack.count > 0 && ingredient.test(bigItemStack.stack)) {
						craftingIngredient = new BigItemStack(bigItemStack.stack, 1);
						if (respectAmounts)
							bigItemStack.count -= 1;
						break;
					}

			craftingIngredients.add(craftingIngredient);

			if (width < 3 && (i + 1) % width == 0)
				for (int j = 0; j < 3 - width; j++)
					if (craftingIngredients.size() < 9)
						craftingIngredients.add(emptyIngredient);
		}

		while (craftingIngredients.size() < 9)
			craftingIngredients.add(emptyIngredient);

		return craftingIngredients;
	}

	@Override
	protected void init() {
		int sizeX = FACTORY_GAUGE_BOTTOM.getWidth();
		int sizeY =
			(restocker ? FACTORY_GAUGE_RESTOCK : FACTORY_GAUGE_RECIPE).getHeight() + FACTORY_GAUGE_BOTTOM.getHeight();

		setWindowSize(sizeX, sizeY);
		super.init();
		clearWidgets();

		int x = guiLeft;
		int y = guiTop;

		if (addressBox == null) {
			String frogAddress = behaviour.getFrogAddress();
			addressBox = new AddressEditBox(this, new NoShadowFontWrapper(font), x + 36, y + windowHeight - 51, 108, 10, false, frogAddress);
			addressBox.setValue(behaviour.recipeAddress);
			addressBox.setTextColor(0x555555);
		}
		addressBox.setX(x + 36);
		addressBox.setY(y + windowHeight - 51);
		addRenderableWidget(addressBox);

		confirmButton = new IconButton(x + sizeX - 33, y + sizeY - 25, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.setScreen(null));
		confirmButton.setToolTip(CreateLang.translate("gui.factory_panel.save_and_close")
			.component());
		addRenderableWidget(confirmButton);

		deleteButton = new IconButton(x + sizeX - 55, y + sizeY - 25, AllIcons.I_TRASH);
		deleteButton.withCallback(() -> {
			sendReset = true;
			minecraft.setScreen(null);
		});
		deleteButton.setToolTip(CreateLang.translate("gui.factory_panel.reset")
			.component());
		addRenderableWidget(deleteButton);

		promiseExpiration = new ScrollInput(x + 97, y + windowHeight - 24, 28, 16).withRange(-1, 31)
			.titled(CreateLang.translate("gui.factory_panel.promises_expire_title")
				.component());
		promiseExpiration.setState(behaviour.promiseClearingInterval);
		addRenderableWidget(promiseExpiration);

		newInputButton = new IconButton(x + 31, y + 47, AllIcons.I_ADD);
		newInputButton.withCallback(() -> {
			FactoryPanelConnectionHandler.startConnection(behaviour);
			minecraft.setScreen(null);
		});
		newInputButton.setToolTip(CreateLang.translate("gui.factory_panel.connect_input")
			.component());

		relocateButton = new IconButton(x + 31, y + 67, AllIcons.I_MOVE_GAUGE);
		relocateButton.withCallback(() -> {
			FactoryPanelConnectionHandler.startRelocating(behaviour);
			minecraft.setScreen(null);
		});
		relocateButton.setToolTip(CreateLang.translate("gui.factory_panel.relocate")
			.component());

		if (!restocker) {
			addRenderableWidget(newInputButton);
			addRenderableWidget(relocateButton);
		}

		activateCraftingButton = null;
		if (availableCraftingRecipe != null) {
			activateCraftingButton = new IconButton(x + 31, y + 27, AllIcons.I_3x3);
			activateCraftingButton.withCallback(() -> {
				craftingActive = !craftingActive;
				init();
				if (craftingActive) {
					outputConfig.count = availableCraftingRecipe.getResultItem(minecraft.level.registryAccess())
						.getCount();
				}
			});
			activateCraftingButton.setToolTip(CreateLang.translate("gui.factory_panel.activate_crafting")
				.component());
			addRenderableWidget(activateCraftingButton);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (inputConfig.size() != behaviour.targetedBy.size()) {
			updateConfigs();
			init();
		}
		if (activateCraftingButton != null)
			activateCraftingButton.green = craftingActive;
		addressBox.tick();
		promiseExpiration.titled(CreateLang
			.translate(promiseExpiration.getState() == -1 ? "gui.factory_panel.promises_do_not_expire"
				: "gui.factory_panel.promises_expire_title")
			.component());
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		// BG
		AllGuiTextures bg = restocker ? FACTORY_GAUGE_RESTOCK : FACTORY_GAUGE_RECIPE;
		if (restocker)
			FACTORY_GAUGE_RECIPE.render(graphics, x, y - 16);
		bg.render(graphics, x, y);
		FACTORY_GAUGE_BOTTOM.render(graphics, x, y + bg.getHeight());
		y = guiTop;

		// RECIPE
		int slot = 0;
		if (craftingActive) {
			for (BigItemStack itemStack : craftingIngredients)
				renderInputItem(graphics, slot++, itemStack, mouseX, mouseY);
		} else {
			for (BigItemStack itemStack : inputConfig)
				renderInputItem(graphics, slot++, itemStack, mouseX, mouseY);
			if (inputConfig.isEmpty()) {
				int inputX = guiLeft + (restocker ? 88 : 68 + (slot % 3 * 20));
				int inputY = guiTop + (restocker ? 12 : 28) + (slot / 3 * 20);
				if (!restocker && mouseY > inputY && mouseY < inputY + 60 && mouseX > inputX && mouseX < inputX + 60)
					graphics.renderComponentTooltip(font,
						List.of(CreateLang.translate("gui.factory_panel.unconfigured_input")
							.color(ScrollInput.HEADER_RGB)
							.component(),
							CreateLang.translate("gui.factory_panel.unconfigured_input_tip")
								.style(ChatFormatting.GRAY)
								.component(),
							CreateLang.translate("gui.factory_panel.unconfigured_input_tip_1")
								.style(ChatFormatting.GRAY)
								.component()),
						mouseX, mouseY);
			}
		}

		if (restocker)
			renderInputItem(graphics, slot, new BigItemStack(behaviour.getFilter(), 1), mouseX, mouseY);

		if (!restocker) {
			int outputX = x + 160;
			int outputY = y + 48;
			graphics.renderItem(outputConfig.stack, outputX, outputY);
			graphics.renderItemDecorations(font, behaviour.getFilter(), outputX, outputY, outputConfig.count + "");

			if (mouseX >= outputX - 1 && mouseX < outputX - 1 + 18 && mouseY >= outputY - 1
				&& mouseY < outputY - 1 + 18) {
				MutableComponent c1 = CreateLang
					.translate("gui.factory_panel.expected_output", CreateLang.itemName(outputConfig.stack)
						.add(CreateLang.text(" x" + outputConfig.count))
						.string())
					.color(ScrollInput.HEADER_RGB)
					.component();
				MutableComponent c2 = CreateLang.translate("gui.factory_panel.expected_output_tip")
					.style(ChatFormatting.GRAY)
					.component();
				MutableComponent c3 = CreateLang.translate("gui.factory_panel.expected_output_tip_1")
					.style(ChatFormatting.GRAY)
					.component();
				MutableComponent c4 = CreateLang.translate("gui.factory_panel.expected_output_tip_2")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component();
				graphics.renderComponentTooltip(font, craftingActive ? List.of(c1, c2, c3) : List.of(c1, c2, c3, c4),
					mouseX, mouseY);
			}
		}

		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(0, 0, 10);

		// ADDRESS
		if (addressBox.isHovered() && !addressBox.isFocused())
			showAddressBoxTooltip(graphics, mouseX, mouseY);

		// TITLE
		Component title = CreateLang
			.translate(restocker ? "gui.factory_panel.title_as_restocker" : "gui.factory_panel.title_as_recipe")
			.component();
		graphics.drawString(font, title, x + 97 - font.width(title) / 2, y + (restocker ? -12 : 4), 0x3D3C48, false);

		// ITEM PREVIEW
		int previewY = restocker ? 0 : 60;

		ms.pushPose();
		ms.translate(0, previewY, 0);
		GuiGameElement.of(AllBlocks.FACTORY_GAUGE.asStack())
			.scale(4)
			.at(0, 0, -200)
			.render(graphics, x + 195, y + 55);
		if (!behaviour.getFilter()
			.isEmpty()) {
			GuiGameElement.of(behaviour.getFilter())
				.scale(1.625)
				.at(0, 0, 100)
				.render(graphics, x + 214, y + 68);
		}

		ms.popPose();

		// REDSTONE LINKS
		if (!behaviour.targetedByLinks.isEmpty()) {
			ItemStack asStack = AllBlocks.REDSTONE_LINK.asStack();
			int itemX = x + 9;
			int itemY = y + windowHeight - 24;
			AllGuiTextures.FROGPORT_SLOT.render(graphics, itemX - 1, itemY - 1);
			graphics.renderItem(asStack, itemX, itemY);

			if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
				List<Component> linkTip = List.of(CreateLang.translate("gui.factory_panel.has_link_connections")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.translate("gui.factory_panel.left_click_disconnect")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component());
				graphics.renderComponentTooltip(font, linkTip, mouseX, mouseY);
			}
		}

		// PROMISES
		int state = promiseExpiration.getState();
		graphics.drawString(font, CreateLang.text(state == -1 ? " /" : state == 0 ? "30s" : state + "m")
			.component(), promiseExpiration.getX() + 3, promiseExpiration.getY() + 4, 0xffeeeeee, true);

		ItemStack asStack = PackageStyles.getDefaultBox();
		int itemX = x + 68;
		int itemY = y + windowHeight - 24;
		graphics.renderItem(asStack, itemX, itemY);
		int promised = behaviour.getPromised();
		graphics.renderItemDecorations(font, asStack, itemX, itemY, promised + "");

		if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
			List<Component> promiseTip = List.of();

			if (promised == 0) {
				promiseTip = List.of(CreateLang.translate("gui.factory_panel.no_open_promises")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang
						.translate(restocker ? "gui.factory_panel.restocker_promises_tip"
							: "gui.factory_panel.recipe_promises_tip")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang
						.translate(restocker ? "gui.factory_panel.restocker_promises_tip_1"
							: "gui.factory_panel.recipe_promises_tip_1")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.factory_panel.promise_prevents_oversending")
						.style(ChatFormatting.GRAY)
						.component());
			} else {
				promiseTip = List.of(CreateLang.translate("gui.factory_panel.promised_items")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.text(behaviour.getFilter()
						.getHoverName()
						.getString() + " x" + promised)
						.component(),
					CreateLang.translate("gui.factory_panel.left_click_reset")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component());
			}

			graphics.renderComponentTooltip(font, promiseTip, mouseX, mouseY);
		}

		ms.popPose();
	}

	//

	private void renderInputItem(GuiGraphics graphics, int slot, BigItemStack itemStack, int mouseX, int mouseY) {
		int inputX = guiLeft + (restocker ? 88 : 68 + (slot % 3 * 20));
		int inputY = guiTop + (restocker ? 12 : 28) + (slot / 3 * 20);

		graphics.renderItem(itemStack.stack, inputX, inputY);
		if (!craftingActive && !restocker && !itemStack.stack.isEmpty())
			graphics.renderItemDecorations(font, itemStack.stack, inputX, inputY, itemStack.count + "");

		if (mouseX < inputX - 2 || mouseX >= inputX - 2 + 20 || mouseY < inputY - 2 || mouseY >= inputY - 2 + 20)
			return;

		if (craftingActive) {
			graphics.renderComponentTooltip(font, List.of(CreateLang.translate("gui.factory_panel.crafting_input")
				.color(ScrollInput.HEADER_RGB)
				.component(),
				CreateLang.translate("gui.factory_panel.crafting_input_tip")
					.style(ChatFormatting.GRAY)
					.component(),
				CreateLang.translate("gui.factory_panel.crafting_input_tip_1")
					.style(ChatFormatting.GRAY)
					.component()),
				mouseX, mouseY);
			return;
		}

		if (itemStack.stack.isEmpty()) {
			graphics.renderComponentTooltip(font, List.of(CreateLang.translate("gui.factory_panel.empty_panel")
				.color(ScrollInput.HEADER_RGB)
				.component(),
				CreateLang.translate("gui.factory_panel.left_click_disconnect")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component()),
				mouseX, mouseY);
			return;
		}

		if (restocker) {
			graphics.renderComponentTooltip(font,
				List.of(CreateLang.translate("gui.factory_panel.sending_item", CreateLang.itemName(itemStack.stack)
					.string())
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.translate("gui.factory_panel.sending_item_tip")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.factory_panel.sending_item_tip_1")
						.style(ChatFormatting.GRAY)
						.component()),
				mouseX, mouseY);
			return;
		}

		graphics.renderComponentTooltip(font,
			List.of(CreateLang.translate("gui.factory_panel.sending_item", CreateLang.itemName(itemStack.stack)
				.add(CreateLang.text(" x" + itemStack.count))
				.string())
				.color(ScrollInput.HEADER_RGB)
				.component(),
				CreateLang.translate("gui.factory_panel.scroll_to_change_amount")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component(),
				CreateLang.translate("gui.factory_panel.left_click_disconnect")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component()),
			mouseX, mouseY);
	}

	private void showAddressBoxTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		if (addressBox.getValue()
			.isBlank()) {
			if (restocker) {
				graphics.renderComponentTooltip(font,
					List.of(CreateLang.translate("gui.factory_panel.restocker_address")
						.color(ScrollInput.HEADER_RGB)
						.component(),
						CreateLang.translate("gui.factory_panel.restocker_address_tip")
							.style(ChatFormatting.GRAY)
							.component(),
						CreateLang.translate("gui.factory_panel.restocker_address_tip_1")
							.style(ChatFormatting.GRAY)
							.component(),
						CreateLang.translate("gui.schedule.lmb_edit")
							.style(ChatFormatting.DARK_GRAY)
							.style(ChatFormatting.ITALIC)
							.component()),
					mouseX, mouseY);

			} else {
				graphics.renderComponentTooltip(font, List.of(CreateLang.translate("gui.factory_panel.recipe_address")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.translate("gui.factory_panel.recipe_address_tip")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.factory_panel.recipe_address_tip_1")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.schedule.lmb_edit")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component()),
					mouseX, mouseY);
			}
		} else
			graphics.renderComponentTooltip(font,
				List.of(
					CreateLang
						.translate(restocker ? "gui.factory_panel.restocker_address_given"
							: "gui.factory_panel.recipe_address_given")
						.color(ScrollInput.HEADER_RGB)
						.component(),
					CreateLang.text("'" + addressBox.getValue() + "'")
						.style(ChatFormatting.GRAY)
						.component()),
				mouseX, mouseY);
	}

	//

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
		if (getFocused() != null && !getFocused().isMouseOver(mouseX, mouseY))
			setFocused(null);

		int x = guiLeft;
		int y = guiTop;

		// Remove connections
		if (!craftingActive)
			for (int i = 0; i < connections.size(); i++) {
				int inputX = x + 68 + (i % 3 * 20);
				int inputY = y + 28 + (i / 3 * 20);
				if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
					sendIt(connections.get(i).from, false);
					playButtonSound();
					return true;
				}
			}

		// Clear promises
		int itemX = x + 68;
		int itemY = y + windowHeight - 24;
		if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
			sendIt(null, true);
			playButtonSound();
			return true;
		}

		// remove redstone connections
		itemX = x + 9;
		itemY = y + windowHeight - 24;
		if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
			sendRedstoneReset = true;
			sendIt(null, false);
			playButtonSound();
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, pButton);
	}

	public void playButtonSound() {
		Minecraft.getInstance()
			.getSoundManager()
			.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.25f));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		int x = guiLeft;
		int y = guiTop;

		if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
			return true;

		if (craftingActive)
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		for (int i = 0; i < inputConfig.size(); i++) {
			int inputX = x + 68 + (i % 3 * 20);
			int inputY = y + 26 + (i / 3 * 20);
			if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
				BigItemStack itemStack = inputConfig.get(i);
				if (itemStack.stack.isEmpty())
					return true;
				itemStack.count =
					Mth.clamp((int) (itemStack.count + Math.signum(scrollY) * (hasShiftDown() ? 10 : 1)), 1, 64);
				return true;
			}
		}

		if (!restocker) {
			int outputX = x + 160;
			int outputY = y + 48;
			if (mouseX >= outputX && mouseX < outputX + 16 && mouseY >= outputY && mouseY < outputY + 16) {
				BigItemStack itemStack = outputConfig;
				itemStack.count =
					Mth.clamp((int) (itemStack.count + Math.signum(scrollY) * (hasShiftDown() ? 10 : 1)), 1, 64);
				return true;
			}
		}

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void removed() {
		sendIt(null, false);
		super.removed();
	}

	private void sendIt(@Nullable FactoryPanelPosition toRemove, boolean clearPromises) {
		Map<FactoryPanelPosition, Integer> inputs = new HashMap<>();

		if (inputConfig.size() == connections.size())
			for (int i = 0; i < inputConfig.size(); i++) {
				BigItemStack stackInConfig = inputConfig.get(i);
				inputs.put(connections.get(i).from, craftingActive ? (int) craftingIngredients.stream()
					.filter(
						b -> !b.stack.isEmpty() && ItemStack.isSameItemSameComponents(b.stack, stackInConfig.stack))
					.count() : stackInConfig.count);
			}

		List<ItemStack> craftingArrangement = craftingActive ? craftingIngredients.stream()
			.map(b -> b.stack)
			.toList() : List.of();

		FactoryPanelPosition pos = behaviour.getPanelPosition();
		int promiseExp = promiseExpiration.getState();
		String address = addressBox.getValue();

		FactoryPanelConfigurationPacket packet = new FactoryPanelConfigurationPacket(pos, address, inputs,
			craftingArrangement, outputConfig.count, promiseExp, toRemove, clearPromises, sendReset, sendRedstoneReset);
		CatnipServices.NETWORK.sendToServer(packet);
	}

	private void searchForCraftingRecipe() {
		ItemStack output = outputConfig.stack;
		if (output.isEmpty())
			return;
		if (behaviour.targetedBy.isEmpty())
			return;

		Set<Item> itemsToUse = inputConfig.stream()
			.map(b -> b.stack)
			.filter(i -> !i.isEmpty())
			.map(i -> i.getItem())
			.collect(Collectors.toSet());

		ClientLevel level = Minecraft.getInstance().level;

		availableCraftingRecipe = level.getRecipeManager()
			.getAllRecipesFor(RecipeType.CRAFTING)
			.parallelStream()
			.filter(r -> output.getItem() == r.value().getResultItem(level.registryAccess())
				.getItem())
			.filter(r -> {
				if (AllRecipeTypes.shouldIgnoreInAutomation(r))
					return false;

				Set<Item> itemsUsed = new HashSet<>();
				for (Ingredient ingredient : r.value().getIngredients()) {
					if (ingredient.isEmpty())
						continue;
					boolean available = false;
					for (BigItemStack bis : inputConfig) {
						if (!bis.stack.isEmpty() && ingredient.test(bis.stack)) {
							available = true;
							itemsUsed.add(bis.stack.getItem());
							break;
						}
					}
					if (!available)
						return false;
				}

				if (itemsUsed.size() < itemsToUse.size())
					return false;

				return true;
			})
			.findAny()
			.map(RecipeHolder::value)
			.orElse(null);
	}

}
