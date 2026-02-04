package com.planetaryexodus.api.interfaces;

/**
 * 事件订阅者接口
 * 所有希望接收事件的组件都应该实现此接口
 * @param <T> 事件类型
 */
public interface IEventSubscriber<T> {
    
    /**
     * 当事件发生时调用
     * @param event 事件对象
     */
    void onEvent(T event);
    
    /**
     * 获取订阅的事件类型
     * @return 事件类型类
     */
    default Class<T> getEventType() {
        return null; // 默认实现，可由子类覆盖
    }
}