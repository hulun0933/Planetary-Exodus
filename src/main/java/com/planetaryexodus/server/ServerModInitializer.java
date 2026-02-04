package com.planetaryexodus.server;

import com.planetaryexodus.PlanetaryExodusMod;
import com.planetaryexodus.core.EventBus;
import com.planetaryexodus.planet.PlanetStatusManager;
import com.planetaryexodus.civilization.CivilizationManager;
import com.planetaryexodus.disaster.DisasterManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器端初始化器
 * 负责服务器端的系统初始化和事件监听
 */
public class ServerModInitializer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PlanetaryExodus/Server");
    
    // 核心管理器
    private static PlanetStatusManager planetStatusManager;
    private static CivilizationManager civilizationManager;
    private static DisasterManager disasterManager;
    
    // 服务器状态
    private static boolean initialized = false;
    
    /**
     * 初始化服务器端系统
     */
    public static void init() {
        if (initialized) {
            LOGGER.warn("服务器系统已经初始化，跳过重复初始化");
            return;
        }
        
        LOGGER.info("🚀 初始化行星迁移计划服务器系统...");
        
        // 初始化核心管理器
        planetStatusManager = new PlanetStatusManager();
        civilizationManager = new CivilizationManager();
        disasterManager = new DisasterManager();
        
        // 注册服务器生命周期事件
        registerServerEvents();
        
        // 注册玩家连接事件
        registerPlayerEvents();
        
        // 注册服务器tick事件
        registerTickEvents();
        
        initialized = true;
        LOGGER.info("✅ 服务器系统初始化完成");
        LOGGER.info("   - 行星状态管理器: 已启用");
        LOGGER.info("   - 文明进度管理器: 已启用");
        LOGGER.info("   - 灾难管理器: 已启用");
        LOGGER.info("   - 事件监听器: 已注册");
    }
    
    /**
     * 注册服务器生命周期事件
     */
    private static void registerServerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("服务器启动中...");
            // 加载保存的数据
            civilizationManager.load();
            planetStatusManager.load();
        });
        
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("服务器已启动，开始行星状态监控");
            // 启动行星状态监控
            planetStatusManager.startMonitoring(server);
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("服务器停止中...");
            // 保存数据
            civilizationManager.save();
            planetStatusManager.save();
        });
        
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            LOGGER.info("服务器已停止，清理资源");
            cleanup();
        });
    }
    
    /**
     * 注册玩家连接事件
     */
    private static void registerPlayerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            LOGGER.info("玩家 {} 加入了服务器", handler.player.getName().getString());
            // 同步玩家数据
            civilizationManager.syncPlayerData(handler.player);
            planetStatusManager.syncPlanetStatus(handler.player);
        });
        
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            LOGGER.info("玩家 {} 离开了服务器", handler.player.getName().getString());
            // 保存玩家数据
            civilizationManager.savePlayerData(handler.player);
        });
    }
    
    /**
     * 注册服务器tick事件
     */
    private static void registerTickEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) { // 每秒执行一次
                try {
                    // 更新文明进度
                    civilizationManager.update(server);
                    
                    // 更新行星状态
                    planetStatusManager.update(server, civilizationManager.getProgress());
                    
                    // 更新灾难系统
                    disasterManager.update(server, planetStatusManager.getCurrentStatus());
                    
                    // 检查里程碑
                    civilizationManager.checkMilestones(server);
                    
                } catch (Exception e) {
                    LOGGER.error("服务器tick更新时出错", e);
                }
            }
        });
    }
    
    /**
     * 清理资源
     */
    private static void cleanup() {
        if (planetStatusManager != null) {
            planetStatusManager.cleanup();
        }
        if (civilizationManager != null) {
            civilizationManager.cleanup();
        }
        if (disasterManager != null) {
            disasterManager.cleanup();
        }
        
        initialized = false;
        LOGGER.info("服务器资源已清理");
    }
    
    /**
     * 获取行星状态管理器
     */
    public static PlanetStatusManager getPlanetStatusManager() {
        return planetStatusManager;
    }
    
    /**
     * 获取文明进度管理器
     */
    public static CivilizationManager getCivilizationManager() {
        return civilizationManager;
    }
    
    /**
     * 获取灾难管理器
     */
    public static DisasterManager getDisasterManager() {
        return disasterManager;
    }
    
    /**
     * 检查服务器系统是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 重新加载所有配置
     */
    public static void reloadConfig() {
        if (!initialized) {
            LOGGER.warn("服务器未初始化，无法重新加载配置");
            return;
        }
        
        LOGGER.info("重新加载服务器配置...");
        PlanetaryExodusMod.getInstance().reloadConfig();
        
        // 重新初始化管理器
        if (planetStatusManager != null) {
            planetStatusManager.reloadConfig();
        }
        if (civilizationManager != null) {
            civilizationManager.reloadConfig();
        }
        if (disasterManager != null) {
            disasterManager.reloadConfig();
        }
        
        LOGGER.info("服务器配置已重新加载");
    }
}