package pl.tijo.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.tijo.exchange.domain.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByTicker(String ticker);

    List<Company> findAllByOrderByTickerAsc();
}
