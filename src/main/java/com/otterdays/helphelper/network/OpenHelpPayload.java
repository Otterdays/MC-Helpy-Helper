package com.otterdays.helphelper.network;

import com.otterdays.helphelper.HelpHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server → client: command list to populate the help GUI. */
public record OpenHelpPayload(List<String> commands) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenHelpPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(HelpHelper.MOD_ID, "open_help"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenHelpPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
        OpenHelpPayload::commands,
        OpenHelpPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
