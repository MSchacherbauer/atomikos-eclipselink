package dev.schacherbauer.atomikos.order;

import static dev.schacherbauer.atomikos.AtomikosApplication.eclipselinkEntityManagerFactory;
import static dev.schacherbauer.atomikos.AtomikosApplication.h2XaDataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
@EnableJpaRepositories(basePackages = OrderConfig.ORDER_PACKAGE,entityManagerFactoryRef = OrderConfig.ORDER_ENTITY_MANAGER)
public class OrderConfig {
	public static final String ORDER_PACKAGE="dev.schacherbauer.atomikos.order";
	public static final String ORDER_ENTITY_MANAGER = "orderEntityManager";
	private static final String ORDER_DS = "orderDs";
	private static final String URL = "jdbc:h2:mem:orderDs;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

	@Bean(name = ORDER_ENTITY_MANAGER)
	@Primary
	public LocalContainerEntityManagerFactoryBean orderEntityManager() {
		var dataSource=h2XaDataSource(URL,ORDER_DS);
		return eclipselinkEntityManagerFactory(ORDER_DS,dataSource, ORDER_PACKAGE);
	}
}