package org.hit.chiikaiwabe.util;

import org.hit.chiikaiwabe.domain.dto.common.DataMailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendMailUtil {

  private final TemplateEngine templateEngine;

  @Value("${brevo.api-key}")
  private String brevoApiKey;

  @Value("${brevo.sender-email}")
  private String senderEmail;

  @Value("${brevo.sender-name:Chiikaiwa}")
  private String senderName;

  private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

  /**
   * Gửi mail với file html qua Brevo HTTP API
   * @param mail Thông tin của mail cần gửi
   * @param template Tên file html trong folder resources/template
   */
  public void sendEmailWithHTML(DataMailDto mail, String template) throws Exception {
    Context context = new Context();
    context.setVariables(mail.getProperties());
    String htmlContent = templateEngine.process(template, context);

    sendViaBrevo(mail.getTo(), mail.getSubject(), htmlContent);
  }

  /**
   * Gửi mail qua Brevo HTTP API
   */
  private void sendViaBrevo(String to, String subject, String htmlContent) throws Exception {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("api-key", brevoApiKey);

    Map<String, Object> sender = new HashMap<>();
    sender.put("name", senderName);
    sender.put("email", senderEmail);

    Map<String, Object> recipient = new HashMap<>();
    recipient.put("email", to);

    Map<String, Object> body = new HashMap<>();
    body.put("sender", sender);
    body.put("to", List.of(recipient));
    body.put("subject", subject);
    body.put("htmlContent", htmlContent);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    ResponseEntity<String> response = restTemplate.exchange(
            BREVO_API_URL, HttpMethod.POST, request, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      log.error("Brevo API error: {}", response.getBody());
      throw new RuntimeException("Brevo API returned: " + response.getStatusCode());
    }

    log.info("Email sent via Brevo to {}", to);
  }

}
