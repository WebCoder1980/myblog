package org.myblog.users.exception;

import lombok.Getter;
import lombok.Setter;

public class RestIllegalArgumentException extends RuntimeException {
  @Getter
  @Setter
  private String field;

  public RestIllegalArgumentException(String message) {
    super(message);

    field = "general";
  }

  public RestIllegalArgumentException(String field, String message) {
    super(message);

    this.field = field;
  }
}
