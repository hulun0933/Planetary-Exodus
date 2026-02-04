package com.planetaryexodus.core;

import com.planetaryexodus.api.interfaces.IEventSubscriber;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 事件总线系统 - 负责模块间的松耦合通信
 * 使用泛型支持不同类型的事件，确保线程安全
 */
public class EventBus {
    
    private static final EventBus INSTANCE = new EventBus();
    
    // 事件类型到订阅者列表的映射
    private final Map<Class<?>, List<IEventSubscriber<?>>> subscribers = new ConcurrentHashMap<>();
    // 快速查找缓存
    private final Map<Class<?>, List<Consumer<Object>>> consumerCache = new ConcurrentHashMap<>();
    
    private EventBus() {
        // 私有构造函数，单例模式
    }
    
    public static EventBus getInstance() {
        return INSTANCE;
    }
    
    /**
     * 订阅事件
     * @param eventType 事件类型类
     * @param subscriber 事件订阅者
     * @param <T> 事件类型
     */
    public <T> void subscribe(Class<T> eventType, IEventSubscriber<T> subscriber) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(subscriber);
        // 清空缓存
        consumerCache.remove(eventType);
    }
    
    /**
     * 订阅事件（使用Consumer接口简化）
     * @param eventType 事件类型类
     * @param consumer 事件处理器
     * @param <T> 事件类型
     */
    public <T> void subscribe(Class<T> eventType, Consumer<T> consumer) {
        subscribe(eventType, new IEventSubscriber<T>() {
            @Override
            public void onEvent(T event) {
                consumer.accept(event);
            }
            
            @Override
            public Class<T> getEventType() {
                return eventType;
            }
        });
    }
    
    /**
     * 取消订阅事件
     * @param eventType 事件类型类
     * @param subscriber 事件订阅者
     * @param <T> 事件类型
     */
    public <T> void unsubscribe(Class<T> eventType, IEventSubscriber<T> subscriber) {
        List<IEventSubscriber<?>> list = subscribers.get(eventType);
        if (list != null) {
            list.remove(subscriber);
            if (list.isEmpty()) {
                subscribers.remove(eventType);
            }
            // 清空缓存
            consumerCache.remove(eventType);
        }
    }
    
    /**
     * 发布事件
     * @param event 事件对象
     * @param <T> 事件类型
     */
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        Class<T> eventType = (Class<T>) event.getClass();
        
        // 首先尝试从缓存获取消费者
        List<Consumer<Object>> consumers = consumerCache.get(eventType);
        if (consumers == null) {
            consumers = new ArrayList<>();
            List<IEventSubscriber<?>> subs = subscribers.get(eventType);
            if (subs != null) {
                for (IEventSubscriber<?> sub : subs) {
                    consumers.add(e -> ((IEventSubscriber<T>) sub).onEvent((T) e));
                }
            }
            consumerCache.put(eventType, consumers);
        }
        
        // 执行所有消费者
        for (Consumer<Object> consumer : consumers) {
            try {
                consumer.accept(event);
            } catch (Exception e) {
                System.err.println("Error processing event: " + eventType.getSimpleName());
                e.printStackTrace();
            }
        }
        
        // 同时处理父类事件
        publishToSuperClasses(event);
    }
    
    /**
     * 向父类事件类型发布
     * @param event 事件对象
     */
    private void publishToSuperClasses(Object event) {
        Class<?> clazz = event.getClass().getSuperclass();
        while (clazz != null && clazz != Object.class) {
            if (subscribers.containsKey(clazz)) {
                List<IEventSubscriber<?>> subs = subscribers.get(clazz);
                for (IEventSubscriber<?> sub : subs) {
                    try {
                        @SuppressWarnings("unchecked")
                        IEventSubscriber<Object> subscriber = (IEventSubscriber<Object>) sub;
                        subscriber.onEvent(event);
                    } catch (Exception e) {
                        System.err.println("Error processing parent event: " + clazz.getSimpleName());
                        e.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
    
    /**
     * 获取事件订阅者数量
     * @param eventType 事件类型
     * @return 订阅者数量
     */
    public int getSubscriberCount(Class<?> eventType) {
        List<IEventSubscriber<?>> list = subscribers.get(eventType);
        return list != null ? list.size() : 0;
    }
    
    /**
     * 清空所有订阅
     */
    public void clear() {
        subscribers.clear();
        consumerCache.clear();
    }
    
    /**
     * 获取所有已注册的事件类型
     * @return 事件类型集合
     */
    public Set<Class<?>> getRegisteredEventTypes() {
        return Collections.unmodifiableSet(subscribers.keySet());
    }
}