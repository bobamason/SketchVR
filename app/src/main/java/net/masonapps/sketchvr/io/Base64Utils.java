package net.masonapps.sketchvr.io;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by Bob Mason on 2/15/2018.
 */

public class Base64Utils {

    public static String encode(float[] array) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(array.length * Float.BYTES);
        final DataOutputStream dos = new DataOutputStream(bos);
        try {
            for (float value : array) dos.writeFloat(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(Base64.encode(bos.toByteArray(), Base64.DEFAULT));
    }

    public static String encode(short[] array) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(array.length * Short.BYTES);
        final DataOutputStream dos = new DataOutputStream(bos);
        try {
            for (short value : array) dos.writeShort(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(Base64.encode(bos.toByteArray(), Base64.DEFAULT));
    }

    public static void decodeFloatArray(String base64, float[] out) {
        final ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(base64, Base64.DEFAULT));
        final DataInputStream dis = new DataInputStream(bis);
        try {
            for (int i = 0; i < out.length; i++) out[i] = dis.readFloat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void decodeShortArray(String base64, short[] out) {
        final ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(base64, Base64.DEFAULT));
        final DataInputStream dis = new DataInputStream(bis);
        try {
            for (int i = 0; i < out.length; i++) out[i] = dis.readShort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
