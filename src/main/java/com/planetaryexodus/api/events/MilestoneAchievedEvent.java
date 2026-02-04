package com.planetaryexodus.api.events;

import com.planetaryexodus.civilization.CivilizationManager;
import net.minecraft.text.Text;

/**
 * 里程碑达成事件
 * 当文明达到里程碑时发布
 */
public class MilestoneAchievedEvent {
    
    private final CivilizationManager.Milestone milestone;
    private final int currentProgress;
    
    public MilestoneAchievedEvent(CivilizationManager.Milestone milestone, int currentProgress) {
        this.milestone = milestone;
        this.currentProgress = currentProgress;
    }
    
    /**
     * 获取达成的里程碑
     */
    public CivilizationManager.Milestone getMilestone() {
        return milestone;
    }
    
    /**
     * 获取当前文明进度
     */
    public int getCurrentProgress() {
        return currentProgress;
    }
    
    /**
     * 获取里程碑名称
     */
    public String getMilestoneName() {
        return milestone.getDisplayName().getString();
    }
    
    /**
     * 获取进度奖励
     */
    public int getProgressReward() {
        return milestone.progressReward;
    }
    
    /**
     * 获取事件描述
     */
    public String getDescription() {
        return String.format("里程碑达成：%s，当前文明进度：%d%%", 
            getMilestoneName(), currentProgress);
    }
    
    @Override
    public String toString() {
        return String.format("MilestoneAchievedEvent{milestone=%s, progress=%d}", 
            milestone.name, currentProgress);
    }
}
