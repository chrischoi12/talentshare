package talentshare;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="Mypage_table")
public class Mypage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long orderId;
        private String talentCategory;
        private Long confirmationId;
        private Long cancellationId;
        private String name;
        private String status;


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
        public Long getConfirmationId() {
            return confirmationId;
        }

        public void setConfirmationId(Long confirmationId) {
            this.confirmationId = confirmationId;
        }
        public Long getCancellationId() {
            return cancellationId;
        }

        public void setCancellationId(Long cancellationId) {
            this.cancellationId = cancellationId;
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
        
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

}
