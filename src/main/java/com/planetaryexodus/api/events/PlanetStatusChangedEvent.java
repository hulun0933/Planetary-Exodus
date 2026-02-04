package com.planetaryexodus.api.events;

import com.planetaryexodus.planet.PlanetStatus;

/**
 * 行星状态改变事件
 * 当行星状态发生变化时触发
 */
public class PlanetStatusChangedEvent {
    
    private final PlanetStatus oldStatus;
    private final PlanetStatus newStatus;
    private final int progress;
    private final long timestamp;
    
    /**
     * 创建行星状态改变事件
     * @param oldStatus 旧的行星状态
     * @param newStatus 新的行星状态
     * @param progress 当前的文明进度
     */
    public PlanetStatusChangedEvent(PlanetStatus oldStatus, PlanetStatus newStatus, int progress) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.progress = progress;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 获取旧的行星状态
     */
    public PlanetStatus getOldStatus() {
        return oldStatus;
    }
    
    /**
     * 获取新的行星状态
     */
    public PlanetStatus getNewStatus() {
        return newStatus;
    }
    
    /**
     * 获取当前的文明进度
     */
    public int getProgress() {
        return progress;
    }
    
    /**
     * 获取事件时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 判断状态是否变差
     */
    public boolean isWorsening() {
        return newStatus.getSeverity() > oldStatus.getSeverity();
    }
    
    /**
     * 判断状态是否改善
     */
    public boolean isImproving() {
        return newStatus.getSeverity() < oldStatus.getSeverity();
    }
    
    /**
     * 判断状态是否保持不变
     */
    public boolean isUnchanged() {
        return newStatus == oldStatus;
    }
    
    /**
     * 获取状态变化的严重程度差异
     * @return 正数表示变差，负数表示改善，零表示不变
     */
    public int getSeverityChange() {
        return newStatus.getSeverity() - oldStatus.getSeverity();
    }
    
    /**
     * 获取事件描述
     */
    public String getDescription() {
        if (isUnchanged()) {
            return "行星状态保持 " + newStatus.getDisplayName().getString();
        } else if (isWorsening()) {
            return "⚠️ 行星状态恶化: " + oldStatus.getDisplayName().getString() + 
                   " → " + newStatus.getDisplayName().getString();
        } else {
            return "✅ 行星状态改善: " + oldStatus.getDisplayName().getString() + 
                   " → " + newStatus.getDisplayName().getString();
        }
    }
    
    @Override
    public String toString() {
        return "PlanetStatusChangedEvent{" +
               "oldStatus=" + oldStatus +
               ", newStatus=" + newStatus +
               ", progress=" + progress +
               ", timestamp=" + timestamp +
               ", worsening=" + isWorsening() +
               '}';
    }
}