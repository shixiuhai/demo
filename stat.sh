#!/bin/bash

# 设置参数变量
JAR_FILE="./demo-0.0.1-SNAPSHOT.jar" # 启动的java包名称
IS_PROXY=false # 是否开启代理
TIMEOUT=300 # 请求接口超时设置
CHAT_MODEL="gpt-3.5-turbo-16k" # 模型设置
SK="1qaz@WSX" # 三方调api接口的key
API_KEY="1234" # openai的可以
CHAT_TEMPERATURE=0.7 # 机器人的温度
CHAT_MAX_TOKENS=4000 # 回答问题最多返回多少tokens

# 启动Java程序
nohup java -jar $JAR_FILE --openai.isProxy=$IS_PROXY --openai.timeout=$TIMEOUT --openai.chatModel=$CHAT_MODEL --openai.sk=$SK --openai.apiKey=$API_KEY --openai.chatTemperature=$CHAT_TEMPERATURE --openai.chatMaxTokens=$CHAT_MAX_TOKENS >output.log &

# 输出启动信息
echo "Java程序已启动"
