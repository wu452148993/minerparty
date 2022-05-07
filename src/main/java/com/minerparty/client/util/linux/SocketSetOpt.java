package com.minerparty.client.util.linux;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketImpl;
import org.apache.commons.lang3.SystemUtils;

public class SocketSetOpt {
   public static final int SOL_SOCKET = 1;
   public static final int SO_REUSEPORT = 15;
   private static Field socketFd;
   private static Field fdField;
   private static Method getImpl;

   public static int getFd(Socket s) {
      try {
         SocketImpl impl = (SocketImpl)getImpl.invoke(s);
         FileDescriptor fd = (FileDescriptor)socketFd.get(impl);
         return (Integer)fdField.get(fd);
      } catch (Exception var3) {
         var3.printStackTrace();
         return -1;
      }
   }

   public static void setSockOpt(Socket socket, int level, int option_name, int option_value) throws IOException {
      if (socket == null) {
         throw new IOException("Null socket");
      } else {
         int fd = getFd(socket);
         if (fd == -1) {
            throw new IOException("Bad socket FD");
         } else {
            IntByReference val = new IntByReference(option_value);

            try {
               setsockopt(fd, level, option_name, val.getPointer(), 4);
            } catch (LastErrorException var7) {
               throw new IOException("setsockopt: " + strerror(var7.getErrorCode()));
            }
         }
      }
   }

   private static native int setsockopt(int var0, int var1, int var2, Pointer var3, int var4) throws LastErrorException;

   public static native String strerror(int var0);

   static {
      if (!SystemUtils.IS_OS_LINUX) {
         throw new ExceptionInInitializerError("Not Linux");
      } else {
         Native.register("c");

         try {
            socketFd = SocketImpl.class.getDeclaredField("fd");
            socketFd.setAccessible(true);
            getImpl = Socket.class.getDeclaredMethod("getImpl");
            getImpl.setAccessible(true);
            fdField = FileDescriptor.class.getDeclaredField("fd");
            fdField.setAccessible(true);
         } catch (Exception var1) {
            socketFd = null;
         }

      }
   }
}
