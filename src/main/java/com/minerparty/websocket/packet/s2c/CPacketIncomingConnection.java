package com.minerparty.websocket.packet.s2c;

import com.minerparty.websocket.packet.Packet;

public class CPacketIncomingConnection implements Packet {
   public String IP;
   public String UUID;
   public int Port;
}
