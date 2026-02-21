package de.polo.core.infrastructure.persistence;

import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Creates and holds the Hibernate {@link SessionFactory}.
 *
 * <p>Reuses the existing {@link HikariDataSource} from
 * {@link de.polo.core.database.impl.CoreDatabase} so that both the legacy
 * JDBC layer and the new Hibernate layer share the same connection pool.
 *
 * <p>hbm2ddl.auto is set to {@code validate} – Hibernate will verify that
 * the mapped columns exist but will never alter the schema.
 */
public final class HibernateConfig {

    private final StandardServiceRegistry serviceRegistry;
    private final SessionFactory sessionFactory;

    public HibernateConfig(HikariDataSource dataSource) {
        serviceRegistry = new StandardServiceRegistryBuilder()
                // Share the same HikariCP pool with the existing JDBC layer.
                .applySetting("hibernate.connection.datasource", dataSource)
                // Auto-detect dialect from the driver; explicit for clarity.
                .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
                // Never touch the schema – only validate existing columns.
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                // Disable second-level cache – we use Caffeine instead.
                .applySetting("hibernate.cache.use_second_level_cache", "false")
                // Minimise JDBC round-trips for batch updates.
                .applySetting("hibernate.jdbc.batch_size", "30")
                .applySetting("hibernate.order_updates", "true")
                .build();

        sessionFactory = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(PlayerEntity.class)
                .buildMetadata()
                .buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        StandardServiceRegistryBuilder.destroy(serviceRegistry);
    }
}
