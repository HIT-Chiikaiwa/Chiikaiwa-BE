package org.hit.chiikaiwabe.base;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

public class VsResponseUtil {

  public static <T> ResponseEntity<RestData<T>> success(T data) {
    return success(HttpStatus.OK, data);
  }

  public static <T> ResponseEntity<RestData<T>> success(HttpStatus status, T data) {
    RestData<T> response = new RestData<>(status.value(), true, "Success", data, Instant.now().toString());
    return new ResponseEntity<>(response, status);
  }

  public static <T> ResponseEntity<RestData<T>> success(MultiValueMap<String, String> header, T data) {
    return success(HttpStatus.OK, header, data);
  }

  public static <T> ResponseEntity<RestData<T>> success(HttpStatus status, MultiValueMap<String, String> header, T data) {
    RestData<T> response = new RestData<>(status.value(), true, "Success", data, Instant.now().toString());
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.addAll(header);
    return ResponseEntity.ok().headers(responseHeaders).body(response);
  }

  public static ResponseEntity<RestData<?>> error(HttpStatus status, String message) {
    RestData<?> response = new RestData<>(status.value(), false, message, null, Instant.now().toString());
    return new ResponseEntity<>(response, status);
  }

  public static ResponseEntity<RestData<?>> error(HttpStatus status, String message, Object data) {
    RestData<?> response = new RestData<>(status.value(), false, message, data, Instant.now().toString());
    return new ResponseEntity<>(response, status);
  }

  public static ResponseEntity<RestData<?>> error(HttpStatus status, Object message) {
    if (message instanceof String) {
      return error(status, (String) message);
    }
    RestData<?> response = new RestData<>(status.value(), false, "Error", message, Instant.now().toString());
    return new ResponseEntity<>(response, status);
  }

}
