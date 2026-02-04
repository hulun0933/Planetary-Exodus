package com.planetaryexodus.api.events;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 文明进度事件
 * 当文明进度发生变化时触发
 */
public class CivilizationProgressEvent {
    
    private final int oldProgress;
    private final int newProgress;
    private final int progressChange;
    private final String source;
    private final ServerPlayerEntity contributor;
    private final long timestamp;
    
    /**
     * 创建文明进度事件
     * @param oldProgress 旧的进度值
     * @param newProgress 新的进度值
     * @param progressChange 进度变化量
     * @param source 进度来源
     * @param contributor 贡献的玩家（可为null）
     */
    public CivilizationProgressEvent(int oldProgress, int newProgress, int progressChange, 
                                    String source, ServerPlayerEntity contributor) {
        this.oldProgress = oldProgress;
        this.newProgress = newProgress;
        this.progressChange = progressChange;
        this.source = source;
        this.contributor = contributor;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 获取旧的进度值
     */
    public int getOldProgress() {
        return oldProgress;
    }
    
    /**
     * 获取新的进度值
     */
    public int getNewProgress() {
        return newProgress;
    }
    
    /**
     * 获取进度变化量
     */
    public int getProgressChange() {
        return progressChange;
    }
    
    /**
     * 获取进度来源
     */
    public String getSource() {
        return source;
    }
    
    /**
     * 获取贡献的玩家
     */
    public ServerPlayerEntity getContributor() {
        return contributor;
    }
    
    /**
     * 获取事件时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 判断是否为进度增加
     */
    public boolean isProgressIncrease() {
        return progressChange > 0;
    }
    
    /**
     * 判断是否为进度减少
     */
    public boolean isProgressDecrease() {
        return progressChange < 0;
    }
    
    /**
     * 判断进度是否达到特定值
     * @param threshold 阈值
     */
    public boolean reachedThreshold(int threshold) {
        return oldProgress < threshold && newProgress >= threshold;
    }
    
    /**
     * 获取贡献者名称（如果存在）
     */
    public String getContributorName() {
        return contributor != null ? contributor.getName().getString() : "未知";
    }
    
    /**
     * 获取事件描述
     */
    public String getDescription() {
        String changeSymbol = isProgressIncrease() ? "+" : "";
        String contributorInfo = contributor != null ? 
            " (贡献者: " + getContributorName() + ")" : "";
        
        return String.format("文明进度: %d%% → %d%% (%s%d%%) [来源: %s]%s",
            oldProgress, newProgress, changeSymbol, progressChange, source, contributorInfo);
    }
    
    @Override
    public String toString() {
        return "CivilizationProgressEvent{" +
               "oldProgress=" + oldProgress +
               ", newProgress=" + newProgress +
               ", progressChange=" + progressChange +
               ", source='" + source + '\'' +
               ", contributor=" + (contributor != null ? contributor.getName().getString() : "null") +
               ", timestamp=" + timestamp +
               '}';
    }
}