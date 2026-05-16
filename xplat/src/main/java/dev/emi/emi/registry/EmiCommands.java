package dev.emi.emi.registry;

import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiNetwork;

import net.minecraft.command.DefaultPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class EmiCommands {
	public static final byte VIEW_RECIPE = 0x01;
	public static final byte VIEW_TREE = 0x02;
	public static final byte TREE_GOAL = 0x11;
	public static final byte TREE_RESOLUTION = 0x12;
	
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("emi")
			.requires(source -> source.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS))
			.then(
				literal("view")
				.then(
					literal("recipe")
					.then(
						argument("id", identifier())
						.executes(context -> {
							send(context.getSource().getPlayer(), VIEW_RECIPE, context.getArgument("id", ResourceLocation.class));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(
					literal("tree")
					.executes(context -> {
						send(context.getSource().getPlayer(), VIEW_TREE, null);
						return Command.SINGLE_SUCCESS;
					})
				)
			)
			.then(
				literal("tree")
				.then(
					literal("goal")
					.then(
						argument("id", identifier())
						.executes(context -> {
							send(context.getSource().getPlayer(), TREE_GOAL, context.getArgument("id", ResourceLocation.class));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
				.then(
					literal("resolution")
					.then(
						argument("id", identifier())
						.executes(context -> {
							send(context.getSource().getPlayer(), TREE_RESOLUTION, context.getArgument("id", ResourceLocation.class));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
			)
		);
	}

	private static void send(ServerPlayer player, byte type, @Nullable ResourceLocation id) {
		EmiNetwork.sendToClient(player, new CommandS2CPacket(type, id));
	}
}
