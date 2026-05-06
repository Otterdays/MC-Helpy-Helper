package com.otterdays.helphelper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.otterdays.helphelper.network.OpenHelpPayload.CommandEntry;
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
            collectUsages(child, source, "", acc);
        }
        acc.sort(Comparator.comparing(CommandEntry::command));
        return acc;
    }

    private static void collectUsages(CommandNode<CommandSourceStack> node, CommandSourceStack source,
        String prefix, List<CommandEntry> acc) {
        if (!node.canUse(source)) {
            return;
        }
        String segment = node.getName();
        String built = segment.isEmpty() ? prefix : (prefix.isEmpty() ? segment : prefix + " " + segment);

        if (node.getCommand() != null && !built.isEmpty()) {
            acc.add(new CommandEntry("/" + built.trim(), segment));
        }
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            collectUsages(child, source, built.isEmpty() ? prefix : built, acc);
        }
    }
}
