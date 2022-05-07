package com.minerparty.client.mixin;

import com.minerparty.client.MinerParty;
import com.minerparty.client.util.SkinUtil;
import com.minerparty.websocket.packet.s2c.CPacketPlayerStatus;
import java.util.Collections;

import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MultiplayerServerListPinger.class})
public abstract class MultiplayerServerListPingerMixin {
   @Shadow
   private static Text createPlayerCountText(int current, int max) {
      return null;
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"add(Lnet/minecraft/client/network/ServerInfo;Ljava/lang/Runnable;)V"},
      cancellable = true
   )
   private void addHead(ServerInfo serverInfo, Runnable runnable, CallbackInfo info) {
      if (serverInfo.address.startsWith("@")) {
         if (serverInfo.getIcon() == null) {
            serverInfo.setIcon(SkinUtil.DEFAULT_AVATAR);
         }

         String username = serverInfo.address.substring(1);
         String skin = SkinUtil.getAvatar(username);
         serverInfo.setIcon(skin);

         try {
            CPacketPlayerStatus packet = MinerParty.getInstance().getClient().checkPlayerStatus(username);
            if (packet.Online) {
               serverInfo.protocolVersion = packet.Version;
               serverInfo.version = new LiteralText("MinerParty");
               serverInfo.playerCountLabel = Text.of("");
               serverInfo.playerListSummary = Collections.emptyList();
               serverInfo.label = (new LiteralText(username + " is online")).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
               serverInfo.ping = 1L;
            } else {
               serverInfo.protocolVersion = Integer.MIN_VALUE;
               serverInfo.version = new LiteralText("Offline");
               serverInfo.label = (new LiteralText(username + " doesn't seem to be online")).setStyle(Style.EMPTY.withColor(Formatting.GREEN));
            }
         } catch (Exception var7) {
            serverInfo.protocolVersion = Integer.MIN_VALUE;
            serverInfo.version = new LiteralText("Error");
            serverInfo.label = (new LiteralText("Cannot connect to MinerParty")).setStyle(Style.EMPTY.withColor(Formatting.RED));
            var7.printStackTrace();
         }

         runnable.run();
         info.cancel();
      }

   }
}
