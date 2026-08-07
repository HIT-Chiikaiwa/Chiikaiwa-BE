package org.hit.chiikaiwabe.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleRegistrationTempData implements Serializable {
    private String email;
    private String providerId;
}
