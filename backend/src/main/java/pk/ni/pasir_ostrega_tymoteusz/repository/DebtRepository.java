package pk.ni.pasir_ostrega_tymoteusz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pk.ni.pasir_ostrega_tymoteusz.model.Debt;
import java.util.List;

public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByGroupId(Long groupId);
    void deleteByGroupId(Long groupId);
}