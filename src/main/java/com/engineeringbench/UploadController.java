package com.engineeringbench;

import com.engineeringbench.service.IngestionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {

  private final IngestionService ingestionService;

  public UploadController(IngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @PostMapping("/upload")
  public String upload(@RequestParam MultipartFile file) throws Exception {
    ingestionService.ingest(file);
    return "uploaded";
  }
}