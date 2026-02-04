package com.planetaryexodus.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令注册器
 * 注册所有模组相关的命令
 */
public class CommandRegistry {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PlanetaryExodus/Commands");
    
    public static void register() {
        LOGGER.info("注册命令系统...");
        // 命令注册将在后续实现中完善
    }
    
    /**
     * 注册服务器命令
     */
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 在这里注册具体的命令
        // 例如：
        // dispatcher.register(CommandManager.literal("planetaryexodus")
        //     .then(CommandManager.literal("status")
        //         .executes(context -> executeStatusCommand(context.getSource())))
        // );
        LOGGER.debug("命令注册完成");
    }
    
    /**
     * 执行状态命令
     */
    private static int executeStatusCommand(ServerCommandSource source) {
        source.sendMessage(net.minecraft.text.Text.literal("行星迁移计划模组状态：运行中"));
        return 1;
    }
}