package com.cora.fastbi.strategy;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.HttpClientOptions;
import com.cora.fastbi.config.KeyConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 具体策略1
 */
public class OpenAIStrategy implements AIStrategy {

    @Override
    public String AIQuestion(String question) {
        // Proxy options
        final String hostname = "localhost";
        final int port = 10809; // 本地测试端口

        // final int port = 8018; // 线上正式端口

        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(hostname, port));

        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new KeyCredential(KeyConfig.getOpenAiApiKey()))
                .clientOptions(new HttpClientOptions().setProxyOptions(proxyOptions))
                .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestUserMessage(question));

        ChatCompletions chatCompletions = client.getChatCompletions("gpt-3.5-turbo",
                new ChatCompletionsOptions(chatMessages));

        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatResponseMessage message = choice.getMessage();
            System.out.println("Message:");
            System.out.println(message.getContent());
            return message.getContent();
        }
        return null;

//        List<ChatRequestMessage> chatMessages = new ArrayList<>();
//        chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
//        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
//        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
//        chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));
//
//        ChatCompletions chatCompletions = client.getChatCompletions("gpt-3.5-turbo"
//                , new ChatCompletionsOptions(chatMessages));
//
//        for (ChatChoice choice : chatCompletions.getChoices()) {
//            ChatResponseMessage message = choice.getMessage();
//            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
//            System.out.println("Message:");
//            System.out.println(message.getContent());
//        }
//
//        System.out.println();
//        CompletionsUsage usage = chatCompletions.getUsage();
//        System.out.printf("Usage: prompt token : %d, "
//                        + "completion token: %d, total tokens : %d.%n",
//                usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());


//        try {
//            // 设置API端点URL
//            String apiUrl = "https://api.openai.com/v1/chat/completions";
//
//            // 设置请求头
//            String apiKey = KeyConfig.getOpenAiApiKey();
//            String contentType = "application/json";
//            String authorization = "Bearer " + apiKey;
//
//            // 设置请求体数据
//            String requestData = "{"
//                    + "\"model\": \"gpt-3.5-turbo\","
//                    + "\"messages\": ["
//                    + "{"
//                    + "\"role\": \"user\","
//                    + "\"content\": "
//                    + "\"" + question + "\""
//                    + "}"
//                    + "]"
//                    + "}";
//
//
//            // 创建URL对象
//            URL url = new URL(apiUrl);
//
//            // 创建代理对象 测试环境用
//            //Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
//
//            // 打开连接
//            //HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//            // 设置请求方法为POST
//            connection.setRequestMethod("POST");
//
//            // 设置请求头
//            connection.setRequestProperty("Content-Type", contentType);
//            connection.setRequestProperty("Authorization", authorization);
//
//            // 启用输入流和输出流
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//
//            // 发送请求体数据
//            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//            outputStream.writeBytes(requestData);
//            outputStream.flush();
//            outputStream.close();
//
//            // 获取HTTP响应代码
//            int responseCode = connection.getResponseCode();
//
//            // 读取响应内容
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//
//            while ((inputLine = reader.readLine()) != null) {
//                response.append(inputLine);
//            }
//            reader.close();
//
//            // 输出响应内容和响应代码
//            System.out.println("HTTP响应代码: " + responseCode);
//            System.out.println("响应内容: " + response.toString());
//
//
//
//            // 关闭连接
//            connection.disconnect();
//
//            return response.toString();
//
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI接口回调异常");
//        }
    }
}
