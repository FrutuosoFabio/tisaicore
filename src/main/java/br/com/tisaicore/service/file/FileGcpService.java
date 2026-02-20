package br.com.tisaicore.service.file;

import br.com.tisaicore.entity.SisFile;
import br.com.tisaicore.repository.SisFileRepository;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
@Profile("gcp")
public class FileGcpService implements FileService {

    @Value("${storage.gcp.bucket-name}")
    private String bucketName;

    private final SisFileRepository sisFileRepository;

    public FileGcpService(SisFileRepository sisFileRepository) {
        this.sisFileRepository = sisFileRepository;
    }

    @Override
    @Transactional
    public SisFile upload(MultipartFile file) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String datePath = new SimpleDateFormat("yyyyMM").format(new Date());
        String objectPath = datePath + "/" + UUID.randomUUID() + extension;

        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, objectPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .setCacheControl("public, max-age=31536000")
                .build();

        storage.createFrom(blobInfo, file.getInputStream());

        SisFile sisFile = new SisFile();
        sisFile.setOriginalName(file.getOriginalFilename());
        sisFile.setStoragePath(objectPath);
        sisFile.setContentType(file.getContentType());
        sisFile.setSize(file.getSize());

        return sisFileRepository.save(sisFile);
    }

    @Override
    @Transactional
    public void delete(SisFile sisFile) throws IOException {
        try {
            Storage storage = StorageOptions.getDefaultInstance().getService();
            storage.delete(BlobId.of(bucketName, sisFile.getStoragePath()));
        } catch (StorageException e) {
            throw new IOException("Erro ao deletar arquivo do GCS: " + e.getMessage(), e);
        }
        sisFile.setActive(false);
        sisFileRepository.save(sisFile);
    }

    @Override
    public String getUrl(SisFile sisFile) {
        return "https://storage.googleapis.com/" + bucketName + "/" + sisFile.getStoragePath();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
