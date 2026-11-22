package pl.tijo.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.tijo.exchange.domain.Company;
import pl.tijo.exchange.domain.Trade;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findTop20ByCompanyOrderByExecutedAtDescIdDesc(Company company);

    List<Trade> findTop100ByOrderByExecutedAtDescIdDesc();

    List<Trade> findByCompanyOrderByExecutedAtAscIdAsc(Company company);
}
