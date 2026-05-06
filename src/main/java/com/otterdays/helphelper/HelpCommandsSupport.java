package com.otterdays.helphelper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
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
    static List<String> collectFor(CommandSourceStack source) {
        CommandDispatcher<CommandSourceStack> dispatcher = source.dispatcher();
        List<String> acc = new ArrayList<>();
        for (CommandNode<CommandSourceStack> child : dispatcher.getRoot().getChildren()) {
            collectUsages(child, source, "", acc);
        }
        acc.sort(Comparator.naturalOrder());
        return acc;
    }

    private static void collectUsages(CommandNode<CommandSourceStack> node, CommandSourceStack source,
        String prefix, List<String> acc) {
        if (!node.canUse(source)) {
            return;
        }
        String segment = node.getName();
        String built = segment.isEmpty() ? prefix : (prefix.isEmpty() ? segment : prefix + " " + segment);

        if (node.getCommand() != null && !built.isEmpty()) {
            acc.add("/" + built.trim());
        }
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            collectUsages(child, source, built.isEmpty() ? prefix : built, acc);
        }
    }
}
