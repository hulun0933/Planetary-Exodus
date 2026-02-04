package com.planetaryexodus.planet;

import com.planetaryexodus.PlanetaryExodusMod;
import com.planetaryexodus.api.events.PlanetStatusChangedEvent;
import com.planetaryexodus.core.ModConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 行星状态管理器
 * 负责管理行星状态的变化、效果应用和状态监控
 */
public class PlanetStatusManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PlanetaryExodus/Planet");
    private static final Identifier DATA_ID = new Identifier("planetaryexodus", "planet_data");
    
    // 行星状态数据
    private PlanetStatus currentStatus = PlanetStatus.STABLE;
    private PlanetStatus previousStatus = PlanetStatus.STABLE;
    private int daysInCurrentStatus = 0;
    private long statusChangeTime = System.currentTimeMillis();
    private long lastUpdateTime = System.currentTimeMillis();
    
    // 配置
    private ModConfig.PlanetConfig config;
    
    // 状态监控
    private ScheduledExecutorService monitoringService;
    private MinecraftServer currentServer;
    private boolean isMonitoring = false;
    
    // 效果应用器
    private PlanetEffects effectsApplier;
    
    public PlanetStatusManager() {
        reloadConfig();
        this.effectsApplier = new PlanetEffects();
        LOGGER.info("行星状态管理器初始化完成，初始状态: {}", currentStatus.getFormattedString());
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        this.config = PlanetaryExodusMod.getInstance().getConfig().getPlanet();
        LOGGER.info("行星配置已重新加载，阈值: 稳定({}%), 负荷({}%), 恶化({}%)",
            config.stableThreshold, config.strainedThreshold, config.degradedThreshold);
    }
    
    /**
     * 根据文明进度更新行星状态
     * @param server 服务器实例
     * @param civilizationProgress 文明进度
     */
    public void update(MinecraftServer server, int civilizationProgress) {
        long currentTime = System.currentTimeMillis();
        
        // 每秒检查一次状态变化
        if (currentTime - lastUpdateTime < 1000) {
            return;
        }
        
        lastUpdateTime = currentTime;
        this.currentServer = server;
        
        // 根据配置阈值计算新状态
        PlanetStatus newStatus = calculateStatus(civilizationProgress);
        
        // 检查状态是否变化
        if (newStatus != currentStatus) {
            changeStatus(newStatus, civilizationProgress);
        }
        
        // 更新在当前状态的天数
        updateDaysInCurrentStatus();
        
        // 应用当前状态的效果
        applyStatusEffects(server);
    }
    
    /**
     * 计算行星状态
     */
    private PlanetStatus calculateStatus(int progress) {
        if (progress < config.stableThreshold) {
            return PlanetStatus.STABLE;
        } else if (progress < config.strainedThreshold) {
            return PlanetStatus.STRAINED;
        } else if (progress < config.degradedThreshold) {
            return PlanetStatus.DEGRADED;
        } else {
            return PlanetStatus.COLLAPSING;
        }
    }
    
    /**
     * 改变行星状态
     */
    private void changeStatus(PlanetStatus newStatus, int progress) {
        previousStatus = currentStatus;
        currentStatus = newStatus;
        statusChangeTime = System.currentTimeMillis();
        daysInCurrentStatus = 0;
        
        // 发布状态改变事件
        PlanetaryExodusMod.getInstance().getEventBus().publish(
            new PlanetStatusChangedEvent(previousStatus, currentStatus, progress)
        );
        
        // 通知所有玩家
        if (currentServer != null) {
            Text message = Text.translatable("planet.status.changed", 
                previousStatus.getDisplayName(),
                currentStatus.getDisplayName());
            currentServer.getPlayerManager().broadcast(message, false);
        }
        
        LOGGER.info("🌍 行星状态变化: {} → {} (进度: {}%)",
            previousStatus.getFormattedString(),
            currentStatus.getFormattedString(),
            progress);
        
        // 记录状态变化日志
        logStatusChange(previousStatus, currentStatus, progress);
    }
    
    /**
     * 更新在当前状态的天数
     */
    private void updateDaysInCurrentStatus() {
        long elapsed = System.currentTimeMillis() - statusChangeTime;
        daysInCurrentStatus = (int) (elapsed / (1000 * 60 * 60 * 24)); // 转换为天数
    }
    
    /**
     * 应用状态效果
     */
    private void applyStatusEffects(MinecraftServer server) {
        if (server == null) return;
        
        try {
            effectsApplier.applyEffects(server, currentStatus, config);
        } catch (Exception e) {
            LOGGER.error("应用行星状态效果时出错", e);
        }
    }
    
    /**
     * 开始状态监控
     */
    public void startMonitoring(MinecraftServer server) {
        if (isMonitoring) {
            LOGGER.warn("行星状态监控已经在运行");
            return;
        }
        
        this.currentServer = server;
        this.isMonitoring = true;
        
        monitoringService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "Planet-Monitoring-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        // 每5秒检查一次状态
        monitoringService.scheduleAtFixedRate(() -> {
            try {
                performMonitoringTasks();
            } catch (Exception e) {
                LOGGER.error("行星状态监控任务出错", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
        
        LOGGER.info("行星状态监控已启动，检查间隔: 5秒");
    }
    
    /**
     * 执行监控任务
     */
    private void performMonitoringTasks() {
        if (currentServer == null) return;
        
        // 检查是否需要自动恶化
        checkAutoDegradation();
        
        // 检查灾难触发条件
        checkDisasterTriggers();
        
        // 更新状态显示
        updateStatusDisplay();
    }
    
    /**
     * 检查自动恶化
     */
    private void checkAutoDegradation() {
        // 如果文明进度长期停滞，行星状态会自动恶化
        // 这里可以实现基于时间的自动恶化逻辑
        // 例如：每在崩溃边缘状态停留一天，恶化程度增加
    }
    
    /**
     * 检查灾难触发条件
     */
    private void checkDisasterTriggers() {
        // 根据当前状态检查灾难触发概率
        // 恶劣状态下灾难触发概率更高
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatusDisplay() {
        // 更新所有玩家的状态显示
        // 例如：更新boss血条、HUD等
    }
    
    /**
     * 同步行星状态给玩家
     */
    public void syncPlanetStatus(ServerPlayerEntity player) {
        // TODO: 实现网络同步
        // 发送当前行星状态给新加入的玩家
    }
    
    /**
     * 获取当前行星状态
     */
    public PlanetStatus getCurrentStatus() {
        return currentStatus;
    }
    
    /**
     * 获取前一个行星状态
     */
    public PlanetStatus getPreviousStatus() {
        return previousStatus;
    }
    
    /**
     * 获取在当前状态的天数
     */
    public int getDaysInCurrentStatus() {
        return daysInCurrentStatus;
    }
    
    /**
     * 获取状态改变时间
     */
    public long getStatusChangeTime() {
        return statusChangeTime;
    }
    
    /**
     * 判断状态是否正在恶化
     */
    public boolean isStatusWorsening() {
        return currentStatus.getSeverity() > previousStatus.getSeverity();
    }
    
    /**
     * 记录状态变化日志
     */
    private void logStatusChange(PlanetStatus oldStatus, PlanetStatus newStatus, int progress) {
        String logMessage = String.format(
            "行星状态变化: %s -> %s | 进度: %d%% | 时间: %s",
            oldStatus.name(),
            newStatus.name(),
            progress,
            new java.util.Date()
        );
        
        // 这里可以将日志保存到文件或数据库
        LOGGER.info(logMessage);
    }
    
    /**
     * 加载数据
     */
    public void load() {
        // TODO: 实现数据加载
        LOGGER.info("加载行星状态数据...");
    }
    
    /**
     * 保存数据
     */
    public void save() {
        // TODO: 实现数据保存
        LOGGER.info("保存行星状态数据...");
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (monitoringService != null) {
            monitoringService.shutdown();
            try {
                if (!monitoringService.awaitTermination(5, TimeUnit.SECONDS)) {
                    monitoringService.shutdownNow();
                }
            } catch (InterruptedException e) {
                monitoringService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            monitoringService = null;
        }
        
        isMonitoring = false;
        currentServer = null;
        LOGGER.info("行星状态管理器资源已清理");
    }
    
    /**
     * 强制设置行星状态（仅用于测试或管理命令）
     */
    public void setStatus(PlanetStatus status, int progress) {
        if (status != currentStatus) {
            changeStatus(status, progress);
        }
    }
}