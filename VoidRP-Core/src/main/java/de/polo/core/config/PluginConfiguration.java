package de.polo.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "plugin")
public class PluginConfiguration {

    /**
     * A flag indicating whether debug mode is enabled for the plugin.
     *
     * <p>When set to {@code true}, the plugin will operate in debug mode, providing
     * more detailed logs and potentially enabling additional diagnostic features.</p>
     */
    private boolean debug;
}
