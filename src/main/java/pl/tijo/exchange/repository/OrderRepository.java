package pl.tijo.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.tijo.exchange.domain.Company;
import pl.tijo.exchange.domain.Order;
import pl.tijo.exchange.domain.OrderSide;
import pl.tijo.exchange.domain.OrderStatus;
import pl.tijo.exchange.domain.Trader;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCompanyAndSideAndStatusIn(Company company, OrderSide side, Collection<OrderStatus> statuses);

    List<Order> findByTraderOrderByCreatedAtDescIdDesc(Trader trader);
}
