package com.btgpactual.fondosapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class NotificationService {

    private final SnsClient sns;
    private final SesClient ses;
    private final String snsTopicArn;
    private final String sesIdentityEmail;

    public NotificationService(SnsClient sns, SesClient ses,
                               @Value("${aws.sns-topic-arn}") String snsTopicArn,
                               @Value("${aws.ses-identity-email}") String sesIdentityEmail) {
        this.sns = sns;
        this.ses = ses;
        this.snsTopicArn = snsTopicArn;
        this.sesIdentityEmail = sesIdentityEmail;
    }

    public void sendSms(String phoneNumber, String message) {
        PublishRequest req = PublishRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .build();
        sns.publish(req);
    }

    public void sendEmail(String toEmail, String subject, String htmlBody) {
        Destination dest = Destination.builder().toAddresses(toEmail).build();
        Content contentSubj = Content.builder().data(subject).build();
        Body body = Body.builder().html(Content.builder().data(htmlBody).build()).build();
        Message message = Message.builder().subject(contentSubj).body(body).build();
        SendEmailRequest req = SendEmailRequest.builder()
                .destination(dest)
                .message(message)
                .source(sesIdentityEmail)
                .build();
        ses.sendEmail(req);
    }

    public void publishToTopic(String message) {
        if (snsTopicArn == null || snsTopicArn.isBlank()) return;
        PublishRequest req = PublishRequest.builder()
                .topicArn(snsTopicArn)
                .message(message)
                .build();
        sns.publish(req);
    }
}
