package org.zerock.b01.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.zerock.b01.dto.upload.UploadFileDTO;
import org.zerock.b01.dto.upload.UploadResultDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@RestController
@Log4j2
public class UpDownController {

    @Value("${org.zerock.upload.path}")// import 시에 springframework으로 시작하는 Value
    private String uploadPath;

    @Operation(summary =  "POST 방식으로 파일 등록")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadResultDTO> upload(
            @Parameter(
                    description = "Files to be uploaded",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            UploadFileDTO uploadFileDTO){

        log.info(uploadFileDTO);

        if(uploadFileDTO.getFiles() != null){

            final List<UploadResultDTO> list = new ArrayList<>();

            uploadFileDTO.getFiles().forEach(multipartFile -> {

                String originalName = multipartFile.getOriginalFilename();
                log.info(originalName);

                String uuid = UUID.randomUUID().toString();

                Path savePath = Paths.get(uploadPath, uuid+"_"+ originalName);

                boolean image = false;

                try {
                    multipartFile.transferTo(savePath);

                    //이미지 파일의 종류라면
                    if(Files.probeContentType(savePath).startsWith("image")){

                        image = true;

                        File thumbFile = new File(uploadPath, "s_" + uuid+"_"+ originalName);

                        Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 500,500);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                list.add(UploadResultDTO.builder()
                        .uuid(uuid)
                        .fileName(originalName)
                        .img(image).build()
                );

            });//end each

            return list;
        }//end if

        return null;
    }


    @Operation(summary =  "GET방식으로 첨부파일 조회")
    @GetMapping("/view/{fileName}")
    public ResponseEntity<Resource> viewFileGET(@PathVariable String fileName){

        Resource resource = new FileSystemResource(uploadPath+File.separator + fileName);
        String resourceName = resource.getFilename();
        HttpHeaders headers = new HttpHeaders();

        try{
            headers.add("Content-Type", Files.probeContentType( resource.getFile().toPath() ));
        } catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @Operation(summary = "DELETE 방식으로 파일 삭제")
    @DeleteMapping("/remove/{fileName}")
    public Map<String,String> removeFile(@PathVariable String fileName){

        Resource resource = new FileSystemResource(uploadPath+File.separator + fileName);
        String resourceName = resource.getFilename();

        Map<String, String> resultMap = new HashMap<>();
        String removed = null;

        try {
            String contentType = Files.probeContentType(resource.getFile().toPath());
            if(resource.getFile().delete()){
                // 파일 삭제 성공 시 removed에 removed 저장
                removed = "removed";
            }

            //섬네일이 존재한다면
            if(contentType.startsWith("image")){
                File thumbnailFile = new File(uploadPath+File.separator +"s_" + fileName);
                thumbnailFile.delete();
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        resultMap.put("result", removed);

        return resultMap;
    }

    // 파일 다운로드 처리
//    @GetMapping("/download/{fileName}")
//    public void fileDownload(@PathVariable String fileName,
//                             HttpServletResponse response) throws IOException {
//        String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
//        File f = new File(uploadPath,  fileName);
//        // file 다운로드 설정
//        response.setContentType("application/download");
//        response.setContentLength((int)f.length());
//        response.setHeader("Content-disposition", "attachment;filename=\"" + fileName + "\"");
//        // response 객체를 통해서 서버로부터 파일 다운로드
//        OutputStream os = response.getOutputStream();
//        // 파일 입력 객체 생성
//        FileInputStream fis = new FileInputStream(f); // 파일을 찾음
//        FileCopyUtils.copy(fis, os);
//        fis.close();
//        os.close();
//        log.info("다운로드 컨트롤러 끝");
//    }

}
