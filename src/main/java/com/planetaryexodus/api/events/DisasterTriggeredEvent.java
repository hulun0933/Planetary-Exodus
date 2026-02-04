package com.planetaryexodus.api.events;

import com.planetaryexodus.disaster.DisasterType;

/**
 * 灾难触发事件
 * 当灾难被触发时发布
 */
public class DisasterTriggeredEvent {
    
    private final DisasterType disasterType;
    private final int durationMinutes;
    
    public DisasterTriggeredEvent(DisasterType disasterType, int durationMinutes) {
        this.disasterType = disasterType;
        this.durationMinutes = durationMinutes;
    }
    
    /**
     * 获取灾难类型
     */
    public DisasterType getDisasterType() {
        return disasterType;
    }
    
    /**
     * 获取灾难持续时间（分钟）
     */
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    /**
     * 获取灾难显示名称
     */
    public String getDisplayName() {
        return disasterType.getDisplayName().getString();
    }
    
    /**
     * 获取灾难描述
     */
    public String getDescription() {
        return String.format("灾难：%s，持续时间：%d分钟", 
            getDisplayName(), durationMinutes);
    }
    
    @Override
    public String toString() {
        return String.format("DisasterTriggeredEvent{type=%s, duration=%dmin}", 
            disasterType, durationMinutes);
    }
}