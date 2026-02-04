package com.planetaryexodus.disaster;

import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

/**
 * 灾难类型枚举
 * 表示可能发生的各种灾难
 */
public enum DisasterType implements StringIdentifiable {
    
    /**
     * 辐射灾难 - 高能辐射泄漏
     */
    RADIATION("radiation", 0x00FF00, "☢️", 0.2f),
    
    /**
     * 酸雨灾难 - 腐蚀性降雨
     */
    ACID_RAIN("acid_rain", 0x80FF00, "🌧️", 0.1f),
    
    /**
     * 地震灾难 - 地壳震动
     */
    EARTHQUAKE("earthquake", 0xFF8000, "🌋", 0.05f),
    
    /**
     * 超级风暴 - 极端天气
     */
    SUPER_STORM("super_storm", 0x0080FF, "🌀", 0.08f),
    
    /**
     * 磁暴灾难 - 电磁干扰
     */
    MAGNETIC_STORM("magnetic_storm", 0xFF00FF, "⚡", 0.03f),
    
    /**
     * 热浪灾难 - 极端高温
     */
    HEAT_WAVE("heat_wave", 0xFF0000, "🔥", 0.15f);
    
    private final String id;
    private final int color;
    private final String emoji;
    private final float baseDamage;
    
    DisasterType(String id, int color, String emoji, float baseDamage) {
        this.id = id;
        this.color = color;
        this.emoji = emoji;
        this.baseDamage = baseDamage;
    }
    
    /**
     * 获取灾难的显示名称（已本地化）
     */
    public Text getDisplayName() {
        return Text.translatable("disaster.type." + id);
    }
    
    /**
     * 获取灾难的颜色（RGB整数值）
     */
    public int getColor() {
        return color;
    }
    
    /**
     * 获取灾难的emoji图标
     */
    public String getEmoji() {
        return emoji;
    }
    
    /**
     * 获取基础伤害值
     */
    public float getBaseDamage() {
        return baseDamage;
    }
    
    /**
     * 获取灾难的本地化描述
     */
    public Text getDescription() {
        return Text.translatable("disaster.type." + id + ".desc");
    }
    
    /**
     * 获取灾难的严重程度等级（1-10）
     */
    public int getSeverityLevel() {
        return (int) (baseDamage * 20); // 将基础伤害映射到1-10的等级
    }
    
    /**
     * 判断是否为环境灾难（影响世界）
     */
    public boolean isEnvironmental() {
        return this == ACID_RAIN || this == SUPER_STORM || this == HEAT_WAVE;
    }
    
    /**
     * 判断是否为地质灾难（影响地形）
     */
    public boolean isGeological() {
        return this == EARTHQUAKE;
    }
    
    /**
     * 判断是否为辐射灾难（影响生物）
     */
    public boolean isRadiological() {
        return this == RADIATION || this == MAGNETIC_STORM;
    }
    
    /**
     * 获取灾难ID（用于序列化）
     */
    @Override
    public String asString() {
        return id;
    }
    
    /**
     * 根据ID获取灾难类型
     * @param id 灾难ID
     * @return 对应的灾难类型，如果无效则返回RADIATION
     */
    public static DisasterType fromId(String id) {
        for (DisasterType type : values()) {
            if (type.id.equals(id.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }
        return RADIATION;
    }
    
    /**
     * 获取格式化灾难字符串（包含emoji和名称）
     */
    public String getFormattedString() {
        return emoji + " " + getDisplayName().getString();
    }
    
    /**
     * 获取应对措施描述
     */
    public Text getCountermeasure() {
        return Text.translatable("disaster.type." + id + ".countermeasure");
    }
    
    /**
     * 获取预警时间（秒）
     */
    public int getWarningTime() {
        switch (this) {
            case EARTHQUAKE: return 10;      // 地震预警时间短
            case SUPER_STORM: return 300;    // 风暴预警时间长
            case HEAT_WAVE: return 600;      // 热浪预警时间长
            case ACID_RAIN: return 180;      // 酸雨中等预警
            case RADIATION: return 60;       // 辐射预警中等
            case MAGNETIC_STORM: return 120; // 磁暴预警中等
            default: return 60;
        }
    }
    
    /**
     * 获取持续时间（秒）
     */
    public int getDuration() {
        switch (this) {
            case EARTHQUAKE: return 30;      // 地震持续时间短
            case SUPER_STORM: return 600;    // 风暴持续时间长
            case HEAT_WAVE: return 1200;     // 热浪持续时间很长
            case ACID_RAIN: return 300;      // 酸雨中持续时间
            case RADIATION: return 180;      // 辐射中等持续时间
            case MAGNETIC_STORM: return 240; // 磁暴中等持续时间
            default: return 300;
        }
    }
}