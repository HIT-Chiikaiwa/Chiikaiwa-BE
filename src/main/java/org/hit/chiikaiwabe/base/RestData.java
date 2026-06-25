package org.hit.chiikaiwabe.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestData<T> {

  private int codeStatus;

  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  private String timestamp;

  public static RestData<?> error(int codeStatus, String message) {
    return new RestData<>(codeStatus, message, null, Instant.now().toString());
  }

}