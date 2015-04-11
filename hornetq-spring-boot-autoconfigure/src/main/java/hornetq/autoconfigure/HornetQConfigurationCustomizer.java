package hornetq.autoconfigure;

import org.hornetq.core.config.Configuration;
import org.hornetq.jms.server.embedded.EmbeddedJMS;

/**
 * Callback interface that can be implemented by beans wishing to customize the HornetQ
 * JMS server {@link Configuration} before it is used by an auto-configured
 * {@link EmbeddedJMS} instance.
 *
 * @author Phillip Webb
 * @see hornetq.autoconfigure.HornetQAutoConfiguration
 */
public interface HornetQConfigurationCustomizer {

	/**
	 * Customize the configuration.
	 * @param configuration the configuration to customize
	 */
	void customize(Configuration configuration);

}