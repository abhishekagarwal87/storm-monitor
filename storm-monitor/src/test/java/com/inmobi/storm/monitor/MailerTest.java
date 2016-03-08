package com.inmobi.storm.monitor;

import org.apache.commons.mail.EmailException;
import org.testng.annotations.Test;

public class MailerTest {

    @Test
    public void test() throws EmailException {
        StormMonitorAppConfiguration.EmailConfig
            emailConfig =
            new StormMonitorAppConfiguration.EmailConfig("abhishek.agarwal@inmobi.com", "abhishek.agarwal@inmobi.com",
                                                         "localhost");
        Mailer mailer = new Mailer(emailConfig);
        mailer.sendMail("Test", "Message");
    }

}
