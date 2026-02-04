package com.planetaryexodus.planet;

import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

/**
 * 行星状态枚举
 * 表示行星当前的环境状况，从稳定到崩溃边缘
 */
public enum PlanetStatus implements StringIdentifiable {
    
    /**
     * 稳定状态 - 环境相对正常
     */
    STABLE("stable", 0x00FF00, "🟢"),
    
    /**
     * 负荷上升状态 - 环境开始恶化
     */
    STRAINED("strained", 0xFFFF00, "🟡"),
    
    /**
     * 恶化状态 - 环境明显恶化，灾难频发
     */
    DEGRADED("degraded", 0xFFA500, "🟠"),
    
    /**
     * 崩溃边缘状态 - 环境濒临崩溃，生存困难
     */
    COLLAPSING("collapsing", 0xFF0000, "🔴");
    
    private final String id;
    private final int color;
    private final String emoji;
    
    PlanetStatus(String id, int color, String emoji) {
        this.id = id;
        this.color = color;
        this.emoji = emoji;
    }
    
    /**
     * 获取状态的显示名称（已本地化）
     */
    public Text getDisplayName() {
        return Text.translatable("planet.status." + id);
    }
    
    /**
     * 获取状态的颜色（RGB整数值）
     */
    public int getColor() {
        return color;
    }
    
    /**
     * 获取状态的emoji图标
     */
    public String getEmoji() {
        return emoji;
    }
    
    /**
     * 获取状态的本地化描述
     */
    public Text getDescription() {
        return Text.translatable("planet.status." + id + ".desc");
    }
    
    /**
     * 获取状态的严重程度（0-3，0最轻，3最重）
     */
    public int getSeverity() {
        return this.ordinal();
    }
    
    /**
     * 判断是否为恶劣状态（DEGRADED或COLLAPSING）
     */
    public boolean isSevere() {
        return this == DEGRADED || this == COLLAPSING;
    }
    
    /**
     * 判断是否为崩溃状态（COLLAPSING）
     */
    public boolean isCollapsing() {
        return this == COLLAPSING;
    }
    
    /**
     * 根据文明进度获取对应的行星状态
     * @param progress 文明进度百分比（0-100）
     * @return 对应的行星状态
     */
    public static PlanetStatus fromProgress(int progress) {
        if (progress < 25) return STABLE;
        if (progress < 50) return STRAINED;
        if (progress < 75) return DEGRADED;
        return COLLAPSING;
    }
    
    /**
     * 获取下一个更严重的状态
     * @return 下一个状态，如果是COLLAPSING则返回自身
     */
    public PlanetStatus getNextWorse() {
        if (this == COLLAPSING) return COLLAPSING;
        return values()[this.ordinal() + 1];
    }
    
    /**
     * 获取前一个更好的状态
     * @return 前一个状态，如果是STABLE则返回自身
     */
    public PlanetStatus getPreviousBetter() {
        if (this == STABLE) return STABLE;
        return values()[this.ordinal() - 1];
    }
    
    /**
     * 获取状态ID（用于序列化）
     */
    @Override
    public String asString() {
        return id;
    }
    
    /**
     * 根据ID获取状态
     * @param id 状态ID
     * @return 对应的状态，如果无效则返回STABLE
     */
    public static PlanetStatus fromId(String id) {
        for (PlanetStatus status : values()) {
            if (status.id.equals(id.toLowerCase(Locale.ROOT))) {
                return status;
            }
        }
        return STABLE;
    }
    
    /**
     * 获取格式化状态字符串（包含emoji和名称）
     */
    public String getFormattedString() {
        return emoji + " " + getDisplayName().getString();
    }
}