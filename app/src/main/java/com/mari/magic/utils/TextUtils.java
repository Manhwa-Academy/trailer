package com.mari.magic.utils;

public class TextUtils {

    // ⭐ loại bỏ null từ API
    public static String clean(String s){

        if(s == null)
            return "";

        if(s.equalsIgnoreCase("null"))
            return "";

        return s.trim();
    }

    // ⭐ fallback title
    public static String bestTitle(String english, String romaji, String nativeTitle){

        english = clean(english);
        romaji = clean(romaji);
        nativeTitle = clean(nativeTitle);

        if(!english.isEmpty())
            return english;

        if(!romaji.isEmpty())
            return romaji;

        if(!nativeTitle.isEmpty())
            return nativeTitle;

        return "Unknown";
    }
}