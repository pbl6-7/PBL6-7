package com.campus.activity.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 上传图片文件
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!isImageFile(extension)) {
            throw new IllegalArgumentException("只支持图片文件：jpg、jpeg、png、gif");
        }

        String newFileName = generateFileName(extension);
        String relativePath = getRelativePath();
        
        File destFile = new File(uploadPath + File.separator + relativePath, newFileName);
        destFile.getParentFile().mkdirs();
        
        file.transferTo(destFile);
        
        return relativePath + "/" + newFileName;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        try {
            File file = new File(uploadPath + File.separator + filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 检查是否为图片文件
     */
    private boolean isImageFile(String extension) {
        return "jpg".equals(extension) || "jpeg".equals(extension) 
            || "png".equals(extension) || "gif".equals(extension);
    }

    /**
     * 生成新的文件名
     */
    private String generateFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date()) + "_" + uuid + "." + extension;
    }

    /**
     * 获取相对路径（按日期分类）
     */
    private String getRelativePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(new Date());
    }

    /**
     * 获取上传目录的绝对路径
     */
    public String getUploadAbsolutePath() {
        return new File(uploadPath).getAbsolutePath();
    }
}
