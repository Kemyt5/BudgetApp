package pk.ni.pasir_ostrega_tymoteusz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.ni.pasir_ostrega_tymoteusz.model.Group;
import pk.ni.pasir_ostrega_tymoteusz.model.User;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMemberships_User(User user);
}