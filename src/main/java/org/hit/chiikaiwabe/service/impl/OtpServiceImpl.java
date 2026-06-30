package org.hit.chiikaiwabe.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.common.DataMailDto;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.service.OtpService;
import org.hit.chiikaiwabe.util.SendMailUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final SendMailUtil sendMailUtil;
    private final MessageSource messageSource;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtp(String email) {
        int otp = secureRandom.nextInt(90000) + 10000;
        String otpCode = String.valueOf(otp);

        redisTemplate.opsForValue().set("OTP:" + email, otpCode, 5, TimeUnit.MINUTES);
        return otpCode;
    }

    @Override
    public void sendOtp(String email, String otpCode) {
        try {
            DataMailDto dataMail = new DataMailDto();
            dataMail.setTo(email);

            String subject = messageSource.getMessage("mail.otp.subject", null, LocaleContextHolder.getLocale());
            dataMail.setSubject(subject);

            Map<String, Object> properties = new HashMap<>();
            properties.put("otp", otpCode);
            properties.put("email", email);
            dataMail.setProperties(properties);

            sendMailUtil.sendEmailWithHTML(dataMail, "otp-template");
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage(), e);
            throw new InvalidException(ErrorMessage.Mail.ERR_SEND_MAIL_FAILED);
        }
    }


    @Override
    public boolean validateOtp(String email, String otpCode) {
        String key = "OTP:" + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp != null && storedOtp.equals(otpCode)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}