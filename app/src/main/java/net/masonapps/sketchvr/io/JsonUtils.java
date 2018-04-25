package net.masonapps.sketchvr.io;

import com.badlogic.gdx.math.Quaternion;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

/**
 * Created by Bob Mason on 1/15/2018.
 */

public class JsonUtils {

    public static JSONArray floatArrayToJsonArray(float[] floats) {
        return new JSONArray(Arrays.asList(floats));
    }

    public static float[] jsonArrayToFloatArray(JSONArray jsonArray) {
        final int length = jsonArray.length();
        final float[] floats = new float[length];
        for (int i = 0; i < length; i++) {
            try {
                floats[i] = Float.parseFloat(jsonArray.getString(i));
            } catch (NumberFormatException | JSONException e) {
                e.printStackTrace();
            }
        }
        return floats;
    }

    public static String quaternionToString(Quaternion q) {
        return "(" + q.x + "," + q.y + "," + q.z + "," + q.w + ")";
    }

    public static Quaternion quaternionFromString(String q) {
        int s0 = q.indexOf(',', 1);
        int s1 = q.indexOf(',', s0 + 1);
        int s2 = q.indexOf(',', s1 + 1);
        if (s0 != -1 && s1 != -1 && q.charAt(0) == '(' && q.charAt(q.length() - 1) == ')') {
            try {
                float x = Float.parseFloat(q.substring(1, s0));
                float y = Float.parseFloat(q.substring(s0 + 1, s1));
                float z = Float.parseFloat(q.substring(s1 + 1, s2));
                float w = Float.parseFloat(q.substring(s2 + 1, q.length() - 1));
                return new Quaternion(x, y, z, w);
            } catch (NumberFormatException ex) {
            }
        }
        throw new IllegalArgumentException("unable to parse quaternion from the input string");
    }
}
