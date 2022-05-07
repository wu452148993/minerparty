package com.minerparty.client.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class AddrUtil {
   private static final byte[] SPECIAL_ADDR = new byte[]{-16, -22, 124, -25};

   public static InetAddress createFakeAddr(String username) {
      try {
         return InetAddress.getByAddress(username, SPECIAL_ADDR);
      } catch (UnknownHostException var2) {
         throw new RuntimeException("How did this even happen", var2);
      }
   }

   public static String isFakeAddr(InetAddress addr) {
      return Arrays.equals(addr.getAddress(), SPECIAL_ADDR) ? addr.getHostName() : null;
   }
}
