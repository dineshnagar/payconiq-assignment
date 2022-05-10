package payconiq.stocks.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.istack.Nullable;

import payconiq.stocks.exception.IncorrectRequestException;
import payconiq.stocks.exception.StockAlreadyExistsException;
import payconiq.stocks.exception.StockNotFoundException;
import payconiq.stocks.model.Stock;
import payconiq.stocks.repository.StockRepository;
import payconiq.stocks.request.NewStockRequest;

/**
 * Service to perform business logic on {@link Stock} entities.
 */
@Service
public class StockService {

	@Autowired
	private StockRepository stockRepository;

	/**
	 * Returns list of all {@link Stock}s.
	 *
	 * @return list of all {@link Stock}s.
	 */
	@Transactional
	@NonNull
	public Collection<Stock> getAllStocks() {
		return stockRepository.findAll();
	}

	/**
	 * Returns {@link Stock} by its id.
	 *
	 * @param id - id of stock to lookup.
	 * @return {@link Stock} by its id.
	 * @throws StockNotFoundException when there is no stock with such id.
	 */
	@Transactional
	@NonNull
	public Stock lookupStock(long id) {
		return stockRepository.findById(id)
				.orElseThrow(() -> new StockNotFoundException("Stock with id " + id + " not found"));
	}

	/**
	 * Updates a price of a given stock.
	 *
	 * @param id    - id of stock to update.
	 * @param price - price to update stock with.
	 * @return update result response.
	 * @throws StockNotFoundException    when there is no stock with such id.
	 * @throws IncorrectRequestException when price is 0 or below.
	 */
	@Transactional
	@NonNull
	public Stock updateStockPrice(long id, @Nullable Double price) {
		validatePrice(price);
		Stock stock = lookupStock(id);
		stock.setCurrentPrice(price);
		return saveStock(stock);
	}

	/**
	 * Adding new stock by request.
	 *
	 * @param newStockRequest - {@link NewStockRequest} of new stock to add.
	 * @return adding result response.
	 * @throws IncorrectRequestException                             when price is 0
	 *                                                               or below or
	 *                                                               stock name is
	 *                                                               empty.
	 * @throws payconiq.stocks.exception.StockAlreadyExistsException when stock with
	 *                                                               such name
	 *                                                               already exists.
	 */
	@Transactional
	@NonNull
	public Stock addNewStock(@NonNull NewStockRequest newStockRequest) {
		Stock newStock = new Stock();
		newStock.setCurrentPrice(validatePrice(newStockRequest.getPrice()));
		newStock.setName(validateName(newStockRequest.getName()));
		return saveStock(newStock);
	}

	@Transactional
	@NonNull
	public void deleteStock(@NonNull Long id) {
		Stock stock = stockRepository.findById(id)
				.orElseThrow(() -> new StockNotFoundException("Stock with id " + id + " not found"));
		stockRepository.delete(stock);

	}

	/**
	 * Saves a {@link Stock} and updates its
	 *
	 * @param stock - {@link Stock} to save.
	 * @return saved Stock.
	 */
	@Transactional
	@NonNull
	private Stock saveStock(@NonNull Stock stock) {
		Instant lastUpdate = Instant.now();
		stock.setLastUpdate(lastUpdate);

		Stock savedStock = stockRepository.save(stock);

		return savedStock;
	}

	@Transactional
	@NonNull
	private void deleteStock(@NonNull Stock stock) {
		stockRepository.delete(stock);
	}

	/**
	 * Validates that price is greater than 0.
	 *
	 * @param price - price to validate.
	 * @return input price.
	 * @throws IncorrectRequestException when price is 0 or less.
	 */
	private static double validatePrice(@Nullable Double price) {
		if (price == null || price <= 0) {
			throw new IncorrectRequestException("Stock price should be greater than zero");
		}
		return price;
	}

	/**
	 * Validates that name is: - not null - not empty - unique among all Stocks.
	 *
	 * @param name - name to validate.
	 * @return input name without leading and trailing whitespaces.
	 */
	@NonNull
	private String validateName(@Nullable String name) {
		if (name == null || name.isEmpty() || name.isBlank()) {
			throw new IncorrectRequestException("Stock name can't be empty");
		}
		String cleanName = name.trim();
		Optional<Stock> actualStock = stockRepository.findByName(cleanName);
		if (actualStock.isPresent()) {
			throw new StockAlreadyExistsException("Stock already exists with name: " + cleanName);
		}
		return cleanName;
	}

	public List<Stock> findPaginated(int pageNo, int pageSize) {
		Pageable paging = PageRequest.of(pageNo, pageSize);
		Page<Stock> pageResult = stockRepository.findAll(paging);
		return pageResult.toList();
		
		
	}
}
