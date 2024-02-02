package com.watson.order.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyuncs.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 工具类
 */
@Slf4j
@Component
public class AliOSSUtils {

    private final String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";

    private final String bucketName = "campus-order";

    /**
     * 实现上传图片到OSS
     *
     * @param file 前端上传的文件
     * @return 上传图片后的网址
     * @throws IOException 获取文件异常
     */
    public String upload(MultipartFile file) throws Exception {

        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        // 避免没有文件名
        if (StringUtils.isEmpty(originalFilename)) {
            originalFilename = UUID.randomUUID().toString();
        }
        String fileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 上传文件到 OSS
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        ossClient.putObject(bucketName, fileName, inputStream);

        log.info("endpoint is: {}, bucketName is: {}, fileName is: {}", endpoint, bucketName, fileName);

        // 文件访问路径
        String url = endpoint.split("//")[0] + "//"   //  'https://'
                + bucketName + "."  // https://your-bucket-name.
                + endpoint.split("//")[1] + "/"  // oss-cn-guangzhou.aliyuncs.com
                + fileName;

        log.info("upload file's url is: {}", url);
        // 关闭ossClient
        ossClient.shutdown();
        return url;// 把上传到oss的路径返回
    }

    /**
     * 调用阿里云oss接口，删除图片
     *
     * @param url 图片源地址
     * @throws Exception 删除发生的异常
     */
    public void delete(String url) throws Exception {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        log.info("delete image url :{}", url);
        // 分隔网址，获得文件名
        String fileName = url.split("aliyuncs.com/")[1];
        log.info("delete file's name is: {}", fileName);

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        try {
            // 判断文件是否存在。如果返回值为true，则文件存在，否则存储空间或者文件不存在。
            boolean found = ossClient.doesObjectExist(bucketName, fileName);
            if (found) {
                // 删除文件或目录。如果要删除目录，目录必须为空。
                ossClient.deleteObject(bucketName, fileName);
                boolean existed = ossClient.doesObjectExist(bucketName, fileName);
                log.info(existed ? "删除失败！" : "删除成功!");
            }

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}
