package com.minerparty.client.mixin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ServerNetworkIo.class})
public abstract class ServerNetworkIoMixin {
   @Redirect(
      at = @At(
   value = "INVOKE",
   target = "Lio/netty/bootstrap/ServerBootstrap;bind()Lio/netty/channel/ChannelFuture;",
   opcode = 182,
   ordinal = 0
),
      method = {"bind(Ljava/net/InetAddress;I)V"}
   )
   private ChannelFuture bindSetOption(ServerBootstrap bootstrap) {
      try {
         bootstrap = (ServerBootstrap)bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return ((ServerBootstrap)bootstrap.option(ChannelOption.SO_REUSEADDR, true)).bind();
   }
}
