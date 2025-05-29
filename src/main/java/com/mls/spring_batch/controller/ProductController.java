package com.mls.spring_batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final JobLauncher jobLauncher;
    private final Job importProducts;

    public ProductController(JobLauncher jobLauncher, Job importProducts) {
        this.jobLauncher = jobLauncher;
        this.importProducts = importProducts;
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<String> uploadCsvAndRun(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Por favor envía un archivo CSV no vacío.");
        }

        // 1. Guardar el MultipartFile en disco temporalmente
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("upload-batch-");
            String originalFilename = file.getOriginalFilename();
            // Usa el nombre original o genera uno nuevo
            Path tempFile = tempDir.resolve(originalFilename != null
                    ? originalFilename
                    : "uploaded.csv");
            // Copiar el contenido
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 2. Construir JobParameters con la URI del fichero recién guardado
            String filePathUri = tempFile.toUri().toString();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFile", filePathUri)  // coincide con @Value("#{jobParameters['inputFile']}")
                    .addLong("startAt", System.currentTimeMillis()) // evita re-ejecución con mismos params
                    .toJobParameters();

            // 3. Lanzar el Job
            jobLauncher.run(importProducts, jobParameters);

            return ResponseEntity.ok("Job lanzado correctamente. Procesando: " + filePathUri);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el archivo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al lanzar el job: " + e.getMessage());
        }
    }
}
