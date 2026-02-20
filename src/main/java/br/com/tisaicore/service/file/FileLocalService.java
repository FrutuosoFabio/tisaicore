package br.com.tisaicore.service.file;

import br.com.tisaicore.entity.SisFile;
import br.com.tisaicore.repository.SisFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
@Profile("local")
public class FileLocalService implements FileService {

    @Value("${storage.local.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${storage.local.base-url:http://localhost:8080}")
    private String baseUrl;

    private final SisFileRepository sisFileRepository;

    public FileLocalService(SisFileRepository sisFileRepository) {
        this.sisFileRepository = sisFileRepository;
    }

    @Override
    @Transactional
    public SisFile upload(MultipartFile file) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String datePath = new SimpleDateFormat("yyyyMM").format(new Date());
        String relativePath = datePath + "/" + UUID.randomUUID() + extension;

        Path targetPath = Paths.get(uploadDir, relativePath);
        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, file.getBytes());

        SisFile sisFile = new SisFile();
        sisFile.setOriginalName(file.getOriginalFilename());
        sisFile.setStoragePath(relativePath);
        sisFile.setContentType(file.getContentType());
        sisFile.setSize(file.getSize());

        return sisFileRepository.save(sisFile);
    }

    @Override
    @Transactional
    public void delete(SisFile sisFile) throws IOException {
        Files.deleteIfExists(Paths.get(uploadDir, sisFile.getStoragePath()));
        sisFile.setActive(false);
        sisFileRepository.save(sisFile);
    }

    @Override
    public String getUrl(SisFile sisFile) {
        return baseUrl + "/uploads/" + sisFile.getStoragePath();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
