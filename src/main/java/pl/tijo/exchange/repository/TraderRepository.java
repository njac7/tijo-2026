package pl.tijo.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.tijo.exchange.domain.Trader;

import java.util.List;

public interface TraderRepository extends JpaRepository<Trader, Long> {

    List<Trader> findAllByOrderByIdAsc();
}
