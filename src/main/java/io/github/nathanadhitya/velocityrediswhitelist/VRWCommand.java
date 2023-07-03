package io.github.nathanadhitya.velocityrediswhitelist;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VRWCommand implements SimpleCommand {

    private final VelocityRedisWhitelist plugin;

    public VRWCommand(VelocityRedisWhitelist plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /vrw reload").color(NamedTextColor.WHITE));
            return;
        } else if (args.length > 1) {
            source.sendMessage(Component.text("Too many arguments!").color(NamedTextColor.RED));
            return;
        } else {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                source.sendMessage(Component.text("Configuration reloaded!").color(NamedTextColor.GREEN));
            } else {
                source.sendMessage(Component.text("Unknown argument!").color(NamedTextColor.RED));
                return;
            }
        }
    }

    // This method allows you to control who can execute the command.
    // If the executor does not have the required permission,
    // the execution of the command and the control of its autocompletion
    // will be sent directly to the server on which the sender is located
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("vrw.admin");
    }

    // Here you can offer argument suggestions in the same way as the previous method,
    // but asynchronously. It is recommended to use this method instead of the previous one
    // especially in cases where you make a more extensive logic to provide the suggestions
    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {

        return CompletableFuture.completedFuture(invocation.arguments().length == 0 ? List.of("reload") : List.of());
    }
}
