package com.fakecalllog.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScheduledCallStore {

    private static final String PREF_NAME = "scheduled_calls";
    private static final String KEY_IDS   = "ids";

    public static void save(Context ctx, int id, String name, String number,
                            int callType, long triggerTime, int duration) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("entry_" + id + "_name", name);
        editor.putString("entry_" + id + "_number", number);
        editor.putInt("entry_" + id + "_type", callType);
        editor.putLong("entry_" + id + "_time", triggerTime);
        editor.putInt("entry_" + id + "_duration", duration);

        // Add to id list
        List<Integer> ids = getAllIds(ctx);
        ids.add(id);
        editor.putString(KEY_IDS, idsToJson(ids));
        editor.apply();
    }

    public static void delete(Context ctx, int id) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("entry_" + id + "_name");
        editor.remove("entry_" + id + "_number");
        editor.remove("entry_" + id + "_type");
        editor.remove("entry_" + id + "_time");
        editor.remove("entry_" + id + "_duration");
        List<Integer> ids = getAllIds(ctx);
        ids.remove(Integer.valueOf(id));
        editor.putString(KEY_IDS, idsToJson(ids));
        editor.apply();
    }

    public static List<ScheduledEntry> getAll(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        List<Integer> ids = getAllIds(ctx);
        List<ScheduledEntry> entries = new ArrayList<>();
        for (int id : ids) {
            ScheduledEntry e = new ScheduledEntry();
            e.id       = id;
            e.name     = prefs.getString("entry_" + id + "_name", "");
            e.number   = prefs.getString("entry_" + id + "_number", "");
            e.callType = prefs.getInt("entry_" + id + "_type", 1);
            e.triggerTime = prefs.getLong("entry_" + id + "_time", 0);
            e.duration = prefs.getInt("entry_" + id + "_duration", 0);
            entries.add(e);
        }
        return entries;
    }

    private static List<Integer> getAllIds(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_IDS, "[]");
        List<Integer> ids = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) ids.add(arr.getInt(i));
        } catch (Exception ignored) {}
        return ids;
    }

    private static String idsToJson(List<Integer> ids) {
        JSONArray arr = new JSONArray();
        for (int id : ids) arr.put(id);
        return arr.toString();
    }

    public static class ScheduledEntry {
        public int id;
        public String name;
        public String number;
        public int callType;
        public long triggerTime;
        public int duration;
    }
}
