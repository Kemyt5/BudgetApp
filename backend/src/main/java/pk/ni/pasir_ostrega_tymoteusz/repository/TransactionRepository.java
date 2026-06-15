package pk.ni.pasir_ostrega_tymoteusz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.ni.pasir_ostrega_tymoteusz.model.Transaction;
import pk.ni.pasir_ostrega_tymoteusz.model.User;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUser(User user);
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserAndTimestampGreaterThanEqual(User user, LocalDateTime timestamp);
}