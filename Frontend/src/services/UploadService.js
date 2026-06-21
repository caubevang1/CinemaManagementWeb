import { http } from "../utils/baseUrl";

export const UploadImage = (file, folder = "cinemaweb/misc") => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("folder", folder);

    return http.post("/upload/image", formData, {
        timeout: 30000,
    });
};
