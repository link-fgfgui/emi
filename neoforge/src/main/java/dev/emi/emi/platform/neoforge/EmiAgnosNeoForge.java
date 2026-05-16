package dev.emi.emi.platform.neoforge;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import dev.emi.emi.mixin.accessor.PotionBrewingAccessor;

import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.ClientHooks;
import org.apache.commons.lang3.text.WordUtils;
import org.objectweb.asm.Type;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.ModFileScanData;

public class EmiAgnosNeoForge extends EmiAgnos {

	static {
		EmiAgnos.delegate = new EmiAgnosNeoForge();
	}

	@Override
	protected boolean isForgeAgnos() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String getModNameAgnos(String namespace) {
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<? extends ModContainer> container = ModList.get().getModContainerById(namespace);
		if (container.isPresent()) {
			return container.get().getModInfo().getDisplayName();
		}
		container = ModList.get().getModContainerById(namespace.replace('_', '-'));
		if (container.isPresent()) {
			return container.get().getModInfo().getDisplayName();
		}
		return WordUtils.capitalizeFully(namespace.replace('_', ' '));
	}

	@Override
	protected Path getConfigDirectoryAgnos() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	protected boolean isDevelopmentEnvironmentAgnos() {
		return !FMLEnvironment.isProduction();
	}

	@Override
	protected boolean isModLoadedAgnos(String id) {
		return ModList.get().isLoaded(id);
	}

	@Override
	protected List<String> getAllModNamesAgnos() {
		return ModList.get().getMods().stream().map(m -> m.getDisplayName()).toList();
	}

	@Override
	protected List<String> getModsWithPluginsAgnos() {
		List<String> mods = Lists.newArrayList();
		Type entrypointType = Type.getType(EmiEntrypoint.class);
		for (ModFileScanData data : ModList.get().getAllScanData()) {
			for (ModFileScanData.AnnotationData annot : data.getAnnotations()) {
				try {
					if (entrypointType.equals(annot.annotationType())) {
						mods.add(data.getIModInfoData().get(0).getMods().get(0).getModId());
					}
				} catch (Throwable t) {
					EmiLog.error("Exception constructing entrypoint:", t);
				}
			}
		}
		return mods;
	}

	@Override
	protected List<EmiPluginContainer> getPluginsAgnos() {
		List<EmiPluginContainer> containers = Lists.newArrayList();
		Type entrypointType = Type.getType(EmiEntrypoint.class);
		for (ModFileScanData data : ModList.get().getAllScanData()) {
			for (ModFileScanData.AnnotationData annot : data.getAnnotations()) {
				try {
					if (entrypointType.equals(annot.annotationType())) {
						Class<?> clazz = Class.forName(annot.memberName());
						if (EmiPlugin.class.isAssignableFrom(clazz)) {
							Class<? extends EmiPlugin> pluginClass = clazz.asSubclass(EmiPlugin.class);
							EmiPlugin plugin = pluginClass.getConstructor().newInstance();
							String id = data.getIModInfoData().get(0).getMods().get(0).getModId();
							containers.add(new EmiPluginContainer(plugin, id));
						} else {
							EmiLog.error("EmiEntrypoint " + annot.memberName() + " does not implement EmiPlugin");
						}
					}
				} catch (Throwable t) {
					EmiLog.error("Exception constructing entrypoint:", t);
				}
			}
		}
		return containers;
	}

	@Override
	protected void addBrewingRecipesAgnos(EmiRegistry registry) {
        Level world = Minecraft.getInstance().world;
		PotionBrewing brewingRegistry = world != null ? Minecraft.getInstance().world.getBrewingRecipeRegistry() : PotionBrewing.EMPTY;
        ContextParameterMap paramMap = SlotDisplayContexts.createParameters(world);
        PotionBrewingAccessor brewingRegistryAccess = (PotionBrewingAccessor)brewingRegistry;

        for (Ingredient ingredient : brewingRegistryAccess.getContainers()) {
			for (ItemStack stack : ingredient.toDisplay().getStacks(paramMap)) {
				String pid = EmiUtil.subId(stack.getItem());
				for (PotionBrewing.Mix<Potion> recipe : brewingRegistryAccess.getPotionMixes()) {
					try {
						if (!recipe.ingredient().toDisplay().getStacks(paramMap).isEmpty()) {
							Material id = EmiPort.id("emi", "/brewing/" + pid
								+ "/" + EmiUtil.subId(recipe.ingredient().toDisplay().getStacks(paramMap).getFirst().getItem())
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId(recipe.from().value()))
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId(recipe.to().value())));
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(EmiPort.setPotion(stack.copy(), recipe.from().value())), EmiIngredient.of(recipe.ingredient()),
								EmiStack.of(EmiPort.setPotion(stack.copy(), recipe.to().value())), id));
						}
					} catch (Exception e) {
						EmiLog.error("Error registering brewing recipe", e);
					}
				}
			}
		}

		for (PotionBrewing.Mix<PotionItem> recipe : brewingRegistryAccess.getContainerMixes()) {
			try {
				if (!recipe.ingredient().toDisplay().getStacks(paramMap).isEmpty()) {
					String gid = EmiUtil.subId(recipe.ingredient().toDisplay().getStacks(paramMap).getFirst().getItem());
					String iid = EmiUtil.subId(recipe.from().value());
					String oid = EmiUtil.subId(recipe.to().value());
					Consumer<Holder<Potion>> potionRecipeGen = entry -> {
						Potion potion = entry.value();
						if (brewingRegistry.isBrewable(entry)) {
							Material id = EmiPort.id("emi", "/brewing/item/"
								+ EmiUtil.subId(entry.getKey().get().getValue()) + "/" + gid + "/" + iid + "/" + oid);
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(EmiPort.setPotion(new ItemStack(recipe.from().value()), potion)), EmiIngredient.of(recipe.ingredient()),
								EmiStack.of(EmiPort.setPotion(new ItemStack(recipe.to().value()), potion)), id));
						}
					};
					if ((recipe.from().value() instanceof PotionItem)) {
						EmiPort.getPotionRegistry().streamEntries().forEach(potionRecipeGen);
					} else {
						potionRecipeGen.accept(Potions.AWKWARD);
					}

				}
			} catch (Exception e) {
				EmiLog.error("Error registering brewing recipe", e);
			}
		}
		for (IBrewingRecipe ibr : brewingRegistry.getRecipes()) {
			try {
				if (ibr instanceof BrewingRecipe recipe) {
					for (ItemStack is : recipe.getInput().toDisplay().getStacks(paramMap)) {
						EmiStack input = EmiStack.of(is);
						EmiIngredient ingredient = EmiIngredient.of(recipe.getIngredient());
						EmiStack output = EmiStack.of(recipe.getOutput(is, recipe.getIngredient().toDisplay().getStacks(paramMap).getFirst()));
						Material id = EmiPort.id("emi", "/brewing/neoforge/"
							+ EmiUtil.subId(input.getId()) + "/"
							+ EmiUtil.subId(ingredient.getEmiStacks().get(0).getId()) + "/"
							+ EmiUtil.subId(output.getId()));
						registry.addRecipe(new EmiBrewingRecipe(input, ingredient, output, id));
					}
				}
			} catch (Exception e) {
				EmiLog.error("Error registering brewing recipe", e);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<String> getAllModAuthorsAgnos() {
		return ModList.get().getMods().stream().flatMap(m -> {
			Optional<Object> opt = m.getConfig().getConfigElement("authors");
			if (opt.isPresent()) {
				Object obj = opt.get();
				if (obj instanceof String authors) {
					return Lists.newArrayList(authors.split("\\,")).stream().map(s -> s.trim());
				} else if (obj instanceof List<?> list) {
					if (list.size() > 0 && list.get(0) instanceof String) {
						List<String> authors = (List<String>) list;
						return authors.stream();
					}
				}
			}
			return Stream.empty();
		}).distinct().toList();
	}

	@Override
	protected List<ClientTooltipComponent> getItemTooltipAgnos(ItemStack stack) {
		Minecraft client = Minecraft.getInstance();
		return ClientHooks.gatherTooltipComponents(stack, Screen.getTooltipFromItem(client, stack), stack.getTooltipData(), 0, Integer.MAX_VALUE, Integer.MAX_VALUE, client.textRenderer);
	}

	@Override
	protected Component getFluidNameAgnos(Fluid fluid, DataComponentPatch componentChanges) {
		return new FluidStack(fluid.getRegistryEntry(), 1000, componentChanges).getHoverName();
	}

	@Override
	protected List<Component> getFluidTooltipAgnos(Fluid fluid, DataComponentPatch componentChanges) {
		List<Component> tooltip = Lists.newArrayList();
		tooltip.add(getFluidName(fluid, componentChanges));
		Minecraft client = Minecraft.getInstance();
		if (client.options.advancedItemTooltips) {
			tooltip.add(EmiPort.literal(EmiPort.getFluidRegistry().getId(fluid).toString()).formatted(ChatFormatting.DARK_GRAY));
		}
		return tooltip;
	}

	@Override
	protected boolean isFloatyFluidAgnos(FluidEmiStack stack) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class).getRegistryEntry(), 1000, stack.getComponentChanges());
		return fs.getFluid().getFluidType().isLighterThanAir();
	}

    @Override
    protected void renderFluidAgnos(FluidEmiStack stack, EmiDrawContext context, int x, int y, float delta, int xOff,
                                    int yOff, int width, int height) {
        FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class).getRegistryEntry(), 1000, stack.getComponentChanges());
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fs.getFluid());
        Material texture = ext.getStillTexture(fs);
        if (texture == null) {
            return;
        }
        int color = ext.getTintColor(fs);
        Minecraft client = Minecraft.getInstance();
        TextureAtlasSprite sprite = client.getAtlasManager().getSprite(new SpriteIdentifier(TextureAtlas.BLOCK_ATLAS_TEXTURE, texture));
        EmiRenderHelper.drawTintedSprite(context, sprite, color, x, y, xOff, yOff, width, height);
    }

	@Override
	protected EmiStack createFluidStackAgnos(Object object) {
		if (object instanceof FluidStack f) {
			return EmiStack.of(f.getFluid(), f.getComponentsPatch(), f.getAmount());
		}
		return EmiStack.EMPTY;
	}

	@Override
	protected boolean canBatchAgnos(ItemStack stack) {
        return false;
//		MinecraftClient client = MinecraftClient.getInstance();
//		ItemRenderer ir = client.getItemRenderer();
//		BakedModel model = ir.getModel(stack, client.world, null, 0);
//		return model != null && model.getClass() == BasicBakedModel.class;
	}

	@Override
	protected Map<PotionItem, Integer> getFuelMapAgnos() {
		Object2IntMap<PotionItem> fuelMap = new Object2IntOpenHashMap<>();
		for (PotionItem item : EmiPort.getItemRegistry()) {
			int time = item.getDefaultStack().getBurnTime(RecipeType.SMELTING, Minecraft.getInstance().world.getFuelRegistry());
			if (time > 0) {
				fuelMap.put(item, time);
			}
		}
		return fuelMap;
	}

	@Override
	protected ItemModel getBakedTagModelAgnos(Material id) {
        return Minecraft.getInstance().getBakedModelManager().getItemModel(id);
	}

	@Override
	protected boolean isEnchantableAgnos(ItemStack stack, Enchantment enchantment) {
        return true;
//		ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
//		enchantedBook.addEnchantment(RegistryEntry.of(enchantment), enchantment.getMaxLevel());
//		return stack.isBookEnchantable(enchantedBook);
	}

    @Override
    protected <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllRecipesOfTypeAgnos(
            RecipeManager recipeManager,
            RecipeType<T> recipeType) {
        return EmiClientNeoForge.SYNCED_RECIPES.getAll(recipeType);
    }

    @Override
    protected <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatchesRecipeAgnos(
            RecipeManager recipeManager, RecipeType<T> recipeType, I input, Level world) {
        return EmiClientNeoForge.SYNCED_RECIPES.find(recipeType, input, world);
    }

    @Override
    protected <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatchRecipeAgnos(
            RecipeManager recipeManager, RecipeType<T> recipeType, I input, Level world) {
        return EmiClientNeoForge.SYNCED_RECIPES.find(recipeType, input, world).findFirst();
    }

    @Override
    protected Collection<RecipeHolder<?>> getAllRecipesAgnos(RecipeManager recipeManager) {
        return EmiClientNeoForge.SYNCED_RECIPES.recipes();
    }

    @Override
    protected RecipeHolder<?> getRecipeAgnos(RecipeManager recipeManager, Material id) {
        return EmiClientNeoForge.SYNCED_RECIPES.get(ResourceKey.of(Registries.RECIPE, id));
    }

}
