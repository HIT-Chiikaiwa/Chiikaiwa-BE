package org.hit.chiikaiwabe.service.impl;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.constant.ErrorMessage; // Nhớ import
import org.hit.chiikaiwabe.domain.dto.common.DataMailDto;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.service.OtpService;
import org.hit.chiikaiwabe.util.SendMailUtil;
import org.springframework.context.MessageSource; // Nhớ import
import org.springframework.context.i18n.LocaleContextHolder; // Nhớ import
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final SendMailUtil sendMailUtil;
    private final MessageSource messageSource;

    @Override
    public void generateAndSendOtp(String email) {
        String otpCode = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set("OTP:" + email, otpCode, 5, TimeUnit.MINUTES);

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
            throw new InvalidException(ErrorMessage.Mail.ERR_SEND_MAIL_FAILED);
        }
    }

    @Override
    public boolean validateOtp(String email, String otpCode) {
        String cacheOtp = redisTemplate.opsForValue().get("OTP:" + email);
        if (cacheOtp != null && cacheOtp.equals(otpCode)) {
            redisTemplate.delete("OTP:" + email);
            return true;
        }
        return false;
    }
}