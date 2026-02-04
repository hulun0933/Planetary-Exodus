package com.planetaryexodus.player;

import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

/**
 * 玩家职业枚举
 * 表示玩家在文明迁移中可以担任的角色
 */
public enum PlayerRole implements StringIdentifiable {
    
    /**
     * 平民 - 基础职业，适应性最强
     */
    CIVILIAN("civilian", "👤", 0xAAAAAA),
    
    /**
     * 工业工程师 - 负责资源和能源生产
     */
    INDUSTRIAL_ENGINEER("industrial_engineer", "🏭", 0xFF6B35),
    
    /**
     * 航天工程师 - 负责火箭和太空工程
     */
    AEROSPACE_ENGINEER("aerospace_engineer", "🚀", 0x4A90E2),
    
    /**
     * 科学家 - 负责研究和灾难预警
     */
    SCIENTIST("scientist", "🔬", 0x7B68EE),
    
    /**
     * 后勤协调员 - 负责物资管理和物流
     */
    LOGISTICS_COORDINATOR("logistics_coordinator", "📦", 0x32CD32);
    
    private final String id;
    private final String emoji;
    private final int color;
    
    PlayerRole(String id, String emoji, int color) {
        this.id = id;
        this.emoji = emoji;
        this.color = color;
    }
    
    /**
     * 获取职业的显示名称（已本地化）
     */
    public Text getDisplayName() {
        return Text.translatable("player.role." + id);
    }
    
    /**
     * 获取职业的emoji图标
     */
    public String getEmoji() {
        return emoji;
    }
    
    /**
     * 获取职业的颜色（RGB整数值）
     */
    public int getColor() {
        return color;
    }
    
    /**
     * 获取职业的本地化描述
     */
    public Text getDescription() {
        return Text.translatable("player.role." + id + ".desc");
    }
    
    /**
     * 获取职业的职责说明
     */
    public Text getResponsibilities() {
        return Text.translatable("player.role." + id + ".responsibilities");
    }
    
    /**
     * 获取职业ID（用于序列化）
     */
    @Override
    public String asString() {
        return id;
    }
    
    /**
     * 根据ID获取职业
     * @param id 职业ID
     * @return 对应的职业，如果无效则返回CIVILIAN
     */
    public static PlayerRole fromId(String id) {
        for (PlayerRole role : values()) {
            if (role.id.equals(id.toLowerCase(Locale.ROOT))) {
                return role;
            }
        }
        return CIVILIAN;
    }
    
    /**
     * 获取格式化职业字符串（包含emoji和名称）
     */
    public String getFormattedString() {
        return emoji + " " + getDisplayName().getString();
    }
    
    /**
     * 判断是否为专业职业（非平民）
     */
    public boolean isSpecialized() {
        return this != CIVILIAN;
    }
    
    /**
     * 获取职业的工作效率加成
     */
    public double getEfficiencyBonus() {
        switch (this) {
            case INDUSTRIAL_ENGINEER:
                return 0.3; // 工业效率+30%
            case AEROSPACE_ENGINEER:
                return 0.25; // 航天效率+25%
            case SCIENTIST:
                return 0.2; // 研究效率+20%
            case LOGISTICS_COORDINATOR:
                return 0.35; // 物流效率+35%
            default:
                return 0.1; // 平民效率+10%
        }
    }
    
    /**
     * 获取职业的特殊能力描述
     */
    public Text getSpecialAbility() {
        return Text.translatable("player.role." + id + ".ability");
    }
    
    /**
     * 获取职业的推荐工作领域
     */
    public String[] getRecommendedTasks() {
        switch (this) {
            case INDUSTRIAL_ENGINEER:
                return new String[]{"mining", "smelting", "automation", "power_generation"};
            case AEROSPACE_ENGINEER:
                return new String[]{"rocket_building", "fuel_production", "navigation", "space_construction"};
            case SCIENTIST:
                return new String[]{"research", "experimentation", "disaster_prediction", "technology_development"};
            case LOGISTICS_COORDINATOR:
                return new String[]{"inventory_management", "supply_chains", "transportation", "resource_allocation"};
            default:
                return new String[]{"general_work", "support", "basic_tasks"};
        }
    }
    
    /**
     * 判断职业是否与特定任务类型匹配
     */
    public boolean matchesTaskType(String taskType) {
        for (String recommended : getRecommendedTasks()) {
            if (recommended.equals(taskType)) {
                return true;
            }
        }
        return false;
    }
}