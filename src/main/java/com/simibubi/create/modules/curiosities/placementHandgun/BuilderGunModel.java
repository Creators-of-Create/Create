package com.simibubi.create.modules.curiosities.placementHandgun;

import java.util.Arrays;
import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.block.CustomRenderItemBakedModel;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.client.event.ModelBakeEvent;

@SuppressWarnings("deprecation")
public class BuilderGunModel extends CustomRenderItemBakedModel {

	public boolean showBlock;

	public IBakedModel core;
	public IBakedModel body;
	public IBakedModel ampCore;
	public IBakedModel acc;

	public IBakedModel goldBody;
	public IBakedModel goldScope;
	public IBakedModel goldAmp;
	public IBakedModel goldRetriever;
	public IBakedModel goldAcc;

	public IBakedModel chorusBody;
	public IBakedModel chorusScope;
	public IBakedModel chorusAmp;
	public IBakedModel chorusRetriever;
	public IBakedModel chorusAcc;

	public BuilderGunModel(IBakedModel template) {
		super(template);
	}

	public static List<String> getCustomModelLocations() {
		String p = "placement_handgun/";
		return Arrays.asList(p + "core", p + "body", p + "amplifier_core", p + "accelerator", p + "gold_body",
				p + "gold_scope", p + "gold_amplifier", p + "gold_retriever", p + "gold_accelerator", p + "chorus_body",
				p + "chorus_amplifier", p + "chorus_retriever", p + "chorus_accelerator");
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
		showBlock = cameraTransformType == TransformType.GUI;
		return super.handlePerspective(cameraTransformType);
	}

	@Override
	public CustomRenderItemBakedModel loadPartials(ModelBakeEvent event) {
		String p = "placement_handgun/";

		this.core = loadCustomModel(event, p + "core");
		this.body = loadCustomModel(event, p + "body");
		this.ampCore = loadCustomModel(event, p + "amplifier_core");
		this.acc = loadCustomModel(event, p + "accelerator");

		this.goldBody = loadCustomModel(event, p + "gold_body");
		this.goldScope = loadCustomModel(event, p + "gold_scope");
		this.goldAmp = loadCustomModel(event, p + "gold_amplifier");
		this.goldRetriever = loadCustomModel(event, p + "gold_retriever");
		this.goldAcc = loadCustomModel(event, p + "gold_accelerator");

		this.chorusBody = loadCustomModel(event, p + "chorus_body");
		this.chorusScope = loadCustomModel(event, p + "chorus_scope");
		this.chorusAmp = loadCustomModel(event, p + "chorus_amplifier");
		this.chorusRetriever = loadCustomModel(event, p + "chorus_retriever");
		this.chorusAcc = loadCustomModel(event, p + "chorus_accelerator");

		return this;
	}

}
