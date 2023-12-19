package com.cora.fastbi.utils.AI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cora.fastbi.config.KeyConfig;
import com.cora.fastbi.exception.BusinessException;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class XunfeiAIUtil extends WebSocketListener {
    // 地址与鉴权信息  https://spark-api.xf-yun.com/v1.1/chat   1.5地址  domain参数为general
    // 地址与鉴权信息  https://spark-api.xf-yun.com/v2.1/chat   2.0地址  domain参数为generalv2
    // 星火大模型V3请求地址，对应的domain参数为generalv3
    public static final String hostUrl = "https://spark-api.xf-yun.com/v3.1/chat";

    // TODO 静态变量无法获取

    @Value("${mykeys.xunfei.apisecret}")
    public static String appid = KeyConfig.getXunfeiAppid();

    @Value("${mykeys.xunfei.apisecret}")
    public static String apiSecret = KeyConfig.getXunfeiApiSecret();

    @Value("${mykeys.xunfei.apikey}")
    public static String apiKey = KeyConfig.getXunfeiApiKey();


    // public static List<RoleContent> historyList = new ArrayList<>(); // 对话历史存储集合

    @Getter
    public static String totalAnswer = ""; // 大模型的答案汇总

    // 环境治理的重要性  环保  人口老龄化  我爱我的祖国
    public String newQuestion = "";

    public static final Gson gson = new Gson();

    // 个性化参数
    private String userId;
    private Boolean wsCloseFlag;


    public interface OnWebSocketCompleteListener {
        void onComplete(String totalAnswer);
    }

    private OnWebSocketCompleteListener callback;

    // 构造函数
    public XunfeiAIUtil(String newQuestion, Boolean wsCloseFlag, OnWebSocketCompleteListener callback) {
        totalAnswer = "";
        this.newQuestion = newQuestion;
        this.wsCloseFlag = wsCloseFlag;
        this.callback = callback;
    }


    @Override
    public void onClosed(WebSocket webSocket, int code, @NotNull String reason) {
        // 处理 WebSocket 连接关闭事件
        callback.onComplete(getTotalAnswer());
    }


/*    public static boolean canAddHistory() {  // 由于历史记录最大上线1.2W左右，需要判断是能能加入历史
        int history_length = 0;
        for (RoleContent temp : historyList) {
            history_length = history_length + temp.content.length();
        }
        if (history_length > 12000) {
            historyList.remove(0);
            historyList.remove(1);
            historyList.remove(2);
            historyList.remove(3);
            historyList.remove(4);
            return false;
        } else {
            return true;
        }
    }*/

    // 线程来发送音频与参数
    class MyThread extends Thread {
        private WebSocket webSocket;

        public MyThread(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        public void run() {
            try {
                JSONObject requestJson = new JSONObject();

                JSONObject header = new JSONObject();  // header参数
                header.put("app_id", appid);
                header.put("uid", userId);

                JSONObject parameter = new JSONObject(); // parameter参数
                JSONObject chat = new JSONObject();
                chat.put("domain", "generalv3");
                chat.put("temperature", 0.5);
                chat.put("max_tokens", 4096);
                parameter.put("chat", chat);

                JSONObject payload = new JSONObject(); // payload参数
                JSONObject message = new JSONObject();
                JSONArray text = new JSONArray();

                // 历史问题获取
//                if (historyList.size() > 0) {
//                    for (RoleContent tempRoleContent : historyList) {
//                        text.add(JSON.toJSON(tempRoleContent));
//                    }
//                }

                // 最新问题
                RoleContent roleContent = new RoleContent();
                roleContent.role = "user";
                roleContent.content = newQuestion;
                text.add(JSON.toJSON(roleContent));
                // historyList.add(roleContent);


                message.put("text", text);
                payload.put("message", message);


                requestJson.put("header", header);
                requestJson.put("parameter", parameter);
                requestJson.put("payload", payload);
                //log.info(requestJson.toString()); // 可以打印看每次的传参明细
                webSocket.send(requestJson.toString());
                // 等待服务端返回完毕后关闭
                while (true) {
                    // System.err.println(wsCloseFlag + "---");
                    Thread.sleep(200);
                    if (wsCloseFlag) {
                        break;
                    }
                }
                webSocket.close(1000, "");
            } catch (Exception e) {
                throw new BusinessException(10000, "模型异常");
            }
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
//        System.out.print("大模型：");
        MyThread myThread = new MyThread(webSocket);
        myThread.start();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // System.out.println(userId + "用来区分那个用户的结果" + text);
        JsonParse myJsonParse = gson.fromJson(text, JsonParse.class);
        if (myJsonParse.header.code != 0) {
            System.out.println("发生错误，错误码为：" + myJsonParse.header.code);
            System.out.println("本次请求的sid为：" + myJsonParse.header.sid);
            webSocket.close(1000, "");
        }
        List<Text> textList = myJsonParse.payload.choices.text;
        for (Text temp : textList) {
//            System.out.print(temp.content);
            totalAnswer = totalAnswer + temp.content;
        }
        if (myJsonParse.header.status == 2) {
            // 可以关闭连接，释放资源
            System.out.println("成功获取到结果");
//            System.out.println();
//            System.out.println("*************************************************************************************");
//            if (canAddHistory()) {
//                RoleContent roleContent = new RoleContent();
//                roleContent.setRole("assistant");
//                roleContent.setContent(totalAnswer);
//                historyList.add(roleContent);
//            } else {
//                historyList.remove(0);
//                RoleContent roleContent = new RoleContent();
//                roleContent.setRole("assistant");
//                roleContent.setContent(totalAnswer);
//                historyList.add(roleContent);
//            }
            wsCloseFlag = true;
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            if (null != response) {
                int code = response.code();
                System.out.println("onFailure code:" + code);
                System.out.println("onFailure body:" + response.body().string());
                if (101 != code) {
                    System.out.println("connection failed");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // 鉴权方法
    public static String getAuthUrl() {
        try {
            URL url = new URL(hostUrl);
            // 时间
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            // 拼接
            String preStr = "host: " + url.getHost() + "\n" +
                    "date: " + date + "\n" +
                    "GET " + url.getPath() + " HTTP/1.1";
            // System.err.println(preStr);
            // SHA256加密
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
            mac.init(spec);

            byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
            // Base64加密
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            // System.err.println(sha);
            // 拼接
            String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
            // 拼接地址
            HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                    addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                    addQueryParameter("date", date).//
                    addQueryParameter("host", url.getHost()).//
                    build();

            // System.err.println(httpUrl.toString());
            return httpUrl.toString();
        } catch (Exception e) {
            throw new BusinessException(10000, "鉴权信息添加失败");
        }
    }

    //返回的json结果拆解
    class JsonParse {
        Header header;
        Payload payload;
    }

    class Header {
        int code;
        int status;
        String sid;
    }

    class Payload {
        Choices choices;
    }

    class Choices {
        List<Text> text;
    }

    class Text {
        String role;
        String content;
    }

    class RoleContent {
        String role;
        String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
