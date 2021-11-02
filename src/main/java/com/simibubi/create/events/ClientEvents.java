package com.simibubi.create.events;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.components.fan.AirCurrent;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.ChassisRangeDisplay;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandlerClient;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingPhysics;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.train.capability.CapabilityMinecartController;
import com.simibubi.create.content.contraptions.components.turntable.TurntableHandler;
import com.simibubi.create.content.contraptions.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlicer;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorHandler;
import com.simibubi.create.content.curiosities.armor.CopperBacktankArmorLayer;
import com.simibubi.create.content.curiosities.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.curiosities.tools.BlueprintOverlayRenderer;
import com.simibubi.create.content.curiosities.tools.ExtendoGripRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.WorldshaperRenderHandler;
import com.simibubi.create.content.logistics.block.depot.EjectorTargetHandler;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPointHandler;
import com.simibubi.create.content.logistics.item.LinkedControllerClientHandler;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.ui.BaseConfigScreen;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.ponder.PonderTooltipHandler;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {

	private static final String ITEM_PREFIX = "item." + Create.ID;
	private static final String BLOCK_PREFIX = "block." + Create.ID;

	@SubscribeEvent
	public static void onTick(ClientTickEvent event) {
		if (!isGameActive())
			return;

		World world = Minecraft.getInstance().level;
		if (event.phase == Phase.START) {
			LinkedControllerClientHandler.tick();
			AirCurrent.tickClientPlayerSounds();
			return;
		}

		SoundScapes.tick();
		AnimationTickHolder.tick();
		ScrollValueHandler.tick();

		CreateClient.SCHEMATIC_SENDER.tick();
		CreateClient.SCHEMATIC_AND_QUILL_HANDLER.tick();
		CreateClient.SCHEMATIC_HANDLER.tick();
		CreateClient.ZAPPER_RENDER_HANDLER.tick();
		CreateClient.POTATO_CANNON_RENDER_HANDLER.tick();
		CreateClient.SOUL_PULSE_EFFECT_HANDLER.tick(world);

		ContraptionHandler.tick(world);
		CapabilityMinecartController.tick(world);
		CouplingPhysics.tick(world);

		PonderTooltipHandler.tick();
		// ScreenOpener.tick();
		ServerSpeedProvider.clientTick();
		BeltConnectorHandler.tick();
//		BeltSlicer.tickHoveringInformation();
		FilteringRenderer.tick();
		LinkRenderer.tick();
		ScrollValueRenderer.tick();
		ChassisRangeDisplay.tick();
		EdgeInteractionRenderer.tick();
		WorldshaperRenderHandler.tick();
		CouplingHandlerClient.tick();
		CouplingRenderer.tickDebugModeRenders();
		KineticDebugger.tick();
		ExtendoGripRenderHandler.tick();
		// CollisionDebugger.tick();
		ArmInteractionPointHandler.tick();
		EjectorTargetHandler.tick();
		PlacementHelpers.tick();
		CreateClient.OUTLINER.tickOutlines();
		CreateClient.GHOST_BLOCKS.tickGhosts();
		ContraptionRenderDispatcher.tick(world);
		BlueprintOverlayRenderer.tick();
		ToolboxHandlerClient.clientTick();
	}

	@SubscribeEvent
	public static void onJoin(ClientPlayerNetworkEvent.LoggedInEvent event) {
		CreateClient.checkGraphicsFanciness();
	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		IWorld world = event.getWorld();
		if (world.isClientSide() && world instanceof ClientWorld && !(world instanceof WrappedClientWorld)) {
			CreateClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	@SubscribeEvent
	public static void onUnloadWorld(WorldEvent.Unload event) {
		if (event.getWorld()
			.isClientSide()) {
			CreateClient.invalidateRenderers();
			CreateClient.SOUL_PULSE_EFFECT_HANDLER.refresh();
			AnimationTickHolder.reset();
		}
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		Vector3d cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();
		float pt = AnimationTickHolder.getPartialTicks();

		MatrixStack ms = event.getMatrixStack();
		ms.pushPose();
		ms.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

		CouplingRenderer.renderAll(ms, buffer);
		CreateClient.SCHEMATIC_HANDLER.render(ms, buffer);
		CreateClient.GHOST_BLOCKS.renderAll(ms, buffer);

		CreateClient.OUTLINER.renderOutlines(ms, buffer, pt);
		// LightVolumeDebugger.render(ms, buffer);
		buffer.draw();
		RenderSystem.enableCull();

		ms.popPose();
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		MatrixStack ms = event.getMatrixStack();
		Impl buffers = Minecraft.getInstance()
			.renderBuffers()
			.bufferSource();
		int light = 0xF000F0;
		int overlay = OverlayTexture.NO_OVERLAY;
		float pt = event.getPartialTicks();

		if (event.getType() == ElementType.AIR)
			CopperBacktankArmorLayer.renderRemainingAirOverlay(ms, buffers, light, overlay, pt);
		if (event.getType() != ElementType.HOTBAR)
			return;

		onRenderHotbar(ms, buffers, light, overlay, pt);
	}

	public static void onRenderHotbar(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay,
		float partialTicks) {
		CreateClient.SCHEMATIC_HANDLER.renderOverlay(ms, buffer, light, overlay, partialTicks);
		LinkedControllerClientHandler.renderOverlay(ms, buffer, light, overlay, partialTicks);
		BlueprintOverlayRenderer.renderOverlay(ms, buffer, light, overlay, partialTicks);
		GoggleOverlayRenderer.renderOverlay(ms, buffer, light, overlay, partialTicks);
		ToolboxHandlerClient.renderOverlay(ms, buffer, light, overlay, partialTicks);
	}

	@SubscribeEvent
	public static void getItemTooltipColor(RenderTooltipEvent.Color event) {
		PonderTooltipHandler.handleTooltipColor(event);
	}

	@SubscribeEvent
	public static void addToItemTooltip(ItemTooltipEvent event) {
		if (!AllConfigs.CLIENT.tooltips.get())
			return;
		if (event.getPlayer() == null)
			return;

		ItemStack stack = event.getItemStack();
		String translationKey = stack.getItem()
			.getDescriptionId(stack);

		if (translationKey.startsWith(ITEM_PREFIX) || translationKey.startsWith(BLOCK_PREFIX))
			if (TooltipHelper.hasTooltip(stack, event.getPlayer())) {
				List<ITextComponent> itemTooltip = event.getToolTip();
				List<ITextComponent> toolTip = new ArrayList<>();
				toolTip.add(itemTooltip.remove(0));
				TooltipHelper.getTooltip(stack)
					.addInformation(toolTip);
				itemTooltip.addAll(0, toolTip);
			}

		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem) stack.getItem();
			if (item.getBlock() instanceof IRotate || item.getBlock() instanceof EngineBlock) {
				List<ITextComponent> kineticStats = ItemDescription.getKineticStats(item.getBlock());
				if (!kineticStats.isEmpty()) {
					event.getToolTip()
						.add(new StringTextComponent(""));
					event.getToolTip()
						.addAll(kineticStats);
				}
			}
		}

		PonderTooltipHandler.addToTooltip(event.getToolTip(), stack);
		SequencedAssemblyRecipe.addToTooltip(event.getToolTip(), stack);
	}

	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent event) {
		if (!isGameActive())
			return;
		TurntableHandler.gameRenderTick();
	}

	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	@SubscribeEvent
	public static void getFogDensity(EntityViewRenderEvent.FogDensity event) {
		ActiveRenderInfo info = event.getInfo();
		FluidState fluidState = info.getFluidInCamera();
		if (fluidState.isEmpty())
			return;
		Fluid fluid = fluidState.getType();

		if (fluid.isSame(AllFluids.CHOCOLATE.get())) {
			event.setDensity(5f);
			event.setCanceled(true);
			return;
		}

		if (fluid.isSame(AllFluids.HONEY.get())) {
			event.setDensity(1.5f);
			event.setCanceled(true);
			return;
		}

		if (FluidHelper.isWater(fluid) && AllItems.DIVING_HELMET.get()
			.isWornBy(Minecraft.getInstance().cameraEntity)) {
			event.setDensity(0.010f);
			event.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent
	public static void getFogColor(EntityViewRenderEvent.FogColors event) {
		ActiveRenderInfo info = event.getInfo();
		FluidState fluidState = info.getFluidInCamera();
		if (fluidState.isEmpty())
			return;
		Fluid fluid = fluidState.getType();

		if (fluid.isSame(AllFluids.CHOCOLATE.get())) {
			event.setRed(98 / 256f);
			event.setGreen(32 / 256f);
			event.setBlue(32 / 256f);
		}

		if (fluid.isSame(AllFluids.HONEY.get())) {
			event.setRed(234 / 256f);
			event.setGreen(174 / 256f);
			event.setBlue(47 / 256f);
		}
	}

	@SubscribeEvent
	public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof ZapperItem) {
			AllPackets.channel.sendToServer(new LeftClickPacket());
		}
	}

	public static void loadCompleted(FMLLoadCompleteEvent event) {
		ModContainer createContainer = ModList.get()
			.getModContainerById(Create.ID)
			.orElseThrow(() -> new IllegalStateException("Create Mod Container missing after loadCompleted"));
		createContainer.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
			() -> (mc, previousScreen) -> BaseConfigScreen.forCreate(previousScreen));
	}

}
