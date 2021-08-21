package talentshare;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="confirmations", path="confirmations")
public interface ConfirmationRepository extends PagingAndSortingRepository<Confirmation, Long>{


}
