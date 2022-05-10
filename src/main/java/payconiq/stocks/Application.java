package payconiq.stocks;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import payconiq.stocks.model.Stock;
import payconiq.stocks.repository.StockRepository;

/**
 * Stocks Application main class. Is driven by Spring Boot 2.
 */
@SpringBootApplication
public class Application {

    /**
     * Application executing class.
     *
     * @param args - args which can be consumed by Spring Boot 2.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private StockRepository stockRepository;

   
    /**
     * Initial state of the application on startup.
     * Is used for tests as well, so, when it will be decided
     * to remove it from here, it should be moved to test scope bean.
     */
    @PostConstruct
    void prepareStocks() {
       
        Instant stock1Update2 = LocalDateTime.of(2019, Month.DECEMBER, 11, 22, 58, 34).toInstant(ZoneOffset.UTC);

        Stock stock1 = new Stock();
        stock1.setName("London Stock");
        stock1.setCurrentPrice(2d);
        stock1.setLastUpdate(stock1Update2);
        stockRepository.saveAndFlush(stock1);

        

       
        Instant stock2Update3 = LocalDateTime.of(2019, Month.DECEMBER, 11, 23, 59, 56).toInstant(ZoneOffset.UTC);

        Stock stock2 = new Stock();
        stock2.setName("NewYork Stock");
        stock2.setCurrentPrice(1.9);
        stock2.setLastUpdate(stock2Update3);
        stockRepository.saveAndFlush(stock2);

    }
}
