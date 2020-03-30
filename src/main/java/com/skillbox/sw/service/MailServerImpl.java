package com.skillbox.sw.service;

import com.skillbox.sw.config.Mail;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service("mailServer")
public class MailServerImpl implements MailService {


  private JavaMailSender mailSender;

  @Autowired
  public MailServerImpl(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void sendMail(Mail mail) {
    MimeMessage mimeMessage = mailSender.createMimeMessage();

    try {
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
      mimeMessageHelper.setSubject(mail.getSubject());
      // TODO: replace social-network.com to real url address
      mimeMessageHelper.setFrom(new InternetAddress(mail.getFrom(), "social-network.com"));
      mimeMessageHelper.setTo(mail.getTo());
      mimeMessageHelper.setText(mail.getContent());
      mailSender.send(mimeMessageHelper.getMimeMessage());

    } catch (MessagingException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}