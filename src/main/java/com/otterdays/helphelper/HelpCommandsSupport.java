package com.otterdays.helphelper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.otterdays.helphelper.network.OpenHelpPayload.CommandEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;

final class HelpCommandsSupport {
    private HelpCommandsSupport() {
    }

    /**
     * Collects usable slash-prefixed command strings for {@code source} from the server's dispatcher.
     */
    static List<CommandEntry> collectFor(CommandSourceStack source) {
        CommandDispatcher<CommandSourceStack> dispatcher = source.dispatcher();
        List<CommandEntry> acc = new ArrayList<>();
        for (CommandNode<CommandSourceStack> child : dispatcher.getRoot().getChildren()) {
            collectUsages(child, source, "", "", child.getName(), acc);
        }
        acc.sort(Comparator.comparing(CommandEntry::command));
        return acc;
    }

    private static void collectUsages(CommandNode<CommandSourceStack> node, CommandSourceStack source,
        String commandPrefix, String syntaxPrefix, String root, List<CommandEntry> acc) {
        if (!node.canUse(source)) {
            return;
        }
        String commandSegment = node.getName();
        String syntaxSegment = node.getUsageText();
        String builtCommand = appendSegment(commandPrefix, commandSegment);
        String builtSyntax = appendSegment(syntaxPrefix, syntaxSegment);

        if (node.getCommand() != null && !builtCommand.isEmpty()) {
            String command = "/" + builtCommand.trim();
            String syntax = "/" + builtSyntax.trim();
            acc.add(new CommandEntry(command, root, syntaxPreview(syntax, node), originHint(root)));
        }
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            collectUsages(child, source, builtCommand, builtSyntax, root, acc);
        }
    }

    private static String appendSegment(String prefix, String segment) {
        if (segment == null || segment.isEmpty()) {
            return prefix;
        }
        return prefix.isEmpty() ? segment : prefix + " " + segment;
    }

    private static String syntaxPreview(String syntax, CommandNode<CommandSourceStack> node) {
        List<String> childUsages = node.getChildren().stream()
            .map(CommandNode::getUsageText)
            .filter(part -> part != null && !part.isBlank())
            .sorted()
            .limit(4)
            .toList();
        if (childUsages.isEmpty()) {
            return syntax;
        }
        String suffix = String.join(" | ", childUsages);
        if (node.getChildren().size() > childUsages.size()) {
            suffix += " | ...";
        }
        return syntax + " [" + suffix + "]";
    }

    private static String originHint(String root) {
        if (root == null || root.isBlank()) {
            return "Server command";
        }
        int namespace = root.indexOf(':');
        if (namespace > 0) {
            return "Namespace " + root.substring(0, namespace);
        }
        return "Brigadier root /" + root;
    }
}
