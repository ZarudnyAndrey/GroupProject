package com.skillbox.sw.controller;

import com.skillbox.sw.api.response.AbstractResponse;
import com.skillbox.sw.api.response.FileUploadResponseApi;
import com.skillbox.sw.api.response.ResponseApi;
import com.skillbox.sw.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;

import static com.skillbox.sw.config.SecurityConstants.HEADER;

@RestController
@RequestMapping("/storage")
public class StorageController {
    @Autowired
    private StorageService fileUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public AbstractResponse fileUpload(@RequestHeader(value = HEADER) String token,
                                       @RequestParam("type") String type,
                                       @RequestBody MultipartFile file) throws IOException, ParseException {
        if (file != null && !file.isEmpty() && file.getContentType().toUpperCase().contains(type.toUpperCase())) {
            return fileUploadService.fileUpload(token,type,file);
        }
        String error = "INVALID_REQUEST";
        return new ResponseApi<FileUploadResponseApi>(error,null);
    }
}
