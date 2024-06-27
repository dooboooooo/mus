package org.zerock.b01.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.b01.dto.*;
import org.zerock.b01.service.AddressService;
import org.zerock.b01.service.CartService;
import org.zerock.b01.service.MemberService;
import org.zerock.b01.service.OrdersService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
public class DownloadController {

    @Value("${org.zerock.upload.path}")// import 시에 springframework으로 시작하는 Value
    private String uploadPath;

    // 파일 다운로드 처리(파일명에 한글이 포함된 파일을 다운로드하면 콘솔에 오류는 출력되지만, 다운로드는 된다 ..)
    // uuid까지 포함한 이름으로 다운로드 처리되어서 밑의 메서드로 수정함 ...
//    @GetMapping("/download/{fileName}")
//    public void fileDownload(@PathVariable String fileName,
//                             HttpServletResponse response) throws IOException {
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

    @GetMapping("/download/{fileName:.+}")
    // /download/{fileName} 경로에 대응한다. {fileName:.+}은 경로 변수로서 확장자를 포함한 파일명을 처리할 수 있도록 한다.
    public ResponseEntity<FileSystemResource> fileDownload(@PathVariable String fileName) throws IOException {
        // 요청받은 파일명을 사용하여 실제 파일 객체를 생성한다. 파일이 존재하지 않으면 예외처리
        File file = new File(uploadPath, fileName);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + fileName);
        }

        // 파일명에서 _를 기준으로 분리하고, 마지막 부분을 downloadFileName으로 설정하여 다운로드 시 파일명으로 사용한다.
        String[] parts = fileName.split("_"); // [uuid, 원본파일명]
        String downloadFileName = parts[parts.length - 1]; // 마지막 부분 사용(다운로드 파일명으로)

        // HTTP 응답 헤더를 설정하여 다운로드할 파일임을 브라우저에게 알린다.
        // Content-Disposition 헤더에 attachment; filename="..." 형식으로 설정하여 파일을 다운로드하도록 유도한다.
        HttpHeaders headers = new HttpHeaders();
        // 다운로드할 파일명인 downloadFileName을 UTF-8로 인코딩한 후, ISO-8859-1로 디코딩하여 문자열을 생성한다. 이는 Content-Disposition 헤더에서 사용하기 위한 인코딩 방식이다.
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + new String(downloadFileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");

        // 다운로드할 파일의 경로를 Paths.get(uploadPath, fileName)를 통해 얻고, 이를 FileSystemResource 객체로 생성
        Path filePath = Paths.get(uploadPath, fileName); // import : file
        FileSystemResource resource = new FileSystemResource(filePath);

        // ResponseEntity.ok()를 사용하여 HTTP 상태 코드 200 OK와 함께 파일을 응답 본문에 포함 시키고, 설정한 헤더, 파일 크기 및 FileSystemResource 객체도 포함 시킨다.
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(resource);
    }

}
