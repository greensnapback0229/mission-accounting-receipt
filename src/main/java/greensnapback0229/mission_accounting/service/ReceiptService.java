package greensnapback0229.mission_accounting.service;

import greensnapback0229.mission_accounting.domain.Receipt;
import greensnapback0229.mission_accounting.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public Receipt saveReceipt(MultipartFile file, String title, String description, String payer) throws IOException {
        // 파일 확장자 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|jpeg|png)$")) {
            throw new IllegalArgumentException("JPG, JPEG, PNG 파일만 업로드 가능합니다.");
        }

        // 저장할 파일명 생성 (UUID)
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + originalFilename;
        Path imagePath = Paths.get(uploadDir, savedFileName);

        // 디렉토리 없으면 생성
        Files.createDirectories(imagePath.getParent());

        // 파일 저장
        Files.copy(file.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

        // DB 저장
        Receipt receipt = Receipt.builder()
                .title(title)
                .description(description)
                .payer(payer)
                .imageName(savedFileName)
                .imagePath(imagePath.toString())
                .uploadedAt(LocalDateTime.now())
                .build();

        return receiptRepository.save(receipt);
    }

    public List<Receipt> getAllReceipts() {
        return (List<Receipt>) receiptRepository.findAll();
    }

    public Receipt getReceiptById(Long id) {
        return receiptRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 ID의 영수증이 존재하지 않습니다: " + id));
    }
}
