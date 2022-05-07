package com.minerparty.client.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class UUIDUtil {
   static final HttpClient client = HttpClientBuilder.create().build();
   private static final JsonParser parser = new JsonParser();

   public static String getUUID(String username) throws IOException {
      HttpGet request = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + username);
      HttpResponse response = client.execute(request);
      JsonObject json = parser.parse(EntityUtils.toString(response.getEntity())).getAsJsonObject();
      return json.getAsJsonPrimitive("id").getAsString();
   }
}
