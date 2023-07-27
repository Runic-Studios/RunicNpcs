package com.runicrealms.plugin.npcs;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.util.Scanner;

public class MineskinUtil {

    /*
    API Key - cb438e3fc30bd9732f56b7d31d2f68d8eced4369bcc0468a14826391c65b87aa
    API Key Secret - 36774173e35d9028a354d7678dcdf1fee4e1414548b1116e9084f59543ef68f130ebbc43ff0e7650c55cf8e9ad16e6f51334535375611aa5716729446b1fe8b9
     */

    public static Skin getMineskinSkin(String id) {
        String url = "https://api.mineskin.org/get/uuid/" + id;
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
