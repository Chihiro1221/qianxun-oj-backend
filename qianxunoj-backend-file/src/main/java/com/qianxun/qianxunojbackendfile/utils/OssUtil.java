package com.qianxun.qianxunojbackendfile.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.qianxun.qianxunojbackendcommon.common.ErrorCode;
import com.qianxun.qianxunojbackendcommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class OssUtil {

    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    // 允许的文件后缀名列表
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png"};

    // 最大允许的文件大小（例如：2MB）
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @param path 存储路径（如 "images/"）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String path) {
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("文件大小超过限制");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过限制");
        }

        // 检查文件后缀名
        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (!isAllowedExtension(fileExtension)) {
            log.error("不允许的文件后缀名: {}", fileExtension);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不允许的文件后缀名");
        }

        try {
            // 生成唯一文件名
            String fileName = path + UUID.randomUUID() + "." +
                    StringUtils.getFilenameExtension(file.getOriginalFilename());

            PutObjectResult putObjectResult = ossClient.putObject(bucketName, fileName,
                    new ByteArrayInputStream(file.getBytes()));

            // 生成访问URL
            return String.format("https://%s.%s/%s", bucketName, endpoint, fileName);
            //return generateUrl(fileName);
        } catch (IOException e) {
            log.error("文件上传失败：", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     * 检查文件后缀名是否允许
     *
     * @param extension 文件后缀名
     * @return 是否允许
     */
    private boolean isAllowedExtension(String extension) {
        if (extension == null) {
            return false;
        }
        for (String allowedExtension : ALLOWED_EXTENSIONS) {
            if (allowedExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成文件访问URL
     */
    //private String generateUrl(String fileName) {
    //    return ossClient.url(bucketName, fileName).toString();
    //}

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        //String fileName = ossClient.extractFileName(fileUrl);
        //ossClient.deleteObject(bucketName, fileName);
    }

    // 其他工具方法...
}
