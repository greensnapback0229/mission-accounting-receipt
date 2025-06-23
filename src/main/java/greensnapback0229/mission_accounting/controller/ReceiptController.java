package greensnapback0229.mission_accounting.controller;


import greensnapback0229.mission_accounting.domain.Receipt;
import greensnapback0229.mission_accounting.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;


    @GetMapping("/")
    public String showReceipt(Model model) {
        List<Receipt> receipts = receiptService.getAllReceipts();
        model.addAttribute("receipts", receipts);
        return "list";
    }

    // 업로드 폼 보여주기
    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload";
    }

    // 업로드 처리
    @PostMapping("/upload")
    public String uploadReceipt(@RequestParam("file") MultipartFile file,
                                @RequestParam("title") String title,
                                @RequestParam("description") String description,
                                @RequestParam("payer") String payer,
                                Model model) {
        try {
            receiptService.saveReceipt(file, title, description, payer);
            return "redirect:/list";
        } catch (IOException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "upload";
        }
    }

    // 목록 페이지
    @GetMapping("/list")
    public String listReceipts(Model model) {
        List<Receipt> receipts = receiptService.getAllReceipts();
        model.addAttribute("receipts", receipts);
        return "list";
    }

    // 상세 보기
    @GetMapping("/view/{id}")
    public String viewReceipt(@PathVariable Long id, Model model) {
        Receipt receipt = receiptService.getReceiptById(id);
        model.addAttribute("receipt", receipt);
        return "detail";
    }

    // 다운로드
    @GetMapping("/pic/{id}")
    public ResponseEntity<Resource> downloadReceiptImage(@PathVariable Long id) {
        Receipt receipt = receiptService.getReceiptById(id);
        File file = new File(receipt.getImagePath());

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String encodedFileName = URLEncoder.encode(receipt.getImageName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/delete/{id}")
    public String deleteReceipt(@PathVariable Long id) {
        receiptService.deleteReceipt(id);
        return "redirect:/list";
    }

}
