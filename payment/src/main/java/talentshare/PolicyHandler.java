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
    @Autowired PaymentHistRepository paymentHistRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverConfirmCanceled_CancelPayment(@Payload ConfirmCanceled confirmCanceled){

        if(!confirmCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelPayment : " + confirmCanceled.toJson() + "\n\n");

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
