package talentshare;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private String talentCategory;
    private Long cardNo;
    private String status;

    @PostPersist
    public void onPostPersist(){
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        talentshare.external.PaymentHist paymentHist = new talentshare.external.PaymentHist();
        // mappings goes here
        //PaymentHist payment = new PaymentHist();
        System.out.println("this.id() : " + this.id);
        paymentHist.setOrderId(this.id);
        paymentHist.setStatus("Order Submitted");
        paymentHist.setCardNo(this.cardNo);      
        
        
        OrderApplication.applicationContext.getBean(talentshare.external.PaymentHistService.class)
            .pay(paymentHist);

    }
    @PostUpdate
    public void onPostUpdate(){
        OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();

    }
    @PrePersist
    public void onPrePersist(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTalentCategory() {
        return talentCategory;
    }

    public void setTalentCategory (String talentCategory) {
        this.talentCategory = talentCategory;
    }
    
    public Long getCardNo() {
        return cardNo;
    }

    public void setCardNo(Long cardNo) {
        this.cardNo = cardNo;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}