package com.hambugi.cullecting.global.exception;

import com.hambugi.cullecting.domain.member.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("업로드 가능한 파일의 최대 크기를 벗어남");
        return ResponseEntity.status(500).body(ApiResponse.error(500, "업로드 가능한 파일의 크기는 최대 30MB 입니다."));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("요청한 URL 이 존재하지 않음");
        return ResponseEntity.status(404).body(ApiResponse.error(404, "요청한 URL을 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        log.error("서버 내부 오류 발생");
        return ResponseEntity.status(500).body(ApiResponse.error(500, "서버 내부 오류가 발생했습니다."));
    }

}
