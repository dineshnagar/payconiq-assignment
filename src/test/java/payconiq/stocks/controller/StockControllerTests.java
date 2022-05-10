package payconiq.stocks.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.ObjectMapper;

import payconiq.stocks.exception.IncorrectRequestException;
import payconiq.stocks.exception.StockAlreadyExistsException;
import payconiq.stocks.exception.StockNotFoundException;
import payconiq.stocks.model.Stock;
import payconiq.stocks.repository.StockRepository;
import payconiq.stocks.request.NewStockRequest;
import payconiq.stocks.request.PriceUpdateRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class StockControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockRepository stockRepository;

    @Test
    void testGetAllStocks() throws Exception {
        mockMvc.perform(
                get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{\"id\":1,\"name\":\"London Stock\",\"currentPrice\":2.0,\"lastUpdate\":\"2019-12-11T22:58:34Z\"}," +
                                "{\"id\":2,\"name\":\"NewYork Stock\",\"currentPrice\":1.9,\"lastUpdate\":\"2019-12-11T23:59:56Z\"}" +
                                "]",
                        true
                ));
    }

    @Test
    void testGetStockById() throws Exception {
        mockMvc.perform(
                get("/api/stocks/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"id\":2,\"name\":\"NewYork Stock\",\"currentPrice\":1.9,\"lastUpdate\":\"2019-12-11T23:59:56Z\"}",
                        true
                ));
    }
    
    @Test
	void testDeleteStockById() throws Exception{
    	mockMvc.perform(delete("/api/stocks/2")).andExpect(status().isOk());
    }

    @Test
    void testGetIncorrectId() throws Exception {
        Exception exception = mockMvc.perform(
                get("/api/stocks/3"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException();
        assertException(exception, StockNotFoundException.class, "Stock with id 3 not found");
    }

    @Test
	void testDeleteIncorrectId() throws Exception{
    	Exception exception = mockMvc.perform(
    			delete("/api/stocks/4"))
    			.andExpect(status().isNotFound())
    			.andReturn()
    			.getResolvedException();
    	 assertException(exception, StockNotFoundException.class, "Stock with id 4 not found");
    }
    
    @Test
    void testGetUnparsableId() throws Exception {
        Exception exception = mockMvc.perform(
                get("/api/stocks/1_is_not_a_number"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();
        assertException(exception, MethodArgumentTypeMismatchException.class, "Failed to convert value of type 'java.lang.String' to required type 'long'; nested exception is java.lang.NumberFormatException: For input string: \"1_is_not_a_number\"");
    }

  

    @Test
    void testPostNewStock() throws Exception {
        NewStockRequest newStockRequest = new NewStockRequest();
        newStockRequest.setName("   Tokyo Stock    ");
        newStockRequest.setPrice(2d);

        Instant timestampBeforeInsertion = Instant.now();

        mockMvc.perform(
                post("/api/stocks")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(newStockRequest)))
                .andExpect(status().isCreated());

        Optional<Stock> expectedStock = stockRepository.findByName("Tokyo Stock");
        assertThat(expectedStock)
                .isPresent()
                .hasValueSatisfying(s -> Assertions.assertAll(
                        () -> assertThat(s.getId()).isNotNull(),
                        () -> assertThat(s.getName()).isEqualTo("Tokyo Stock"),
                        () -> assertThat(s.getCurrentPrice()).isEqualTo(2d),
                        () -> assertThat(s.getLastUpdate()).isAfter(timestampBeforeInsertion)
                        
                ));
    }

    @Test
    void testPostNewStockZeroPrice() throws Exception {
        NewStockRequest newStockRequest = new NewStockRequest();
        newStockRequest.setName("Hong-Kong Stock");
        newStockRequest.setPrice(0d);

        Exception exception = mockMvc.perform(
                post("/api/stocks")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(newStockRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        assertException(exception, IncorrectRequestException.class, "Stock price should be greater than zero");
    }

    @Test
    void testPostNewStockEmptyName() throws Exception {
        NewStockRequest newStockRequest = new NewStockRequest();
        newStockRequest.setName("     ");
        newStockRequest.setPrice(10d);

        Exception exception = mockMvc.perform(
                post("/api/stocks")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(newStockRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        assertException(exception, IncorrectRequestException.class, "Stock name can't be empty");
    }

    @Test
    void testPostNewStockAlreadyExists() throws Exception {
        NewStockRequest newStockRequest = new NewStockRequest();
        newStockRequest.setName(" London Stock  ");
        newStockRequest.setPrice(10d);

        Exception exception = mockMvc.perform(
                post("/api/stocks")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(newStockRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();

        assertException(exception, StockAlreadyExistsException.class, "Stock already exists with name: London Stock");
    }

    @Test
    void testUpdateNewPrice() throws Exception {
        double newPrice = 2.1;

        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
        priceUpdateRequest.setPrice(newPrice);

        Optional<Stock> expectedStock = stockRepository.findById(2L);
        assertThat(expectedStock).isPresent();
        Stock expectedStockValue = expectedStock.get();
        String originalStockName = expectedStockValue.getName();
        Instant timestampBeforeUpdate = expectedStockValue.getLastUpdate();
        mockMvc.perform(
        		patch("/api/stocks/2")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isOk());

        Optional<Stock> updatedStock = stockRepository.findById(2L);
        assertThat(updatedStock)
                .isPresent()
                .hasValueSatisfying(s -> Assertions.assertAll(
                        () -> assertThat(s.getName()).isEqualTo(originalStockName),
                        () -> assertThat(s.getCurrentPrice()).isEqualTo(newPrice),
                        () -> assertThat(s.getLastUpdate()).isAfter(timestampBeforeUpdate)
                        
                ));
    }

    @Test
    void testPutNewPriceIncorrectId() throws Exception {
        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
        priceUpdateRequest.setPrice(3d);

        Exception exception = mockMvc.perform(
        		patch("/api/stocks/6")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResolvedException();
        assertException(exception, StockNotFoundException.class, "Stock with id 6 not found");
    }

    @Test
    void testPutNewPriceNegative() throws Exception {
        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
        priceUpdateRequest.setPrice(-3d);

        Exception exception = mockMvc.perform(
                patch("/api/stocks/2")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();
        assertException(exception, IncorrectRequestException.class, "Stock price should be greater than zero");
    }

    @Test
    void testPutNewPriceNull() throws Exception {
        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();

        Exception exception = mockMvc.perform(
        		patch("/api/stocks/2")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResolvedException();
        assertException(exception, IncorrectRequestException.class, "Stock price should be greater than zero");
    }
    
    @Test
    void testGetAllStocksByPage() throws Exception {
        mockMvc.perform(
                get("/api/stocks/0/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[" +
                                "{\"id\":1,\"name\":\"London Stock\",\"currentPrice\":2.0,\"lastUpdate\":\"2019-12-11T22:58:34Z\"}," +
                                "{\"id\":2,\"name\":\"NewYork Stock\",\"currentPrice\":1.9,\"lastUpdate\":\"2019-12-11T23:59:56Z\"}" +
                                "]",
                        true
                ));
    }

    private static void assertException(Exception exception, Class<? extends Throwable> exceptionClass, String message) {
        assertThat(exception).isNotNull();
        assertThat(exception).isExactlyInstanceOf(exceptionClass);
        assertThat(exception.getMessage()).isEqualTo(message);
    }
}
