package de.polo.core;

import de.polo.core.config.PluginConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableConfigurationProperties({
        PluginConfiguration.class
})
public class VoidSpringApplication {
}
