package com.chatgpt.demo.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.theokanning.openai.completion.chat.ChatMessage;


public class ConversationMemory {
    private static ConversationMemory instance;

    // 用于存储对话和记忆数据的字段
    private Map<String, List<ChatMessage>> conversationMap = new HashMap<>();

    // 私有构造函数，防止外部实例化
    private ConversationMemory() {}

    // 获取单例实例的方法
    public static synchronized ConversationMemory getInstance() {
        if (instance == null) {
            instance = new ConversationMemory();
        }
        return instance;
    }

    // 添加对话记忆
    public void addMemory(String userId, ChatMessage message) {
        conversationMap.computeIfAbsent(userId, k -> new ArrayList<ChatMessage>()).add(message);
    }

    // 获取对话记忆
    public List<ChatMessage> getMemory(String userId) {
        return conversationMap.getOrDefault(userId, new ArrayList<ChatMessage>());
    }

    // 可以添加其他操作和逻辑，如清除对话记忆等
    public void clearAllMemory(String userId) {
        conversationMap.remove(userId);
    }

    // 清理第一轮的对话
    public void clearTwoMemory(String userId) {
        List<ChatMessage> conversation = conversationMap.get(userId);
        if (conversation != null && conversation.size() >= 2) {
            // 移除前两条记录
            conversation.remove(0);
            conversation.remove(0);
        }
    }
    

}