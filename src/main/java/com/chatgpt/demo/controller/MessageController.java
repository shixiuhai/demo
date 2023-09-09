package com.chatgpt.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
// 自定义的实体类和单列模式类
import com.chatgpt.demo.common.ConversationMemory;
import com.chatgpt.demo.dto.Message;



import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.time.Duration;

import java.net.InetSocketAddress;
import java.net.Proxy;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import lombok.extern.slf4j.Slf4j;

import com.theokanning.openai.client.OpenAiApi;


@RestController
@RequestMapping("/api/gpt")
@Slf4j
public class MessageController {
    // openai的key
    @Value("${openai.apiKey}")
    private String apiKey;
    // 选择使用openai对话模型
    @Value("${openai.chatModel}")
    private String chatModel;
    // openai聊天模型的温度
    @Value("${openai.chatTemperature}")
    private Double chatTemperature;
    // 设置最大标记数  回答问题最大的返回数
    @Value("${openai.chatmaxTokens}")
    private int chatmaxTokens;
    // 设置会话记忆几轮
    @Value("${openai.conversationNumber}")
    private int conversationNumber;
    // 设置代理ip
    @Value("${openai.proxyHost}")
    private String proxyHost;
    // 设置代理端口
    @Value("${openai.proxyPort}")
    private int proxyPort;
    // 设置请求超时时间
    @Value("${openai.timeout}")
    private int timeout;
    // 设置是否使用代理
    @Value("${openai.isProxy}")
    private boolean isProxy;

    private ConversationMemory memory = ConversationMemory.getInstance();

    @PostMapping("")
    public Map<String,String> createMessage(@RequestBody Message message) {
        log.info("-------------"+Boolean.toString(isProxy));
        // 大于设置的轮数清理会话记忆
        if(memory.getMemory(message.getUserId()).size()>=conversationNumber*2){
            memory.clearMemory(message.getUserId());
        }
        
        // 添加会话到记忆列表中
        ChatMessage chatMessageUser = new ChatMessage();
        chatMessageUser.setRole("user");
        chatMessageUser.setContent(message.getContent());
        memory.addMemory(message.getUserId(), chatMessageUser);

        Map<String,String> out = getOneChat(memory.getMemory(message.getUserId()));

        // 简单的处理了一下错误，所有的错误都移除历史会话
        if(out.get("code").equals("500")){
            memory.clearMemory(message.getUserId());
        }

        // 返回结果的角色是gpt，添加助手对话内容到对话列表
        ChatMessage chatMessageAssistant = new ChatMessage();
        chatMessageAssistant.setRole("assistant");
        chatMessageAssistant.setContent(out.get("content"));
        memory.addMemory(message.getUserId(), chatMessageAssistant);
        return out;
    }


    public Map<String,String> getOneChat(List<ChatMessage> chatList){  
        OpenAiService service = new OpenAiService(apiKey,Duration.ofSeconds(timeout));
        if(isProxy){
            ObjectMapper mapper = OpenAiService.defaultObjectMapper();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            OkHttpClient client = OpenAiService.defaultClient(apiKey, Duration.ofSeconds(timeout))
                    .newBuilder()
                    .proxy(proxy)
                    .build();
            Retrofit retrofit = OpenAiService.defaultRetrofit(client, mapper);
            OpenAiApi api = retrofit.create(OpenAiApi.class);
            service = new OpenAiService(api);
        }
        
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(chatModel)
                .messages(chatList)
                .temperature(chatTemperature)
                .maxTokens(chatmaxTokens)
                .build();
        Map<String,String> out = new HashMap<>();
        try {
            ChatCompletionResult result = service.createChatCompletion(completionRequest);
            //根据响应的结构（可以在官网查看，也可以自行输出查看），获取返回结果中的第一个结果中的内容
            String content = result.getChoices().get(0).getMessage().getContent();
            String  questionTokens = Long.toString(result.getUsage().getPromptTokens());
            String totalTokens = Long.toString(result.getUsage().getTotalTokens());
            String completionTokens = Long.toString(result.getUsage().getCompletionTokens());
            out.put("questionTokens", questionTokens);
            out.put("completionTokens", completionTokens);
            out.put("totalTokens", totalTokens);
            out.put("content", content);            
            out.put("code", "200");
            
        } catch (Exception e) {
            log.error("请求openAI接口出现错误，错误原因是:", e);
            // // TODO: handle exception
            out.put("questionTokens", "0");
            out.put("completionTokens", "0");
            out.put("totalTokens", "0");
            out.put("content", "我有点累了，请稍后再来吧");
            out.put("code", "500");
        }
        return out;
        
    }
    
}