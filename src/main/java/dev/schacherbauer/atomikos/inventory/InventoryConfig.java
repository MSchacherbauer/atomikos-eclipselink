package dev.schacherbauer.atomikos.inventory;

import static dev.schacherbauer.atomikos.AtomikosApplication.eclipselinkEntityManagerFactory;
import static dev.schacherbauer.atomikos.AtomikosApplication.h2XaDataSource;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
@EnableJpaRepositories(basePackages = InventoryConfig.INVENTORY_PACKAGE, entityManagerFactoryRef = InventoryConfig.INVENTORY_ENTITY_MANAGER)
public class InventoryConfig {

	public static final String INVENTORY_PACKAGE = "dev.schacherbauer.atomikos.inventory";
	public static final String INVENTORY_ENTITY_MANAGER = "inventoryEntityManager";
	private static final String INVENTORY_DS = "inventoryDs";
	private static final String URL = "jdbc:h2:mem:inventoryDs;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
	
	@Bean(name = INVENTORY_ENTITY_MANAGER)
	public LocalContainerEntityManagerFactoryBean inventoryEntityManager() {
		return eclipselinkEntityManagerFactory(INVENTORY_DS,inventoryDs(), INVENTORY_PACKAGE);
	}
	
	@Bean
	public DataSource inventoryDs() {
		return h2XaDataSource(URL, INVENTORY_DS);
	}
}