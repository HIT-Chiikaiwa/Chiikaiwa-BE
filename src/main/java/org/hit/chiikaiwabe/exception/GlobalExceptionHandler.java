package org.hit.chiikaiwabe.exception;

import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  private String getMessageOrDefault(String code, Object[] args) {
    if (code == null) {
      return ErrorMessage.ERR_EXCEPTION_GENERAL;
    }
    return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
  }

  //Error validate for param
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<RestData<?>> handleConstraintViolationException(ConstraintViolationException ex) {
    Map<String, String> result = new LinkedHashMap<>();
    ex.getConstraintViolations().forEach((error) -> {
      String fieldName = ((PathImpl) error.getPropertyPath()).getLeafNode().getName();
      String errorMessage = getMessageOrDefault(error.getMessage(), null);
      result.put(fieldName, errorMessage);
    });
    return VsResponseUtil.error(HttpStatus.BAD_REQUEST, "Validation error", result);
  }

  //Error validate for body
  @ExceptionHandler(BindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<RestData<?>> handleValidException(BindException ex) {
    Map<String, String> result = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = getMessageOrDefault(error.getDefaultMessage(), null);
      result.put(fieldName, errorMessage);
    });
    return VsResponseUtil.error(HttpStatus.BAD_REQUEST, "Validation error", result);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<RestData<?>> handlerInternalServerError(Exception ex) {
    log.error(ex.getMessage(), ex);
    String message = getMessageOrDefault(ErrorMessage.ERR_EXCEPTION_GENERAL, null);
    return VsResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }

  //Exception custom
  @ExceptionHandler(VsException.class)
  public ResponseEntity<RestData<?>> handleVsException(VsException ex) {
    log.error(ex.getMessage(), ex);
    return VsResponseUtil.error(ex.getStatus(), ex.getErrMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<RestData<?>> handlerNotFoundException(NotFoundException ex) {
    String message = getMessageOrDefault(ex.getMessage(), ex.getParams());
    log.error(message, ex);
    return VsResponseUtil.error(ex.getStatus(), message);
  }

  @ExceptionHandler(InvalidException.class)
  public ResponseEntity<RestData<?>> handlerInvalidException(InvalidException ex) {
    log.error(ex.getMessage(), ex);
    String message = getMessageOrDefault(ex.getMessage(), ex.getParams());
    return VsResponseUtil.error(ex.getStatus(), message);
  }

  @ExceptionHandler(InternalServerException.class)
  public ResponseEntity<RestData<?>> handlerInternalServerException(InternalServerException ex) {
    String message = getMessageOrDefault(ex.getMessage(), ex.getParams());
    log.error(message, ex);
    return VsResponseUtil.error(ex.getStatus(), message);
  }

  @ExceptionHandler(UploadFileException.class)
  public ResponseEntity<RestData<?>> handleUploadImageException(UploadFileException ex) {
    String message = getMessageOrDefault(ex.getMessage(), ex.getParams());
    log.error(message, ex);
    return VsResponseUtil.error(ex.getStatus(), message);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<RestData<?>> handleUnauthorizedException(UnauthorizedException ex) {
    String message = getMessageOrDefault(ex.getMessage(), ex.getParams());
    log.error(message, ex);
    return VsResponseUtil.error(ex.getStatus(), message);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<RestData<?>> handleAccessDeniedException(ForbiddenException ex) {
    String message = getMessageOrDefault(ex.getMessage(), ex.getParams());
    log.error(message, ex);
    return VsResponseUtil.error(ex.getStatus(), message);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<RestData<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
    log.error("Max upload size exceeded: {}", ex.getMessage());
    String message = getMessageOrDefault(ErrorMessage.File.ERR_FILE_SIZE_EXCEED, null);
    return VsResponseUtil.error(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public ResponseEntity<RestData<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    log.warn("Method not allowed: {} - Request: {} {}", ex.getMessage(), request.getMethod(), request.getRequestURI());
    return VsResponseUtil.error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
  }

}