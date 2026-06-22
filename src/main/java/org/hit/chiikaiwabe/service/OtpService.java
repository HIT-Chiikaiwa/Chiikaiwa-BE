package org.hit.chiikaiwabe.service;

public interface OtpService {
    void generateAndSendOtp(String email);
    boolean validateOtp(String email, String inputOtp);
}
