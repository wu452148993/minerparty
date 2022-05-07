package com.minerparty.client.mixin;

import com.minerparty.client.MinerParty;
import com.minerparty.client.util.AddrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ClientConnection.class})
public abstract class ClientConnectionMixin {
   @Redirect(
      at = @At(
   value = "INVOKE",
   target = "Lio/netty/bootstrap/Bootstrap;connect(Ljava/net/InetAddress;I)Lio/netty/channel/ChannelFuture;",
   opcode = 182,
   ordinal = 0
),
      method = {"connect(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/ClientConnection;"}
   )
   private static ChannelFuture connectReturn(Bootstrap bootstrap, InetAddress inetHost, int inetPort) {
      String username = AddrUtil.isFakeAddr(inetHost);
      if (username == null) {
         return bootstrap.connect(inetHost, inetPort);
      } else {
         try {
            Channel channel = bootstrap.bind(0).sync().channel();
            int port = ((InetSocketAddress)channel.localAddress()).getPort();
            return channel.connect(MinerParty.getInstance().getClient().getEndpoint(username, port));
         } catch (Exception var6) {
            throw new RuntimeException(var6);
         }
      }
   }
}
