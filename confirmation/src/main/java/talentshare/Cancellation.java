package talentshare;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Cancellation_table")
public class Cancellation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String talentCategory;
    private String status;

    @PostPersist
    public void onPostPersist(){
        ConfirmCanceled confirmCanceled = new ConfirmCanceled();
        BeanUtils.copyProperties(this, confirmCanceled);
        confirmCanceled.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getTalentCategory() {
        return talentCategory;
    }

    public void setTalentCategory (String talentCategory) {
        this.talentCategory = talentCategory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}