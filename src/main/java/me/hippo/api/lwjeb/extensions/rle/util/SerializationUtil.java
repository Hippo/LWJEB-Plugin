package me.hippo.api.lwjeb.extensions.rle.util;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author Hippo
 * @version 1.0.0, 4/21/20
 * @since 1.0.0
 *
 * The <tt>Serialization Util</tt> helps with serialization. (duh)
 */
public enum SerializationUtil {
    ;

    /**
     * Serializes <tt>object</tt> into a "padded" json.
     * <p>
     *     The json padding would start off with the length of the class name,
     *     followed by a colon, then by the class nome itself, and finally
     *     the serialized json of the object.
     * </p>
     *
     * @param object  The object to serialize.
     * @param gson  The gson instance.
     * @return  The json string.
     */
    public static String serialize(Serializable object, Gson gson) {
        String name = object.getClass().getName();
        return name.length() + ":" + name + gson.toJson(object);
    }

    /**
     * Deserializes <tt>json</tt> following the json padding rule.
     *
     * @param json  The json.
     * @param gson  The gson instance.
     * @return  The deserialized object.
     * @throws ClassNotFoundException  If the deserialized object's class does not exist.
     */
    public static Serializable deserialize(String json, Gson gson) throws ClassNotFoundException {
        int length = Integer.parseInt(json.substring(0, json.indexOf(':')));
        int extra = String.valueOf(length).length() + 1;
        String className = json.substring(json.indexOf(':') + 1, length + extra);
        return (Serializable) gson.fromJson(json.substring(className.length() + extra), Class.forName(className));
    }
}
