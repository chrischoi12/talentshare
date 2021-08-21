package talentshare;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="paymentHist", path="paymentHist")
public interface PaymentHistRepository extends PagingAndSortingRepository<PaymentHist, Long>{


}
