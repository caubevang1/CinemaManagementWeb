package com.cinemaweb.API.Cinema.Web.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryService {

    Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name}")
    @lombok.experimental.NonFinal
    String cloudName;

    // Bắt phần public_id giữa "/upload/[vNNN/]" và đuôi mở rộng cuối cùng
    private static final Pattern PUBLIC_ID_PATTERN =
            Pattern.compile("/upload/(?:v\\d+/)?(.+?)(?:\\.[a-zA-Z0-9]+)?$");

    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty())
            throw new AppException(ErrorCode.FILE_EMPTY);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);

        try {
            Map<?, ?> result = cloudinary.uploader()
                    .upload(file.getBytes(), ObjectUtils.asMap("folder", folder));
            return (String) result.get("secure_url");
        } catch (Exception e) {
            // IOException khi đọc file/mạng, hoặc RuntimeException khi cấu hình Cloudinary sai
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    /**
     * Xóa ảnh trên Cloudinary dựa vào URL đang lưu. Best-effort: không ném lỗi để
     * không làm fail thao tác chính. Bỏ qua URL không thuộc cloud của mình
     * (vd poster TMDB, chuỗi "default avatar", null).
     */
    public void deleteByUrl(String url) {
        if (url == null || url.isBlank())
            return;
        if (!url.contains("res.cloudinary.com/" + cloudName + "/"))
            return;

        try {
            Matcher matcher = PUBLIC_ID_PATTERN.matcher(url);
            if (!matcher.find()) {
                log.warn("Không trích được public_id từ URL: {}", url);
                return;
            }
            String publicId = matcher.group(1);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Xóa ảnh Cloudinary thất bại cho URL {}: {}", url, e.getMessage());
        }
    }
}
