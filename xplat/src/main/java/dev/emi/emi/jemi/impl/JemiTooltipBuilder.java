package dev.emi.emi.jemi.impl;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;

import dev.emi.emi.runtime.EmiLog;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiKeyMapping;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;

public class JemiTooltipBuilder implements ITooltipBuilder {
	public final List<ClientTooltipComponent> tooltip = Lists.newArrayList();

	@Override
	public void add(FormattedText component) {
		// JEI allows non-text StringVisitable... Minecraft's methods don't easily
		if (component instanceof Component text) {
			tooltip.add(ClientTooltipComponent.of(text.asOrderedText()));
		}
	}

	@Override
	public void addAll(Collection<? extends FormattedText> components) {
		for (FormattedText v : components) {
			add(v);
		}
	}

	@Override
	public void add(TooltipComponent data) {
		try {
			tooltip.add(ClientTooltipComponent.of(data));
		} catch (Exception e) {
			EmiLog.error("Error converting TooltipComponent", e);
		}
	}

    @Override
    public void addKeyUsageComponent(String s, IJeiKeyMapping iJeiKeyMapping) {

    }

    @Override
	public void setIngredient(ITypedIngredient<?> typedIngredient) {
		// EMI's methods bypass the vanilla tooltip render which accepts a stack, so this will do nothing
	}

	@Override
	public void clear() {
		// EMI does not support tooltip removeal, this will only clear the user's additions
	}

    @Override
    public void clearIngredient() {

    }

    @Override
    public List<Either<FormattedText, TooltipComponent>> getLines() {
        return List.of();
    }

}
