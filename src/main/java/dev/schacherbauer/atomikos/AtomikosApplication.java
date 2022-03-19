package dev.schacherbauer.atomikos;

import static javax.persistence.spi.PersistenceUnitTransactionType.JTA;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;
import static org.eclipse.persistence.config.PersistenceUnitProperties.WEAVING;

import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import javax.sql.DataSource;
import javax.transaction.SystemException;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.atomikos.eclipselink.platform.AtomikosPlatform;
import com.atomikos.icatch.jta.UserTransactionManager;

import dev.schacherbauer.atomikos.inventory.Inventory;
import dev.schacherbauer.atomikos.inventory.InventoryRepository;
import dev.schacherbauer.atomikos.order.Order;
import dev.schacherbauer.atomikos.order.OrderRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableTransactionManagement
@SpringBootApplication
public class AtomikosApplication {

	@Autowired
	private InventoryRepository inventoryRepository;
	@Autowired
	private OrderRepository orderRepository;

	public static void main(String[] args) {
		SpringApplication.run(AtomikosApplication.class, args);
	}

	@Bean
	CommandLineRunner runner() {
		return args -> {
			var balanceBefore = inventoryRepository.findById("A").get().getBalance();
			log.info("Balance of A before TA: {}", balanceBefore);
			log.info("OrderCount before TA: {}", orderRepository.findAll().size());
			placeOrder("A", 1);
			var balanceAfter = inventoryRepository.findById("A").get().getBalance();
			log.info("Balance of A after TA: {}", balanceAfter);
			log.info("OrderCount after TA: {}", orderRepository.findAll().size());
		};
	}

	@Transactional(rollbackFor = Exception.class)
	public void placeOrder(String productId, int amount) throws SQLException {
		String orderId = UUID.randomUUID().toString();
		Inventory inventory = inventoryRepository.findById(productId).get();
		inventory.setBalance(inventory.getBalance() - amount);
		inventoryRepository.save(inventory);
		Order order = new Order();
		order.setOrderId(orderId);
		order.setProductId(productId);
		order.setAmount(Long.valueOf(amount));
		orderRepository.save(order);
	}

	@Bean(initMethod = "init", destroyMethod = "close")
	public UserTransactionManager userTransactionManager() throws SystemException {
		UserTransactionManager userTransactionManager = new UserTransactionManager();
		userTransactionManager.setTransactionTimeout(300);
		userTransactionManager.setForceShutdown(true);
		return userTransactionManager;
	}

	@Bean
	public JtaTransactionManager transactionManager() throws SystemException {
		JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
		jtaTransactionManager.setTransactionManager(userTransactionManager());
		jtaTransactionManager.setUserTransaction(userTransactionManager());
		return jtaTransactionManager;
	}

	public static LocalContainerEntityManagerFactoryBean eclipselinkEntityManagerFactory(String persistenceUnitName,
			DataSource ds, String packagesToScan) {
		var emfb = new LocalContainerEntityManagerFactoryBean();
		var jpaVendorAdapter = new EclipseLinkJpaVendorAdapter();
		jpaVendorAdapter.setGenerateDdl(true);
		emfb.setJpaVendorAdapter(jpaVendorAdapter);
		emfb.setDataSource(ds);
		emfb.setJtaDataSource(ds);
		emfb.setPersistenceUnitName(persistenceUnitName);
		emfb.setPackagesToScan(packagesToScan);
		Properties jpaProperties = new Properties();
		jpaProperties.put(TARGET_SERVER, AtomikosPlatform.class.getName());
		jpaProperties.put(WEAVING, "false");
		jpaProperties.put(TRANSACTION_TYPE, JTA.toString());

		emfb.setJpaProperties(jpaProperties);
		emfb.afterPropertiesSet();
		return emfb;
	}

	public static AtomikosDataSourceBean h2XaDataSource(String url, String uniqueResourceName) {
		var xaDs = DataSourceBuilder.create().url(url).username("sa").password("sa").type(JdbcDataSource.class)
				.driverClassName("org.h2.Driver").build();
		var ads = new AtomikosDataSourceBean();
		ads.setXaDataSource(xaDs);
		ads.setUniqueResourceName(uniqueResourceName);
		return ads;
	}

}
