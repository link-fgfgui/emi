package dev.emi.emi.data;

import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.emi.emi.EmiPort;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class RecipeDefaultLoader extends SimplePreparableReloadListener<RecipeDefaults>
		implements EmiResourceReloadListener {
	private static final Gson GSON = new Gson();
	public static final ResourceLocation ID = EmiPort.id("emi:recipe_defaults");

	@Override
	protected RecipeDefaults prepare(ResourceManager manager, ProfilerFiller profiler) {
		RecipeDefaults defaults = new RecipeDefaults();
		for (ResourceLocation id : EmiPort.findResources(manager, "recipe/defaults", i -> i.endsWith(".json"))) {
			if (!id.getNamespace().equals("emi")) {
				continue;
			}
			try {
				for (Resource resource : manager.getAllResources(id)) {
					InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream(resource));
					JsonObject json = GsonHelper.deserialize(GSON, reader, JsonObject.class);
					loadDefaults(defaults, json);
				}
			} catch (Exception e) {
				EmiLog.error("Error loading recipe default file " + id, e);
			}
		}
		return defaults;
	}

	@Override
	protected void apply(RecipeDefaults prepared, ResourceManager manager, ProfilerFiller profiler) {
		BoM.setDefaults(prepared);
	}
	
	@Override
	public ResourceLocation getEmiId() {
		return ID;
	}

	public static void loadDefaults(RecipeDefaults defaults, JsonObject json) {
		if (GsonHelper.getBoolean(json, "replace", false)) {
			defaults.clear();
		}
		JsonArray disabled = GsonHelper.getArray(json, "disabled", new JsonArray());
		for (JsonElement el : disabled) {
			ResourceLocation id = EmiPort.id(el.getAsString());
			defaults.remove(id);
		}
		JsonArray added = GsonHelper.getArray(json, "added", new JsonArray());
		if (GsonHelper.hasArray(json, "recipes")) {
			added.addAll(GsonHelper.getArray(json, "recipes"));
		}
		for (JsonElement el : added) {
			ResourceLocation id = EmiPort.id(el.getAsString());
			defaults.add(id);
		}
		JsonObject resolutions = GsonHelper.getObject(json, "resolutions", new JsonObject());
		for (String key : resolutions.keySet()) {
			ResourceLocation id = EmiPort.id(key);
			if (GsonHelper.hasArray(resolutions, key)) {
				defaults.add(id, GsonHelper.getArray(resolutions, key));
			}
		}
		JsonObject addedTags = GsonHelper.getObject(json, "tags", new JsonObject());
		for (String key : addedTags.keySet()) {
			defaults.addTag(new JsonPrimitive(key), addedTags.get(key));
		}
	}
}
