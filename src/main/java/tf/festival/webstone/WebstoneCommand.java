package tf.festival.webstone;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import tf.festival.webstone.WebstoneRegistry.WebstoneRegistryContext;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class WebstoneCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("webstone")
            .then(Commands.literal("help")
                .executes(WebstoneCommand::printHelp))
            .then(Commands.literal("context")
                .then(Commands.literal("server").executes((command) -> switchRegistryContext(command, WebstoneRegistryContext.SERVER)))
                .then(Commands.literal("player").executes((command) -> switchRegistryContext(command, WebstoneRegistryContext.PLAYER)))
            )
            .then(Commands.literal("genpass")
                .executes(WebstoneCommand::generatePassphrase))
            .then(Commands.literal("setpass")
                .then(Commands.argument("passphrase", StringArgumentType.string())
                    .executes(WebstoneCommand::setPassphrase)))
        );
    }

    private static int printHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().getPlayer().sendSystemMessage(Component.literal("/webstone help"));
        context.getSource().getPlayer().sendSystemMessage(Component.literal("  Shows this help text."));

        context.getSource().getPlayer().sendSystemMessage(Component.literal("/webstone context (server | player)"));
        context.getSource().getPlayer().sendSystemMessage(Component.literal("  Switches between public (server) or personal (player) block lists when registering a new Webstone block."));

        context.getSource().getPlayer().sendSystemMessage(Component.literal("/webstone genpass"));
        context.getSource().getPlayer().sendSystemMessage(Component.literal("  Generates a random passphrase for your personal block list."));

        context.getSource().getPlayer().sendSystemMessage(Component.literal("/webstone context (server | player)"));
        context.getSource().getPlayer().sendSystemMessage(Component.literal("  Sets a custom passphrase for your personal block list."));

        return Command.SINGLE_SUCCESS;
    }

    private static int switchRegistryContext(CommandContext<CommandSourceStack> context, WebstoneRegistryContext registryContext) {
        UUID playerId = context.getSource().getPlayer().getUUID();

        if (registryContext.equals(WebstoneRegistry.getUserRegistryContext(playerId))) {
            return Command.SINGLE_SUCCESS;
        }

        switch (registryContext) {
            case SERVER -> {
                WebstoneRegistry.setUserRegistryContext(playerId, registryContext);
                context.getSource().getPlayer().sendSystemMessage(Component.literal("Switched to server context."));
            }
            case PLAYER -> {
                WebstoneRegistry.setUserRegistryContext(playerId, registryContext);
                context.getSource().getPlayer().sendSystemMessage(Component.literal("Switched to player context."));
            }
            default -> context.getSource().getPlayer().sendSystemMessage(Component.literal("Unknown context."));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int generatePassphrase(CommandContext<CommandSourceStack> context) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String num = "0123456789";

        String combination = upper + upper.toLowerCase() + num;
        int len = 8;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(combination.charAt(
                ThreadLocalRandom.current().nextInt(
                    combination.length()
                )
            ));
        }

        WebstoneRegistry.getOrCreateRegistry(context.getSource().getPlayer().getUUID()).setPassphrase(sb.toString());

        context.getSource().getPlayer().sendSystemMessage(Component.literal(String.format("Your passphrase is \"%s\".", sb)));
        context.getSource().getPlayer().sendSystemMessage(Component.literal("You can change it using \"/webstone setpass <passphrase>\"."));

        return Command.SINGLE_SUCCESS;
    }

    private static int setPassphrase(CommandContext<CommandSourceStack> context) {
        WebstoneRegistry.getOrCreateRegistry(context.getSource().getPlayer().getUUID()).setPassphrase(StringArgumentType.getString(context, "passphrase"));
        context.getSource().getPlayer().sendSystemMessage(Component.literal("Your passphrase has been successfully changed."));

        return Command.SINGLE_SUCCESS;
    }
}