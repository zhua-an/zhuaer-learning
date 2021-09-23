package com.zhuaer.learning.webserver.client;

/**
 * @ClassName ImportWsClient
 * @Description 用import命令生成客户端代码
 * @Author zhua
 * @Date 2020/8/13 12:51
 * @Version 1.0
 */
public class ImportWsClient {

    /**
     * 用import命令生成客户端代码
     * -keep：是否生成java源文件
     *
     * -d：指定.class文件的输出目录
     *
     * -s：指定.java文件的输出目录
     *
     * -p：定义生成类的包名，不定义的话有默认包名
     *
     * -verbose：在控制台显示输出信息
     *
     * -b：指定jaxws/jaxb绑定文件或额外的schemas
     *
     * -extension：使用扩展来支持SOAP1.2
     *
     * wsimport -d d:/webservice -keep -p com.zhua.test.wsimportClient -verbose http://127.0.0.1:11008/webService?wsdl
     * @param args
     */
    public static void main(String[] args) {
//        MyWebService_Service service = new MyWebServiceImpl_Service();
//        MyWebService myWebService = service.getMyWebService();
//        String result = myWebService.sayHello("admin");
//        System.out.println(result);
    }

}



