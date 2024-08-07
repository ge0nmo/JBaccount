package com.jbaacount.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jbaacount.model.File;
import com.jbaacount.repository.FileRepository;
import com.jbaacount.global.exception.BusinessLogicException;
import com.jbaacount.global.exception.ExceptionMessage;
import com.jbaacount.model.Member;
import com.jbaacount.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class FileService
{
    private final FileRepository fileRepository;

    private final String bucket = "jbaccount";
    private final AmazonS3 amazonS3;

    @Transactional
    public File save(File file)
    {
        return fileRepository.save(file);
    }

    @Transactional
    public File saveForOauth2(String picture, Member member)
    {
        File file = File
                .builder()
                .uploadFileName(UUID.randomUUID().toString())
                .storeFileName(UUID.randomUUID().toString())
                .url(picture)
                .contentType(UUID.randomUUID().toString())
                .build();

        file.addMember(member);

        return fileRepository.save(file);
    }

    @Transactional
    public File updateForOAuth2(String picture, Member member)
    {
        File file = getFileByMemberId(member.getId())
                .orElseGet(() -> {
                    return saveForOauth2(picture, member);
                });

        file.setUrl(picture);

        return file;
    }


    @Transactional
    public List<File> storeFiles(List<MultipartFile> files, Post post)
    {
        List<File> storedFiles = new ArrayList<>();
        log.info("파일 갯수 = {}", files.size());

        for(MultipartFile file : files)
        {
            File storedFile = storeFileInPost(file, post);
            storedFiles.add(storedFile);

            log.info("file saved successfully = {}", storedFile.getStoredFileName());
        }

        log.info("저장 후 파일 개수 = {}", storedFiles.size());

        return storedFiles;
    }

    @Transactional
    public void deleteUploadedFile(Post post)
    {
        List<File> files = fileRepository.findByPostId(post.getId());

        if(files != null && !files.isEmpty())
        {
            for (File file : files)
            {
                amazonS3.deleteObject(bucket, "post/" + file.getStoredFileName());
                log.info("file removed successfully = {}", file.getStoredFileName());
                post.removeFile(file);
            }
        }

        fileRepository.deleteAll(files);
    }

    @Transactional
    public void deleteProfilePhoto(Member member)
    {
        Optional<File> file = fileRepository.findByMemberId(member.getId());

        if(file.isPresent())
        {
            log.info("file removed successfully = {}", file.get().getStoredFileName());
            fileRepository.deleteById(file.get().getId());
        }
    }

    @Transactional
    public File storeProfileImage(MultipartFile multipartFile, Member member)
    {
        String ext = multipartFile.getContentType();
        if(!ext.contains("image"))
            throw new BusinessLogicException(ExceptionMessage.EXT_NOT_ACCEPTED);

        String uploadFileName = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(uploadFileName);
        String location = "profile/";

        try{
            saveUploadFile(storeFileName, multipartFile, location);
        } catch (IOException e){
            throw new BusinessLogicException(ExceptionMessage.FILE_NOT_STORED);
        }

        File file = File.builder()
                .uploadFileName(uploadFileName)
                .storeFileName(storeFileName)
                .url(getFileUrl(storeFileName, location))
                .contentType(extractContentType(multipartFile))
                .build();

        file.addMember(member);
        return fileRepository.save(file);
    }

    private Optional<File> getFileByMemberId(Long memberId)
    {
        return fileRepository.findByMemberId(memberId);
    }


    private File storeFileInPost(MultipartFile multipartFile, Post post)
    {
        String uploadFileName = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(uploadFileName);
        String location = "post/";

        try{
            saveUploadFile(storeFileName, multipartFile, location);
        } catch (IOException e){
            throw new BusinessLogicException(ExceptionMessage.FILE_NOT_STORED);
        }

        File file = File.builder()
                .uploadFileName(uploadFileName)
                .storeFileName(storeFileName)
                .url(getFileUrl(storeFileName, location))
                .contentType(extractContentType(multipartFile))
                .build();


        File filePS = fileRepository.save(file);
        filePS.addPost(post);

        return filePS;
    }

    private void saveUploadFile(String storeFileName, MultipartFile file, String location) throws IOException
    {
        String contentType = extractContentType(file);
        ObjectMetadata metadata = new ObjectMetadata();

        metadata.setContentType(contentType);
        metadata.setContentLength(file.getSize());
        amazonS3.putObject(bucket, location + storeFileName, file.getInputStream(), metadata);
    }

    private String getFileUrl(String fileName, String location)
    {
        return amazonS3.getUrl(bucket,  location + fileName).toString();
    }

    private String extractContentType(MultipartFile multipartFile)
    {
        String contentType = multipartFile.getContentType();
        String ext = extractedEXT(multipartFile.getOriginalFilename());
        log.info("content type = {}", contentType);

        if(contentType == null || "application/octet-stream".equals(contentType))
        {
            switch (ext.toLowerCase())
            {
                case "jfif":
                    contentType = "image/jpeg";
                    break;
            }
        }

        return contentType;
    }

    private String createStoreFileName(String originalFileName)
    {
        String uuid = UUID.randomUUID().toString();

        String ext = extractedEXT(originalFileName);

        return uuid + "." + ext;
    }

    private String extractedEXT(String originalFileName)
    {
        int num = originalFileName.lastIndexOf(".");

        String ext = originalFileName.substring(num + 1);

        return ext;
    }
}