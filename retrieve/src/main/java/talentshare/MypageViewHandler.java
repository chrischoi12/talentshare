package talentshare;

import talentshare.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MypageViewHandler {


    @Autowired
    private MypageRepository retrieveRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_CREATE_1 (@Payload Ordered ordered) {
        try {

            if (!ordered.validate()) return;
            Mypage retrieve = new Mypage();
            retrieve.setOrderId(ordered.getId());
            retrieve.setName(ordered.getName());
            retrieve.setTalentCategory(ordered.getTalentCategory());
            retrieve.setStatus(ordered.getStatus());
            retrieveRepository.save(retrieve);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentApproved_then_UPDATE_1(@Payload PaymentApproved paymentApproved) {
        try {
            if (!paymentApproved.validate()) return;

                    List<Mypage> retrieveList = retrieveRepository.findByOrderId(paymentApproved.getOrderId());
                    for(Mypage retrieve : retrieveList){
                    retrieve.setStatus(paymentApproved.getStatus());
                retrieveRepository.save(retrieve);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenConfirmAccepted_then_UPDATE_2(@Payload ConfirmAccepted confirmAccepted) {
        try {
            if (!confirmAccepted.validate()) return;
                    List<Mypage> retrieveList = retrieveRepository.findByOrderId(confirmAccepted.getOrderId());
                    for(Mypage retrieve : retrieveList){
                    retrieve.setConfirmationId(confirmAccepted.getId());
                    retrieve.setStatus(confirmAccepted.getStatus());
                retrieveRepository.save(retrieve);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCanceled_then_UPDATE_3(@Payload OrderCanceled orderCanceled) {
        try {
            if (!orderCanceled.validate()) return;
                    List<Mypage> retrieveList = retrieveRepository.findByOrderId(orderCanceled.getId());
                    for(Mypage retrieve : retrieveList){
                    retrieve.setStatus(orderCanceled.getStatus());
                retrieveRepository.save(retrieve);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenConfirmCanceled_then_UPDATE_4(@Payload ConfirmCanceled confirmCanceled) {
        try {
            if (!confirmCanceled.validate()) return;
                    List<Mypage> retrieveList = retrieveRepository.findByOrderId(confirmCanceled.getOrderId());
                    for(Mypage retrieve : retrieveList){
                    retrieve.setOrderId(confirmCanceled.getOrderId());
                    retrieve.setStatus(confirmCanceled.getStatus());
                retrieveRepository.save(retrieve);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCanceled_then_UPDATE_5(@Payload PaymentCanceled paymentCanceled) {
        try {
            if (!paymentCanceled.validate()) return;
                    List<Mypage> retrieveList = retrieveRepository.findByOrderId(paymentCanceled.getOrderId());
                    for(Mypage retrieve : retrieveList){
                    retrieve.setStatus(paymentCanceled.getStatus());
                retrieveRepository.save(retrieve);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}