package net.raynna.raynnarpg.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.raynna.raynnarpg.network.packets.message.MessagePacket;
import net.raynna.raynnarpg.network.packets.message.MessagePacketHandler;
import net.raynna.raynnarpg.network.packets.skills.SkillsPacket;
import net.raynna.raynnarpg.network.packets.skills.SkillsPacketHandler;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class Packets {

    @SubscribeEvent
    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        String version = "1.0";
        PayloadRegistrar registrar = event.registrar(version);
        registerServerPackets(registrar);
        registerClientPackets(registrar);
    }

    public static void registerClientPackets(PayloadRegistrar registrar) {
        registrar.playToClient(MessagePacket.TYPE, MessagePacket.CODEC, new MessagePacketHandler());
    }


    private static void registerServerPackets(PayloadRegistrar registrar) {
        registrar.playToClient(SkillsPacket.TYPE, SkillsPacket.CODEC, new SkillsPacketHandler());
    }

}