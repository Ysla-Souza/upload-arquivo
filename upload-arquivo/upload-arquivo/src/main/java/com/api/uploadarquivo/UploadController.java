package com.api.uploadarquivo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/api/files")
public class UploadController {
	private final Path uploadRepository;
	
	public UploadController(UploadProperties uploadProperties) {
		this.uploadRepository = Paths.get(uploadProperties.getUploadDir())
				.toAbsolutePath().normalize();		
	}
	@PostMapping("/uploads")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file){
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		
		 try {
		      Path targetRepository = uploadRepository.resolve(fileName);
		      file.transferTo(targetRepository);

		      String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
		          .path("/api/files/download/")
		          .path(fileName)
		          .toUriString();

		      return ResponseEntity.ok("Arquivo enviado com sucesso. Link para Download: " + fileDownloadUri);
		    } catch (IOException ex) {
		      ex.printStackTrace();
		      return ResponseEntity.badRequest().body("File upload failed.");
		    }
		}
	@GetMapping("/download/{fileName:.+}")
	  public ResponseEntity<Resource> downloadFile(@PathVariable String fileName,
	      HttpServletRequest request) throws IOException {
	    Path filePath = uploadRepository.resolve(fileName).normalize();
	    try {
	      Resource resource = new UrlResource(filePath.toUri());

	      String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	      if (contentType == null) {
	        contentType = "application/octet-stream";
	      }

	      return ResponseEntity.ok()
	          .contentType(MediaType.parseMediaType(contentType))
	          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
	          .body(resource);
	    } catch (MalformedURLException ex) {
	      return ResponseEntity.badRequest().body(null);
	    }
	  }

	  @GetMapping("/list")
	  public ResponseEntity<List<String>> listFiles() throws IOException {
	    List<String> fileNames = Files.list(uploadRepository)
	        .map(Path::getFileName)
	        .map(Path::toString)
	        .collect(Collectors.toList());

	    return ResponseEntity.ok(fileNames);
	  }
	}

