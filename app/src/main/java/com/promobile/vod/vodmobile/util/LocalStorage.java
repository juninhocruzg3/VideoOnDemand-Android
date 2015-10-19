package com.promobile.vod.vodmobile.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Classe que gerencia a Persistência de dados por meio do SharedPreferences
 * @authors LOPES, R.L.; CRUZ JR, A.C.V.
 */
public class LocalStorage {
    public static final String LABEL = "storage_label";

    /**
     * Type: String
     */
    public static final String VIDEO_URL = "video_url";

    /**
     * Type: boolean
     */
    public static final String IS_FORMAT_SELECTED = "is_format_selected";

    /**
     * Type: int
     */
    public static final String FORMAT_SELECTED = "format_selected";

    /**
     * Type: {@link com.promobile.vod.vodmobile.model.Video}
     */
    public static final String OBJ_VIDEO = "obj_video";

    /**
     * Type: {@link com.promobile.vod.vodmobile.model.Channel}
     */
    public static final String OBJ_CHANNEL = "obj_channel";

    private static final String SETTINGS = "settings";


    public static LocalStorage localStorage;
    private SharedPreferences settings;
    private Editor editor;

    public LocalStorage(Context context) {
        settings = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    public static LocalStorage getInstance(Context context) {
        if (localStorage == null)
            localStorage = new LocalStorage(context);
        return localStorage;
    }

    public void addToStorage(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void addToStorage(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * Persiste um valor <code>boolean</code> sob uma chave pré-definida.
     *
     * @param key   Chave pré-definida.
     * @param value Valor
     */
    public void addToStorage(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * Persiste um valor <code>double</code> sob uma chave pré-definida.
     *
     * @param key   Chave pré-definida.
     * @param value Valor
     */
    public void addToStorage(String key, double value) {
        editor.putString(key, "" + value);
        editor.commit();
    }

    /**
     * Persiste um valor <code>Object</code> sob uma chave pré-definida.
     *
     * @param key   Chave pré-definida.
     * @param value Valor
     */
    public <T> void addToStorage(String key, T value) {
        Gson gson = new Gson();
        String json = gson.toJson(value);
        editor.putString(key, json);
        editor.commit();
    }

    /**
     * Retorna um Objeto que foi armazenado utilizando uma chave especícia <br/>
     *
     * @param key Chave pré-definida.
     * @return Dado persistido. Caso não haja, retorna <code>null</code>
     */
    public <T> T getObjectFromStorage(String key, Class<T> clazz) {
        String json = settings.getString(key, null);
        Gson gson = new Gson();

        T object = gson.fromJson(json, clazz);

        return object;
    }

    /**
     * Retorna uma string que foi armazenada utilizando uma chave especícia <br/>
     *
     * @param key Chave pré-definida.
     * @return Dado persistido. Caso não haja, retorna <code>null</code>
     */
    public String getStringFromStorage(String key) {
        return settings.getString(key, null);
    }

    /**
     * Retorna um inteiro que foi armazenado utilizando uma chave especícia <br/>
     *
     * @param key Chave pré-definida
     * @return Dado persistido
     */
    public int getIntFromStorage(String key) {
        return settings.getInt(key, 0);
    }

    /**
     * Retorna um valor booleano foi armazenado utilizando uma chave especícia <br/>
     *
     * @param key Chave pré-definida.
     * @return Dado persistido
     */
    public boolean getBooleanFromStorage(String key) {
        return settings.getBoolean(key, false);
    }

    /**
     * Retorna um valor double foi armazenado utilizando uma chave especícia <br/>
     *
     * @param key Chave pré-definida.
     * @return Dado persistido
     */
    public double getDoubleFromStorage(String key) {
        String str = settings.getString(key, null);
        if (str != null)
            try {
                return Double.parseDouble(str);
            } catch (Exception e) {
                //Falha na conversão String para Double
                Log.e("LocalStorage", "Falha na conversão de String para Double em LocalStorage.getDoubleFromStorage");
                return 0.0;
            }
        else
            return 0.0;
    }
}