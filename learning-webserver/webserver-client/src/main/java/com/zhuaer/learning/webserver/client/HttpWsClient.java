package com.zhuaer.learning.webserver.client;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;

/**
 * @ClassName HttpWsClient
 * @Description httpclient作为客户端调用webservice
 * @Author zhua
 * @Date 2020/8/13 14:03
 * @Version 1.0
 */
public class HttpWsClient {

    /**
     * httpclient作为客户端调用webservice
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        //soap服务地址
        String url = "http://127.0.0.1:11008/webService?wsdl";
        StringBuilder soapBuilder = new StringBuilder(64);
        soapBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        soapBuilder.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://service.publish.webserver.learning.zhuaer.com\">");
        soapBuilder.append("   <soapenv:Header/>");
        soapBuilder.append("       <soapenv:Body>");
        soapBuilder.append("             <web:sayHello>");
        soapBuilder.append("                     <username>").append("admin").append("</username>");
        soapBuilder.append("               </web:sayHello>");
        soapBuilder.append("    </soapenv:Body>");
        soapBuilder.append("</soapenv:Envelope>");

        //创建httpcleint对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建http Post请求
        HttpPost httpPost = new HttpPost(url);
        // 构建请求配置信息
        RequestConfig config = RequestConfig.custom().setConnectTimeout(1000) // 创建连接的最长时间
                .setConnectionRequestTimeout(500) // 从连接池中获取到连接的最长时间
                .setSocketTimeout(3 * 1000) // 数据传输的最长时间10s
                .build();
        httpPost.setConfig(config);
        CloseableHttpResponse response = null;
        try {
            //采用SOAP1.1调用服务端，这种方式能调用服务端为soap1.1和soap1.2的服务
            httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");

            //采用SOAP1.2调用服务端，这种方式只能调用服务端为soap1.2的服务
            // httpPost.setHeader("Content-Type", "application/soap+xml;charset=UTF-8");
            StringEntity stringEntity = new StringEntity(soapBuilder.toString(), Charset.forName("UTF-8"));
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println("调用结果为:"+content);

                ///
                // 返回结果为
                // <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                //  <soap:Body>
                //      <ns2:sayHelloResponse xmlns:ns2="http://service.zhua.web.com/wsdl">
                //          <return>hello,admin</return>
                //      </ns2:sayHelloResponse>
                //   </soap:Body>
                // </soap:Envelope>
                // 用Jsoup提取响应数据
                Document soapRes = Jsoup.parse(content);
                Elements returnEle = soapRes.getElementsByTag("return");
                System.out.println("调用结果为:"+returnEle.text());
            } else {
                System.out.println("调用失败!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != response) {
                response.close();
            }
            if (null != httpClient) {
                httpClient.close();
            }
        }


    }
}
