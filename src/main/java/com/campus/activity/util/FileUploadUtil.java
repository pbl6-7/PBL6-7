package com.campus.activity.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传工具类
 * 提供安全的文件上传功能，包含类型白名单、Magic Number校验、路径遍历防护和大小检查
 */
@Component
public class FileUploadUtil {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp")
    );

    /** 允许的图片 MIME 类型 */
    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp")
    );

    /** 图片文件 Magic Number（文件头签名） */
    private static final byte[] JPEG_SIG_1 = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] GIF_SIG_1 = {0x47, 0x49, 0x46, 0x38}; // "GIF8"
    private static final byte[] WEBP_SIG = {0x52, 0x49, 0x46, 0x46}; // "RIFF"

    /**
     * 上传图片文件（完整安全校验）
     *
     * @param file 上传的文件
     * @return 文件相对路径
     * @throws IOException 文件操作异常
     * @throws IllegalArgumentException 校验不通过
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 2. 文件大小校验
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制（最大" + (maxFileSize / 1024 / 1024) + "MB）");
        }

        // 3. 原始文件名校验（路径遍历防护）
        String originalFilename = file.getOriginalFilename();
        validateFilename(originalFilename);

        // 4. 扩展名白名单校验
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("只支持图片文件：" + String.join("、", ALLOWED_IMAGE_EXTENSIONS));
        }

        // 5. MIME 类型校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("文件MIME类型不被允许");
        }

        // 6. Magic Number（文件头）校验
        validateImageMagicNumber(file);

        // 7. 生成安全文件名并保存
        String newFileName = generateFileName(extension);
        String relativePath = getRelativePath();

        File destFile = new File(uploadPath + File.separator + relativePath, newFileName);

        // 8. 路径规范化校验（确保目标路径在上传目录内）
        validateDestinationPath(destFile);

        destFile.getParentFile().mkdirs();
        file.transferTo(destFile);

        return relativePath + "/" + newFileName;
    }

    /**
     * 上传头像文件（保存到指定子目录）
     *
     * @param file 上传的文件
     * @param subDir 子目录名（如 "avatars"）
     * @param namePrefix 文件名前缀（如用户ID）
     * @return 文件访问URL路径
     * @throws IOException 文件操作异常
     * @throws IllegalArgumentException 校验不通过
     */
    public String uploadImageToSubDir(MultipartFile file, String subDir, String namePrefix) throws IOException {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 2. 文件大小校验
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制（最大" + (maxFileSize / 1024 / 1024) + "MB）");
        }

        // 3. 原始文件名校验
        String originalFilename = file.getOriginalFilename();
        validateFilename(originalFilename);

        // 4. 扩展名白名单校验
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("只支持图片文件：" + String.join("、", ALLOWED_IMAGE_EXTENSIONS));
        }

        // 5. MIME 类型校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("文件MIME类型不被允许");
        }

        // 6. Magic Number 校验
        validateImageMagicNumber(file);

        // 7. 生成安全文件名
        String filename = namePrefix + "_" + System.currentTimeMillis() + "." + extension;
        File destFile = new File(uploadPath + File.separator + subDir, filename);

        // 8. 路径规范化校验
        validateDestinationPath(destFile);

        destFile.getParentFile().mkdirs();
        file.transferTo(destFile);

        return "/uploads/" + subDir + "/" + filename;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件相对路径
     * @return 是否删除成功
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
     * 获取上传目录的绝对路径
     *
     * @return 绝对路径字符串
     */
    public String getUploadAbsolutePath() {
        return new File(uploadPath).getAbsolutePath();
    }

    /**
     * 校验原始文件名安全性（防止路径遍历攻击）
     */
    private void validateFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        // 防止路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }
        // 防止空字节注入
        if (filename.contains("\0")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }
    }

    /**
     * 校验图片文件的 Magic Number（文件头签名）
     * 通过读取文件前几个字节验证文件是否为真实的图片文件
     */
    private void validateImageMagicNumber(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[16];
            int bytesRead = is.read(header);

            if (bytesRead < 4) {
                throw new IllegalArgumentException("文件内容无效");
            }

            boolean valid = false;

            // JPEG: FF D8 FF
            if (bytesRead >= 3 && startsWith(header, JPEG_SIG_1)) {
                valid = true;
            }
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            if (bytesRead >= 8 && startsWith(header, PNG_SIG)) {
                valid = true;
            }
            // GIF: "GIF8"
            if (bytesRead >= 4 && startsWith(header, GIF_SIG_1)) {
                valid = true;
            }
            // WebP: "RIFF"
            if (bytesRead >= 4 && startsWith(header, WEBP_SIG)) {
                valid = true;
            }

            if (!valid) {
                throw new IllegalArgumentException("文件内容与图片格式不匹配，可能是伪装文件");
            }
        }
    }

    /**
     * 校验目标文件路径在上传目录内（防止路径遍历）
     */
    private void validateDestinationPath(File destFile) throws IOException {
        String canonicalUploadPath = new File(uploadPath).getCanonicalPath();
        String canonicalDestPath = destFile.getCanonicalPath();
        if (!canonicalDestPath.startsWith(canonicalUploadPath)) {
            throw new IllegalArgumentException("非法的文件存储路径");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex == filename.lastIndexOf('.')) {
            String ext = filename.substring(dotIndex + 1).toLowerCase();
            // 扩展名长度校验（1-5个字符）
            if (ext.length() >= 1 && ext.length() <= 5) {
                return ext;
            }
        }
        return "";
    }

    /**
     * 生成新的安全文件名（UUID + 时间戳）
     */
    private String generateFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return timestamp + "_" + uuid + "." + extension;
    }

    /**
     * 获取相对路径（按日期分类）
     */
    private String getRelativePath() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    /**
     * 检查字节数组是否以指定前缀开头
     */
    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
