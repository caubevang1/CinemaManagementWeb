package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.UploadResponse;
import com.cinemaweb.API.Cinema.Web.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final String DEFAULT_FOLDER = "cinemaweb/misc";
    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "cinemaweb/avatar",
            "cinemaweb/comment",
            "cinemaweb/foodanddrink",
            "cinemaweb/misc"
    );

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = DEFAULT_FOLDER) String folder) {

        String safeFolder = ALLOWED_FOLDERS.contains(folder) ? folder : DEFAULT_FOLDER;
        String url = cloudinaryService.upload(file, safeFolder);

        return ApiResponse.<UploadResponse>builder()
                .message("Image uploaded")
                .body(UploadResponse.builder().url(url).build())
                .build();
    }
}
