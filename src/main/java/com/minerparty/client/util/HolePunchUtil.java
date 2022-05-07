package com.minerparty.client.util;

import com.minerparty.client.util.linux.SocketSetOpt;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.SystemUtils;

public class HolePunchUtil {
   private static final ExecutorService SERVICE = Executors.newFixedThreadPool(5);

   public static void holePunch(int fromPort, String toIP, int toPort) throws SocketException, InterruptedException {
      Set<Integer> targetPorts = new HashSet();

      for(int i = 0; i < 5; ++i) {
         targetPorts.add(toPort + i);
         targetPorts.add(1024 + i);
      }

      Stream<Callable<Boolean>> var10000 = Collections.list(NetworkInterface.getNetworkInterfaces()).stream().flatMap((x) -> {
         return Collections.list(x.getInetAddresses()).stream();
      }).filter((x) -> {
         return !x.isLoopbackAddress();
      }).map((x) -> {
         return new InetSocketAddress(x, fromPort);
      }).map((addr) -> {
         return () -> {
            return holePunchFromTo(addr, new InetSocketAddress(toIP.split(":")[0], toPort));
         };
      });
      ExecutorService var10001 = SERVICE;
      Objects.requireNonNull(var10001);
      Stream<Future<Boolean>> futures = var10000.map(var10001::submit);
      Iterator var5 = ((List)futures.collect(Collectors.toList())).iterator();

      while(var5.hasNext()) {
         Future future = (Future)var5.next();

         try {
            future.get();
         } catch (ExecutionException var8) {
            var8.printStackTrace();
         }
      }

   }

   private static boolean holePunchFromTo(InetSocketAddress from, InetSocketAddress to) {
      try {
         Socket socket = new Socket();

         boolean var3;
         try {
            socket.setSoTimeout(1500);
            setSocketReusePort(socket);
            socket.setReuseAddress(true);
            socket.bind(from);
            socket.connect(to, 1500);
            var3 = true;
         } catch (Throwable var6) {
            try {
               socket.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         socket.close();
         return var3;
      } catch (SocketTimeoutException var7) {
         return true;
      } catch (Exception var8) {
         var8.printStackTrace();
         return false;
      }
   }

   private static void setSocketReusePort(Socket socket) {
      if (SystemUtils.IS_OS_LINUX) {
         try {
            SocketSetOpt.setSockOpt(socket, 1, 15, 1);
         } catch (IOException var2) {
            var2.printStackTrace();
         }
      }

   }
}
