package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.certificatemanager.CertificateManagerApp.util.FilesUtil;

import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Transactional
public class FilesService {
    private final FilesUtil filesUtil;

    private final String filePath = "C:\\Users\\danie\\IdeaProjects\\CertificateManagerApp\\src\\main\\java\\pl\\certificatemanager\\CertificateManagerApp\\files\\";

    public void saveFile(MultipartFile file) {
        try {
            String filePathFull = filePath + file.getOriginalFilename();
            Files.copy(file.getInputStream(), Path.of(filePathFull));
            filesUtil.saveFromFileToDatabase(filePathFull);
            filesUtil.deleteFile(filePathFull);
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }
}
