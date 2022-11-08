package com.bezkoder.spring.files.upload.controller;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.bezkoder.spring.files.upload.message.ResponseMessage;
import com.bezkoder.spring.files.upload.model.FileInfo;
import com.bezkoder.spring.files.upload.service.FilesStorageService;

@Controller
@CrossOrigin(origins = "https://localhost:4200")
public class FilesController {

  @Autowired
  FilesStorageService storageService;

  @PostMapping("/upload")
  public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
    String message = "";
    try {
      storageService.save(file);

      message = "Uploaded the file successfully: " + file.getOriginalFilename();
      return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
    } catch (Exception e) {
      message = "Could not upload the file: " + file.getOriginalFilename() + "!";
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
    }
  }

  @CrossOrigin(origins = "https://localhost:4200")
  @GetMapping("/files")
  public ResponseEntity<List<FileInfo>> getListFiles() {
    List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
      String filename = path.getFileName().toString();
      String url = MvcUriComponentsBuilder
          .fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();

      return new FileInfo(filename, url);
    }).collect(Collectors.toList());

    return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
  }

 /* @GetMapping("/files/{filename:.+}")
  public ResponseEntity<Resource> getFile(@PathVariable String filename) {
    Resource file = storageService.load(filename);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
  }*/

  @GetMapping("/file/{filename:.+}")
  public ResponseEntity<JSONObject> getFileJson(@PathVariable String filename) {
    Resource file = storageService.load(filename);

    JSONParser parser = null;
    JSONObject jsonObject  = null;
    try {
      parser = new JSONParser(file.getInputStream());

      Object obj = parser.parse();
      jsonObject = (JSONObject)obj;
    } catch (Exception e) {

    }
    return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
  }

  @DeleteMapping("/delete/{filename:.+}")
  public ResponseEntity<ResponseMessage> deleteFile(@PathVariable String filename) {
       storageService.deleteFile(filename);
    String message = "Successfully Deleted File:"+filename;
    return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));

  }

  @DeleteMapping("/delete")
  public ResponseEntity<ResponseMessage> deleteAll(@PathVariable String filename) {
    storageService.deleteAll();
    String message = "Successfully Deleted All Files";
    return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));

  }
}
