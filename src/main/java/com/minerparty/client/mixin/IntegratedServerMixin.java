package com.minerparty.client.mixin;

import com.minerparty.client.MinerParty;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import java.net.Proxy;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.UserCache;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.GameMode;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({IntegratedServer.class})
public abstract class IntegratedServerMixin extends MinecraftServer {
   @Shadow
   private int lanPort;

   public IntegratedServerMixin(Thread thread, DynamicRegistryManager.Impl impl, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager resourcePackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
      super(thread, impl, session, saveProperties, resourcePackManager, proxy, dataFixer, serverResourceManager, minecraftSessionService, gameProfileRepository, userCache, worldGenerationProgressListenerFactory);
   }

   public boolean isUsingNativeTransport() {
      return true;
   }

   @Inject(
      at = {@At("RETURN")},
      method = {"openToLan(Lnet/minecraft/world/GameMode;ZI)Z"}
   )
   private void openToLanReturn(GameMode gameMode, boolean cheatsAllowed, int port, CallbackInfoReturnable<Boolean> info) {
      if (info.getReturnValueZ()) {
         try {
            MinerParty.getInstance().getClient().listenMinerParty(port);
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }

   }

   @Inject(
      at = {@At("HEAD")},
      method = {"shutdown()V"}
   )
   private void shutdownHead(CallbackInfo info) {
      if (this.isRemote()) {
         try {
            MinerParty.getInstance().getClient().closeMinerParty(this.lanPort);
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }

   }
}
