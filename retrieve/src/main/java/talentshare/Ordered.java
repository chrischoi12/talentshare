package talentshare;

public class Ordered extends AbstractEvent {

    private Long id;
    private String name;
    private String talentCategory;
    private Long cardNo;
    private String status;

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