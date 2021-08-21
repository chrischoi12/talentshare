package talentshare;

import talentshare.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverConfirmAccepted_SendSms(@Payload ConfirmAccepted confirmAccepted){

        if(!confirmAccepted.validate()) return;

        System.out.println("\n\n##### listener SendSms : " + confirmAccepted.toJson() + "\n\n");


    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCanceled_SendSms(@Payload PaymentCanceled paymentCanceled){

        if(!paymentCanceled.validate()) return;

        System.out.println("\n\n##### listener SendSms : " + paymentCanceled.toJson() + "\n\n");

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
