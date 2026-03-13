package com.example.pointingstick.utils;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ColorUtils {

    public static class PingColor {
        public final TextColor textColor;
        public final Color particleColor;

        public PingColor(TextColor textColor, Color particleColor) {
            this.textColor = textColor;
            this.particleColor = particleColor;
        }
    }

    private static final Map<String, PingColor> WOOL_COLORS = new HashMap<>();

    static {
        WOOL_COLORS.put("WHITE",      create("#FFFFFF"));
        WOOL_COLORS.put("ORANGE",     create("#F9801D"));
        WOOL_COLORS.put("MAGENTA",    create("#C74EBD"));
        WOOL_COLORS.put("LIGHT_BLUE", create("#3AB3DA"));
        WOOL_COLORS.put("YELLOW",     create("#FED83D"));
        WOOL_COLORS.put("LIME",       create("#80C71F"));
        WOOL_COLORS.put("PINK",       create("#F38BAA"));
        WOOL_COLORS.put("GRAY",       create("#474F52"));
        WOOL_COLORS.put("LIGHT_GRAY", create("#9D9D97"));
        WOOL_COLORS.put("CYAN",       create("#169C9C"));
        WOOL_COLORS.put("PURPLE",     create("#8932B8"));
        WOOL_COLORS.put("BLUE",       create("#3C44AA"));
        WOOL_COLORS.put("BROWN",      create("#835432"));
        WOOL_COLORS.put("GREEN",      create("#5E7C16"));
        WOOL_COLORS.put("RED",        create("#B02E26"));
        WOOL_COLORS.put("BLACK",      create("#1D1D21"));
    }

    private static PingColor create(String hex) {
        TextColor textColor = TextColor.fromHexString(hex);
        int r = Integer.valueOf(hex.substring(1, 3), 16);
        int g = Integer.valueOf(hex.substring(3, 5), 16);
        int b = Integer.valueOf(hex.substring(5, 7), 16);
        return new PingColor(textColor, Color.fromRGB(r, g, b));
    }

    public static PingColor getPingColor(String name) {
        if (name == null) return WOOL_COLORS.get("YELLOW");
        return WOOL_COLORS.getOrDefault(name.toUpperCase(), WOOL_COLORS.get("YELLOW"));
    }

    public static Set<String> getColorNames() {
        return WOOL_COLORS.keySet();
    }
}
