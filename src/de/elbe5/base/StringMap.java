package de.elbe5.base;

import java.util.HashMap;

public class StringMap extends HashMap<String, String> {

    public String getString(String key, String def){
        String result = super.get(key);
        return result==null ? def : result;
    }

    public String getString(String key){
        return getString(key, "");
    }

    public int getInt(String key, int def){
        try{
            return Integer.parseInt(super.get(key));
        }
        catch (Exception e){
            return def;
        }
    }
    public int getInt(String key){
        return getInt(key, 0);
    }

    public long getLong(String key, long def){
        try{
            return Long.parseLong(super.get(key));
        }
        catch (Exception e){
            return def;
        }
    }

    public long getLong(String key){
        return getLong(key, 0);
    }

    public boolean getBoolean(String key, boolean def){
        try{
            return Boolean.parseBoolean(super.get(key));
        }
        catch (Exception e){
            return def;
        }
    }

    public boolean getBoolean(String key){
        return getBoolean(key,false);
    }

    public double getDouble(String key, double def){
        try{
            return Double.parseDouble(super.get(key));
        }
        catch (Exception e){
            return def;
        }
    }

    public double getDouble(String key){
        return getDouble(key, 0);
    }

    public void put(String key, int value){
        put(key, Integer.toString(value));
    }

    public void put(String key, long value){
        put(key, Long.toString(value));
    }

    public void put(String key, boolean value){
        put(key, Boolean.toString(value));
    }

    public void put(String key, double value){
        put(key, Double.toString(value));
    }

}
