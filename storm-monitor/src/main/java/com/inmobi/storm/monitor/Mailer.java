package com.inmobi.storm.monitor;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Mailer {

    private StormMonitorAppConfiguration.EmailConfig emailConfig;

    public Mailer(StormMonitorAppConfiguration.EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    public void sendMail(String subject, String message) throws EmailException {
        Email email = new HtmlEmail();
        email.setFrom(emailConfig.getFrom());
        email.addTo(emailConfig.getTo().split(","));
        email.setMsg(message);
        email.setSubject(subject);
        email.setHostName(emailConfig.getHostName());
        String result = email.send();
        log.info(result);
    }
}
