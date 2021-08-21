package talentshare;

public class ConfirmCanceled extends AbstractEvent {

    private Long id;
    private Long orderId;
    private String talentCategory;
    private String status;

    public ConfirmCanceled(){
        super();
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
