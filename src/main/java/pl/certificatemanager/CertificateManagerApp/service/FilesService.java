package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.certificatemanager.CertificateManagerApp.util.FilesUtil;

import javax.transaction.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Transactional
public class FilesService {
    private final FilesUtil filesUtil;

    @Value("${filesManagement.path}")
    private String directoryPath;

    public void saveFile(MultipartFile file) {
        try {
            String filePathFull = directoryPath + file.getOriginalFilename();
            Files.copy(file.getInputStream(), Path.of(filePathFull));
            filesUtil.saveFromFileToDatabase(filePathFull);
            filesUtil.deleteFile(filePathFull);
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public File downloadFile(String typeOfExport) {
        return filesUtil.saveFromDatabaseToFile(typeOfExport);
    }
}
