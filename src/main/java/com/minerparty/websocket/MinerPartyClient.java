package com.minerparty.websocket;

import com.google.gson.Gson;
import com.minerparty.client.util.HolePunchUtil;
import com.minerparty.client.util.UUIDUtil;
import com.minerparty.websocket.packet.s2c.CPacketIncomingConnection;
import com.minerparty.websocket.packet.s2c.CPacketPlayerEndpoint;
import com.minerparty.websocket.packet.s2c.CPacketPlayerStatus;
import com.mojang.authlib.exceptions.AuthenticationException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import com.simtechdata.waifupnp.UPnP;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class MinerPartyClient {
   private final SecureRandom random = new SecureRandom();
   private final HttpClient client = HttpClientBuilder.create().build();
   private final Logger LOGGER = LogManager.getLogger("MinerParty");
   public final String apiDomain;
   public final String wsEndpoint;
   public final String httpEndpoint;
   private final String endpointPlayerStatus;
   private final String endpointPlayerEndpoint;
   private final URI endpointListen;
   private MinerPartyClient.WSClient wsClient = null;

   public MinerPartyClient() throws IOException {
      HttpGet request = new HttpGet("https://raw.githubusercontent.com/MinerParty/minerparty.github.io/master/api-domain.txt");
      this.apiDomain = EntityUtils.toString(this.client.execute(request).getEntity()).trim();
      this.wsEndpoint = "wss://" + this.apiDomain;
      this.httpEndpoint = "https://" + this.apiDomain;
      this.endpointPlayerStatus = this.httpEndpoint + "/player-status";
      this.endpointPlayerEndpoint = this.httpEndpoint + "/player-endpoint";

      try {
         this.endpointListen = new URI(this.wsEndpoint + "/listen");
      } catch (URISyntaxException var3) {
         throw new ExceptionInInitializerError(var3);
      }
   }

   public CPacketPlayerStatus checkPlayerStatus(String username) throws IOException {
      String uuid = UUIDUtil.getUUID(username);
      HttpGet request = new HttpGet(this.endpointPlayerStatus + "?uuid=" + uuid);
      HttpResponse response = this.client.execute(request);
      Gson gson = new Gson();
      String body = EntityUtils.toString(response.getEntity());
      return (CPacketPlayerStatus)gson.fromJson(body, CPacketPlayerStatus.class);
   }

   public String generateToken() throws AuthenticationException {
      MinecraftClient mc = MinecraftClient.getInstance();
      byte[] bytes = new byte[20];
      this.random.nextBytes(bytes);
      String token = (new BigInteger(bytes)).toString(16);
      mc.getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getAccessToken(), token);
      return MinecraftClient.getInstance().getSession().getUsername() + ":" + token;
   }

   public void listenMinerParty(int port) {
      if (this.wsClient != null) {
         this.wsClient.close();
      }

      try {
         this.wsClient = new MinerPartyClient.WSClient(port);
         this.wsClient.connect();
      } catch (AuthenticationException var3) {
         this.info("Cannot connect to MinerParty because of invalid session, try restarting your launcher?");
      }

      (new Thread(() -> {
         if (UPnP.isUPnPAvailable()) {
            this.LOGGER.info(UPnP.openPortTCP(port) ? "UPnP port opened" : "UPnP port unable to open");
         }

      })).start();
   }

   public SocketAddress getEndpoint(String username, int port) throws IOException, AuthenticationException {
      String uuid = UUIDUtil.getUUID(username);
      HttpGet request = new HttpGet(this.endpointPlayerEndpoint + "?port=" + port + "&uuid=" + uuid);
      request.setHeader("Authorization", "Bearer " + this.generateToken());
      HttpResponse response = this.client.execute(request);
      Gson gson = new Gson();
      String body = EntityUtils.toString(response.getEntity());
      CPacketPlayerEndpoint packet = (CPacketPlayerEndpoint)gson.fromJson(body, CPacketPlayerEndpoint.class);
      return new InetSocketAddress(packet.IP.split(":")[0], packet.Port);
   }

   public void closeMinerParty(int port) {
      (new Thread(() -> {
         UPnP.closePortTCP(port);
      })).start();
      if (this.wsClient != null) {
         this.wsClient.close();
      }

   }

   private ClientPlayerEntity player() {
      return MinecraftClient.getInstance().player;
   }

   private void info(String message) {
      if (this.player() != null) {
         MutableText text = (new LiteralText("[MINERPARTY]: ")).formatted(Formatting.WHITE);
         text = text.append(new LiteralText(message));
         this.player().sendMessage(text, false);
      }

   }

   private void error(String message) {
      if (this.player() != null) {
         MutableText text = (new LiteralText("[MINERPARTY ERROR]: ")).formatted(Formatting.RED);
         text = text.append(new LiteralText(message));
         this.player().sendMessage(text, false);
      }

   }

   private class WSClient extends WebSocketClient {
      private final int port;

      public WSClient(int port) throws AuthenticationException {
         super(MinerPartyClient.this.endpointListen);
         this.port = port;
         this.addHeader("Authorization", "Bearer " + MinerPartyClient.this.generateToken());
         this.addHeader("MC-Version", String.valueOf(SharedConstants.getGameVersion().getProtocolVersion()));
         this.addHeader("MC-Port", String.valueOf(port));
      }

      public void onOpen(ServerHandshake handshakedata) {
         MinerPartyClient.this.info("Connected to MinerParty");
      }

      public void onMessage(String message) {
         try {
            Gson gson = new Gson();
            CPacketIncomingConnection packet = (CPacketIncomingConnection)gson.fromJson(message.trim(), CPacketIncomingConnection.class);
            MinerPartyClient.this.LOGGER.info("Incoming Connection: " + packet.IP + " " + packet.Port);
            HolePunchUtil.holePunch(this.port, packet.IP, packet.Port);
            this.send("ok");
         } catch (Exception var4) {
            MinerPartyClient.this.error("Error parsing message: " + var4.getMessage());
         }

      }

      public void onClose(int code, String reason, boolean remote) {
         if (remote) {
            MinerPartyClient.this.error("Unexpected disconnected from MinerParty" + (reason.isEmpty() ? "" : ": " + reason));
         }

      }

      public void onError(Exception ex) {
         MinerPartyClient.this.error(ex.toString());
      }
   }
}
