package br.com.tisaicore.service.file;

import br.com.tisaicore.entity.SisFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    SisFile upload(MultipartFile file) throws IOException;

    void delete(SisFile sisFile) throws IOException;

    String getUrl(SisFile sisFile);
}
