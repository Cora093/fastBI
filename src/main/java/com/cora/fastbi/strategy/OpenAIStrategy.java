package com.cora.fastbi.strategy;

import com.cora.fastbi.common.ErrorCode;
import com.cora.fastbi.config.KeyConfig;
import com.cora.fastbi.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class OpenAIStrategy implements AIStrategy {

    @Override
    public String AIQuestion(String question) {
        try {
            // 设置API端点URL
            String apiUrl = "https://api.openai.com/v1/chat/completions";

            // 设置请求头
            String apiKey = KeyConfig.getOpenAiApiKey();
            String contentType = "application/json";
            String authorization = "Bearer " + apiKey;

            // 设置请求体数据
            String requestData = "{"
                    + "\"model\": \"gpt-3.5-turbo\","
                    + "\"messages\": ["
                    + "{"
                    + "\"role\": \"user\","
                    + "\"content\": "
                    + "\"" + question + "\""
                    + "}"
                    + "]"
                    + "}";


            // 创建URL对象
            URL url = new URL(apiUrl);

            // 创建代理对象 TODO 测试环境用
            //Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));

            // 打开连接
            //HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为POST
            connection.setRequestMethod("POST");

            // 设置请求头
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Authorization", authorization);

            // 启用输入流和输出流
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // 发送请求体数据
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(requestData);
            outputStream.flush();
            outputStream.close();

            // 获取HTTP响应代码
            int responseCode = connection.getResponseCode();

            // 读取响应内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();

            // 输出响应内容和响应代码
            System.out.println("HTTP响应代码: " + responseCode);
            System.out.println("响应内容: " + response.toString());



            // 关闭连接
            connection.disconnect();

            return response.toString();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI接口回调异常");
        }
    }
}
