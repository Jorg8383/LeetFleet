
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessController {

    private final AccessRepository accessRepository;

    public AccessController(AccessRepository accessRepository) {
        this.accessRepository = accessRepository;
    }

    @GetMapping(value = "access")
    public List<AccessEntity> getAccessList() {
        List<AccessEntity> accessEntityList = (List<AccessEntity>) accessRepository.findAll();
        return accessEntityList;
    }

    @PutMapping(value = "/{location}")
    public void updateAccessCountItem(@PathVariable("location") String location) {
        AccessEntity accessEntity = getAccessCountEntity(location);
        if (accessEntity == null) {
            accessEntity = new AccessEntity(location);
        }

        accessEntity.setAccessCount(accessEntity.accessCount + 1);
        accessRepository.save(accessEntity);
    }

    private AccessEntity getAccessCountEntity(String location) {
        return Optional.ofNullable(accessRepository.findById(location)).get().orElse(null);
    }
}
