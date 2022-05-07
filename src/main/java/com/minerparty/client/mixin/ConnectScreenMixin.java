package com.minerparty.client.mixin;

import com.minerparty.client.util.AddrUtil;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   targets = {"net/minecraft/client/gui/screen/ConnectScreen$1"}
)
public abstract class ConnectScreenMixin {
   @Redirect(
      at = @At(
   value = "INVOKE",
   target = "Ljava/net/InetAddress;getByName(Ljava/lang/String;)Ljava/net/InetAddress;",
   opcode = 184,
   ordinal = 0
),
      method = {"run()V"}
   )
   private InetAddress parsePlayerAddress(String addr) throws UnknownHostException {
      return addr.startsWith("@") ? AddrUtil.createFakeAddr(addr.substring(1)) : InetAddress.getByName(addr);
   }
}
