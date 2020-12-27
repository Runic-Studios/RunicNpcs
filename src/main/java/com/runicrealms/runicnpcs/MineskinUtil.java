package com.runicrealms.runicnpcs;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.util.Scanner;

public class MineskinUtil {

    public static Skin getMineskinSkin(String id) {
        String url = "https://api.mineskin.org/get/id/" + id;
        try {
            Scanner scanner = new Scanner(new URL(url).openStream(), "UTF-8");
            Scanner withDelimiter = scanner.useDelimiter("\\A");
            JSONObject object = (JSONObject) JSONValue.parseWithException(scanner.next());
            JSONObject data = (JSONObject) object.get("data");
            JSONObject texture = (JSONObject) data.get("texture");
            String value = (String) texture.get("value");
            String signature = (String) texture.get("signature");
            scanner.close();
            withDelimiter.close();
            return new Skin(value, signature);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

}
