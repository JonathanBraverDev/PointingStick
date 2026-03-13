package com.example.pointingstick.utils;

import org.bukkit.Sound;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoundUtils {

    private static final List<String> APPROVED_SOUNDS = new ArrayList<>();

    static {
        APPROVED_SOUNDS.add("ENTITY_EXPERIENCE_ORB_PICKUP");
        APPROVED_SOUNDS.add("BLOCK_NOTE_BLOCK_CHIME");
        APPROVED_SOUNDS.add("BLOCK_NOTE_BLOCK_BELL");
        APPROVED_SOUNDS.add("BLOCK_NOTE_BLOCK_PLING");
        APPROVED_SOUNDS.add("BLOCK_NOTE_BLOCK_BIT");
        APPROVED_SOUNDS.add("BLOCK_NOTE_BLOCK_FLUTE");
        APPROVED_SOUNDS.add("BLOCK_NOTE_BLOCK_XYLOPHONE");
        APPROVED_SOUNDS.add("BLOCK_NOTE_BLOCK_HARP");
        APPROVED_SOUNDS.add("ENTITY_ARROW_HIT_PLAYER");
        APPROVED_SOUNDS.add("UI_BUTTON_CLICK");
        APPROVED_SOUNDS.add("BLOCK_AMETHYST_BLOCK_CHIME");
        APPROVED_SOUNDS.add("BLOCK_AMETHYST_BLOCK_BREAK");
        APPROVED_SOUNDS.add("BLOCK_AMETHYST_BLOCK_PLACE");
        APPROVED_SOUNDS.add("BLOCK_AMETHYST_BLOCK_STEP");
        APPROVED_SOUNDS.add("ENTITY_ITEM_PICKUP");
        
        Collections.sort(APPROVED_SOUNDS);
    }

    public static List<String> getApprovedSounds() {
        return APPROVED_SOUNDS;
    }

    public static boolean isApproved(String soundName) {
        if (soundName == null) return false;
        return APPROVED_SOUNDS.contains(soundName.toUpperCase());
    }

    public static Sound getSound(String soundName) {
        if (soundName == null || !isApproved(soundName)) {
            return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }
}
