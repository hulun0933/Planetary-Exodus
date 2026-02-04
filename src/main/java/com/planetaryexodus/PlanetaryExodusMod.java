package com.planetaryexodus;

import com.planetaryexodus.core.EventBus;
import com.planetaryexodus.core.ModConfig;
import com.planetaryexodus.server.ServerModInitializer;
import com.planetaryexodus.command.CommandRegistry;
import com.planetaryexodus.network.ModPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;

/**
 * 行星迁移计划 - 主模组入口
 * 采用混合架构：模块化 + 事件驱动
 */
public class PlanetaryExodusMod implements ModInitializer {

    public static final String MOD_ID = "planetaryexodus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // 单例实例
    private static PlanetaryExodusMod INSTANCE;
    
    // 核心系统
    private EventBus eventBus;
    private ModConfig config;
    
    @Override
    public void onInitialize() {
        INSTANCE = this;
        
        LOGGER.info("🚀 行星迁移计划模组初始化中...");
        
        // 初始化事件总线
        eventBus = EventBus.getInstance();
        
        // 加载配置
        config = ModConfig.load();
        
        // 初始化网络数据包
        ModPackets.register();
        
        // 初始化命令系统
        CommandRegistry.register();
        
        // 初始化服务器系统
        ServerModInitializer.init();
        
        LOGGER.info("✅ 行星迁移计划模组初始化完成！");
        LOGGER.info("🌌 架构：模块化 + 事件驱动");
        LOGGER.info("👥 系统：行星状态、文明进度、玩家职业、火箭系统、灾难系统");
    }
    
    /**
     * 获取模组实例
     */
    public static PlanetaryExodusMod getInstance() {
        return INSTANCE;
    }
    
    /**
     * 获取事件总线
     */
    public EventBus getEventBus() {
        return eventBus;
    }
    
    /**
     * 获取配置管理器
     */
    public ModConfig getConfig() {
        return config;
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        config = ModConfig.load();
        LOGGER.info("配置已重新加载");
    }
}
