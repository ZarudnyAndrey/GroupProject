package com.skillbox.sw.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.skillbox.sw.api.response.FileUploadResponseApi;
import com.skillbox.sw.api.response.ResponseApi;
import com.skillbox.sw.domain.Person;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

  @Value("${cloudinary.cloud_name}")
  private String cloudName;

  @Value("${cloudinary.url}")
  String cloudUri;

  @Value("${cloudinary.api_key}")
  private String apiKey;

  @Value("${cloudinary.api_secret}")
  private String apiSecret;

  @Autowired
  PersonService personService;

  private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  public ResponseApi fileUpload(String token, String type, MultipartFile file) {
    try {
      Cloudinary cloudinary = new Cloudinary(
          "cloudinary://" + apiKey + ":" + apiSecret + "@" + cloudName);
      Map uploadResponse = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

      Person person = personService.getCurrentPersonByToken(token);
      Integer ownerId = person.getId();

      FileUploadResponseApi fileUploadResponse = new FileUploadResponseApi();
      String photoId = uploadResponse.get("public_id").toString();
      fileUploadResponse.setId(photoId);
      fileUploadResponse.setOwnerId(ownerId);
      fileUploadResponse.setFileName(photoId + "." + uploadResponse.get("format").toString());
      fileUploadResponse.setRelativeFilePath(cloudUri + cloudName + "/image/upload/");
      fileUploadResponse.setRawFileURL(uploadResponse.get("url").toString());
      fileUploadResponse.setFileFormat(uploadResponse.get("format").toString());
      fileUploadResponse.setBytes(Long.valueOf(uploadResponse.get("bytes").toString()));
      fileUploadResponse.setFileType(FileUploadResponseApi.FileType.IMAGE);
      fileUploadResponse
          .setCreatedAt(formatter.parse(uploadResponse.get("created_at").toString()).getTime());

      ResponseApi response = new ResponseApi<>();
      response.setError("");
      response.setTimestamp(new Timestamp(System.currentTimeMillis()).getTime());
      response.setData(fileUploadResponse);
      return response;
    } catch (IOException | ParseException e) {
      new ResponseApi<FileUploadResponseApi>("Не удалось загрузить файл. " + e.getMessage(), null);
    }
    return new ResponseApi<>(null, null);
  }
}