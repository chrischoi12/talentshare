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
    @Autowired ConfirmationRepository confirmationRepository;
    @Autowired CancellationRepository cancellationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_AcceptConfirm(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        System.out.println("\n\n##### listener AcceptConfirm : " + paymentApproved.toJson() + "\n\n");


        Confirmation confirmation = new Confirmation();
        confirmation.setStatus("Confirmation Complete");
        confirmation.setOrderId(paymentApproved.getOrderId());
        confirmation.setId(paymentApproved.getOrderId());
        confirmationRepository.save(confirmation);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_CancelConfirm(@Payload OrderCanceled orderCanceled){

        if(!orderCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelConfirm : " + orderCanceled.toJson() + "\n\n");

        Cancellation cancellation = new Cancellation();
        cancellation.setOrderId(orderCanceled.getId());
        cancellation.setStatus("Confirmation Canceled");
        cancellationRepository.save(cancellation);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
