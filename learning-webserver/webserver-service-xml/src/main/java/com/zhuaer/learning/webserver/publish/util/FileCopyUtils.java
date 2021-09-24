package com.zhuaer.learning.webserver.publish.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @ClassName FileCopyUtils
 * @Description 将jar内的文件复制到jar包外的同级目录下
 * @Author zhua
 * @Date 2021/9/24 8:59
 * @Version 1.0
 */
public class FileCopyUtils {

    private static final Logger log = LoggerFactory.getLogger(FileCopyUtils.class);

    private static InputStream getResource(String location) throws IOException {
        InputStream in=null;
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            in = resolver.getResource(location).getInputStream();
            byte[] byteArray = IOUtils.toByteArray(in);
            return new ByteArrayInputStream(byteArray);
        }catch (Exception e){
            e.printStackTrace();
            log.error("getResource is error: {}", e);
            return null;
        }finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 获取项目所在文件夹的绝对路径
     * @return
     */
    private static String getCurrentDirPath() {
        URL url = FileCopyUtils.class.getProtectionDomain().getCodeSource().getLocation();
        String path = url.getPath();
        if(path.startsWith("file:")) {
            path = path.replace("file:", "");
        }
        if(path.contains(".jar!/")) {
            path = path.substring(0, path.indexOf(".jar!/")+4);
        }

        File file = new File(path);
        path = file.getParentFile().getAbsolutePath();
        return path;
    }

    private static Path getDistFile(String path) throws IOException {
        String currentRealPath = getCurrentDirPath();
        Path dist = Paths.get(currentRealPath + File.separator + path);
        Path parent = dist.getParent();
        if(parent != null) {
            Files.createDirectories(parent);
        }
        Files.deleteIfExists(dist);
        return dist;
    }

    /**
     * 复制classpath下的文件到jar包的同级目录下
     * @param location 相对路径文件,例如kafka/kafka_client_jaas.conf
     * @return
     * @throws IOException
     */
    public static String copy(String location) throws IOException {
        InputStream in = getResource("classpath:"+location);
        Path dist = getDistFile(location);
        Files.copy(in, dist);
        in.close();
        return dist.toAbsolutePath().toString();
    }

}
