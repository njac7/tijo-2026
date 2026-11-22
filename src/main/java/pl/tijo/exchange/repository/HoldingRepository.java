package pl.tijo.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.tijo.exchange.domain.Company;
import pl.tijo.exchange.domain.Holding;
import pl.tijo.exchange.domain.Trader;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByTraderAndCompany(Trader trader, Company company);

    List<Holding> findByTraderOrderByCompanyTickerAsc(Trader trader);
}
