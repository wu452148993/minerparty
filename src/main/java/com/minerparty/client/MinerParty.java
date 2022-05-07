package com.minerparty.client;

import com.minerparty.websocket.MinerPartyClient;
import java.io.IOException;

import com.simtechdata.waifupnp.UPnP;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class MinerParty implements ClientModInitializer {
   private static MinerParty INSTANCE;
   private static final Logger LOGGER = LogManager.getLogger("MinerParty");
   private MinerPartyClient client = null;

   public void onInitializeClient() {
      INSTANCE = this;
      (new Thread(() -> {
         LOGGER.info("Checking UPnP availability");
         if (UPnP.isUPnPAvailable()) {
            LOGGER.info("UPnP is available");
         } else {
            LOGGER.info("UPnP is not available");
         }

      })).start();

      try {
         this.client = new MinerPartyClient();
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }

   public static MinerParty getInstance() {
      return INSTANCE;
   }

   public MinerPartyClient getClient() {
      return this.client;
   }
}
