package com.minerparty.client.util;

import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;

public class SkinUtil {
   public static final String DEFAULT_AVATAR;
   private static final Map<String, SkinUtil.SkinEntry> cache = new HashMap();

   public static String getAvatar(String username) {
      if (cache.containsKey(username) && ((SkinUtil.SkinEntry)cache.get(username)).expire > System.currentTimeMillis()) {
         return ((SkinUtil.SkinEntry)cache.get(username)).skin;
      } else {
         try {
            String uuid = UUIDUtil.getUUID(username);
            HttpGet request = new HttpGet("https://crafatar.com/avatars/" + uuid + "?size=64&overlay=true");
            InputStream is = UUIDUtil.client.execute(request).getEntity().getContent();

            String var5;
            try {
               String skin = Base64.getEncoder().encodeToString(IOUtils.toByteArray(is));
               cache.put(username, new SkinUtil.SkinEntry(skin, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5L)));
               var5 = skin;
            } catch (Throwable var7) {
               if (is != null) {
                  try {
                     is.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (is != null) {
               is.close();
            }

            return var5;
         } catch (Exception var8) {
            var8.printStackTrace();
            return null;
         }
      }
   }

   static {
      String avatar = null;

      try {
         InputStream is = SkinUtil.class.getResourceAsStream("/assets/fabricmod/alex.png");

         try {
            avatar = Base64.getEncoder().encodeToString(IOUtils.toByteArray(is));
         } catch (Throwable var5) {
            if (is != null) {
               try {
                  is.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (is != null) {
            is.close();
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      DEFAULT_AVATAR = avatar;
   }

   private static final class SkinEntry {
      final String skin;
      final long expire;

      private SkinEntry(String skin, long expire) {
         this.skin = skin;
         this.expire = expire;
      }

      // $FF: synthetic method
      SkinEntry(String x0, long x1, Object x2) {
         this(x0, x1);
      }
   }
}
