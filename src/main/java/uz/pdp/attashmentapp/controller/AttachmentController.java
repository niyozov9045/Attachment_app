package uz.pdp.attashmentapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.pdp.attashmentapp.entity.Attachment;
import uz.pdp.attashmentapp.entity.AttachmentContent;
import uz.pdp.attashmentapp.repository.AttachmentContentRepository;
import uz.pdp.attashmentapp.repository.AttachmentRepository;


import javax.servlet.http.HttpServletResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    AttachmentRepository attachmentRepository;
    @Autowired
    AttachmentContentRepository attachmentContentRepository;
    private static final String uploadDirectory = "uploads";


    @PostMapping("/uploadDb")
    public String uploadFile(MultipartHttpServletRequest request) throws IOException {
        System.out.println(System.currentTimeMillis());
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null) {
            Attachment attachment = new Attachment();
            attachment.setFileOriginalName(file.getOriginalFilename());
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());
            Attachment savedAttachment = attachmentRepository.save(attachment);

            AttachmentContent attachmentContent = new AttachmentContent();
            attachmentContent.setBytes(file.getBytes());
            attachmentContent.setAttachment(savedAttachment);
            attachmentContentRepository.save(attachmentContent);
            System.out.println(System.currentTimeMillis());
            return "File saqlandi. ID si:" + savedAttachment.getId();
        }
        return "File Saqnalmadi";
//        Iterator<String> fileNames = request.getFileNames();
//        MultipartFile file = request.getFile(fileNames.next());
//        if (file != null) {
//            //FILE HAQIDA MA'LUMOT OLISH UCHUN
//            String originalFilename = file.getOriginalFilename();
//            long size = file.getSize();
//            String contentType = file.getContentType();
//            Attachment attachment = new Attachment();
//            attachment.setFileOriginalName(originalFilename);
//            attachment.setSize(size);
//            attachment.setContentType(contentType);
//            Attachment savedAttachment = attachmentRepository.save(attachment);
//
//            //FILENI CONTENT(BYTE []) SAQLAYMIZ
//            AttachmentContent attachmentContent = new AttachmentContent();
//            attachmentContent.setBytes(file.getBytes());
//            attachmentContent.setAttachment(savedAttachment);
//            attachmentContentRepository.save(attachmentContent);
//            return "File Saqalandi. ID si: " + savedAttachment.getId();
//        }
//        return "File Saqlanmadi";
    }


    @PostMapping("/uploadSystem")
    public String UploadFileSystem(MultipartHttpServletRequest request) throws IOException {
        System.out.println(System.currentTimeMillis());
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null) {
            String originalFilename = file.getOriginalFilename();
            Attachment attachment = new Attachment();
            attachment.setFileOriginalName(originalFilename);
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());

            String[] split = originalFilename.split("\\.");
            String name = UUID.randomUUID().toString() + "." + split[split.length - 1];
            attachment.setName(name);
            attachmentRepository.save(attachment);
            Path path = Paths.get(uploadDirectory + "/" + name);
            Files.copy(file.getInputStream(), path);
            System.out.println(System.currentTimeMillis());
            return "Fayl saqlandi ID si: " + attachment.getId();
        }
        return "Saqlanmadi";
    }


    @GetMapping("/downloadFromDb/{id}")
    public void getFile(@PathVariable Integer id, HttpServletResponse response) throws IOException {

        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            Optional<AttachmentContent> contentOptional = attachmentContentRepository.findByAttachmentId(id);
            if (contentOptional.isPresent()) {
                AttachmentContent attachmentContent = contentOptional.get();
                //FILENI NOMINI BERISH UCHUN
                response.setHeader("Content-Disposition",
                        "attachment:filename=\"" + attachment.getFileOriginalName() + "\"");
                //FILENI CONTENTINI BERISH UCHUN
                response.setContentType(attachment.getContentType());
                // FILENI ASOSIY BYTENI BERISH UCHUN
                FileCopyUtils.copy(attachmentContent.getBytes(), response.getOutputStream());
            }
        }
    }

    @GetMapping("/downloadFileSystem/{id}")
    public void getFailSystem(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            response.setHeader("Content-Disposition",
                    "attachment:filename=\"" + attachment.getFileOriginalName() + "\"");
            //FILENI CONTENTINI BERISH UCHUN
            response.setContentType(attachment.getContentType());

            FileInputStream fileInputStream = new FileInputStream(uploadDirectory + "/" + attachment.getName());
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
//            FileCopyUtils.copy(fileInputStream, response.getOutputStream());


        }
    }
}
