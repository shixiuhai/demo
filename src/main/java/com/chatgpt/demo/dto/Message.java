package com.chatgpt.demo.dto;

import lombok.Data;

@Data
public class Message {
    private String userId;
    private String content;
    private String sk;
}
