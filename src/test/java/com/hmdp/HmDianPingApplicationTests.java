package com.hmdp;

import com.hmdp.entity.User;
import com.hmdp.service.IUserService;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.apache.http.impl.client.HttpClients;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private IUserService userService;

    @Autowired
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    private final ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    public void test(){
        shopService.saveShop2Redis(1L,1000L);
    }


    @Test
    public void testIdWorker()throws InterruptedException{
        CountDownLatch latch = new CountDownLatch(300);

        Runnable task = ()->{
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("总耗时：" + (end - begin));
    }

    @Test
    public void funtion(){
        String loginUrl = "http://localhost:8081/user/login"; // 替换为实际的登录URL
        String tokenFilePath = "tokens.txt"; // 存储Token的文件路径

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();

            BufferedWriter writer = new BufferedWriter(new FileWriter(tokenFilePath));

            // 从数据库中查询用户手机号
            List<User> users = userService.list();

            for (User user: users){
                String phoneNumber = "3383272743@qq.com";

                // 构建登录请求
                HttpPost httpPost = new HttpPost(loginUrl);

                JSONObject jsonRequest = new JSONObject();

                jsonRequest.put("phone", phoneNumber);
                StringEntity requestEntity = new StringEntity(
                        jsonRequest.toString(),
                        ContentType.APPLICATION_JSON
                );
                httpPost.setEntity(requestEntity);

                // 发送登录请求
                HttpResponse response = httpClient.execute(httpPost);

                // 处理登录响应，获取token
                if(response.getStatusLine().getStatusCode() == 200){
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity);
                    System.out.println(responseString);
                    // 解析响应， 获取token，这里假设响应是json格式的
                    // 根据实际情况使用合适的JSON库进行解析
                    String token = pareTokenFromJson(responseString);
                    System.out.println("手机号: " + phoneNumber + ", 登录成功,Token: " + token);
                    // 将token写入txt文件
                    writer.write(token);
                    writer.newLine();
                }else{
                    System.out.println("手机号: " + phoneNumber + ", 登录失败");
                }
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 解析JSON响应获取token的方法，这里是示例代码，具体实现需要根据实际响应格式进行解析
    private String pareTokenFromJson(String json) {
        try {
            // 将JSON字符串转换为JSONObject
            JSONObject jsonObject = new JSONObject(json);
            // 从JSONObject中获取名为"token"的字段的值
            String token = jsonObject.getString("data");
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 解析失败，返回null或者抛出异常，具体根据实际需求处理
        }
    }
}
