package hornetq.autoconfigure;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.ConnectionFactory;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.config.impl.TopicConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass({ConnectionFactory.class, HornetQJMSClient.class})
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties(HornetQProperties.class)
public class HornetQAutoConfiguration {

	private static final String EMBEDDED_JMS_CLASS = "org.hornetq.jms.server.embedded.EmbeddedJMS";

	@Autowired
	private HornetQProperties properties;

	@Bean
	public ConnectionFactory connectionFactory() {
		HornetQMode mode = this.properties.getMode();
		if (mode == null) {
			mode = deduceMode();
		}
		if (mode == HornetQMode.EMBEDDED) {
			return createEmbeddedConnectionFactory();
		}
		else {
			return createNettyConnectionFactory();
		}
	}

	private HornetQMode deduceMode() {
		if (this.properties.getEmbedded().isEnabled()
				&& ClassUtils.isPresent(EMBEDDED_JMS_CLASS, null)) {
			return HornetQMode.EMBEDDED;
		}
		return HornetQMode.NATIVE;
	}

	private ConnectionFactory createEmbeddedConnectionFactory() {
		try {
			TransportConfiguration transportConfiguration = new TransportConfiguration(
					InVMConnectorFactory.class.getName());
			ServerLocator serviceLocator = HornetQClient
					.createServerLocatorWithoutHA(transportConfiguration);
			return new HornetQConnectionFactory(serviceLocator);
		}
		catch (NoClassDefFoundError ex) {
			throw new IllegalStateException("Unable to create embedded "
					+ "HornetQ connection, ensure that the hornetq-jms-server.jar "
					+ "is the classpath", ex);
		}
	}

	private ConnectionFactory createNettyConnectionFactory() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(TransportConstants.HOST_PROP_NAME, this.properties.getHost());
		params.put(TransportConstants.PORT_PROP_NAME, this.properties.getPort());
		TransportConfiguration transportConfiguration = new TransportConfiguration(
				NettyConnectorFactory.class.getName(), params);
		return HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF,
				transportConfiguration);
	}

	/**
	 * Configuration used to create the embedded hornet server.
	 */
	@Configuration
	@ConditionalOnClass(name = EMBEDDED_JMS_CLASS)
	@ConditionalOnProperty(prefix = "hornetq.embedded", value = "enabled", havingValue = "true", matchIfMissing = true)
	static class EmbeddedServerConfiguration {

		@Autowired
		private HornetQProperties properties;

		@Autowired(required = false)
		private List<JMSQueueConfiguration> queuesConfiguration;

		@Autowired(required = false)
		private List<TopicConfiguration> topicsConfiguration;

		@Autowired(required = false)
		private List<HornetQConfigurationCustomizer> configurationCustomizers;

		@Bean
		@ConditionalOnMissingBean
		public org.hornetq.core.config.Configuration hornetQConfiguration() {
			return new HornetQEmbeddedConfigurationFactory(this.properties)
					.createConfiguration();
		}

		@Bean(initMethod = "start", destroyMethod = "stop")
		@ConditionalOnMissingBean
		public EmbeddedJMS hornetQServer(
				org.hornetq.core.config.Configuration configuration,
				JMSConfiguration jmsConfiguration) {
			EmbeddedJMS server = new EmbeddedJMS();
			customize(configuration);
			server.setConfiguration(configuration);
			server.setJmsConfiguration(jmsConfiguration);
			server.setRegistry(new HornetQNoOpBindingRegistry());
			return server;
		}

		private void customize(org.hornetq.core.config.Configuration configuration) {
			if (this.configurationCustomizers != null) {
				for (HornetQConfigurationCustomizer customizer : this.configurationCustomizers) {
					customizer.customize(configuration);
				}
			}
		}

		@Bean
		@ConditionalOnMissingBean
		public JMSConfiguration hornetQJmsConfiguration() {
			JMSConfiguration configuration = new JMSConfigurationImpl();
			addAll(configuration.getQueueConfigurations(), this.queuesConfiguration);
			addAll(configuration.getTopicConfigurations(), this.topicsConfiguration);
			addQueues(configuration, this.properties.getEmbedded().getQueues());
			addTopics(configuration, this.properties.getEmbedded().getTopics());
			return configuration;
		}

		private <T> void addAll(List<T> list, Collection<? extends T> items) {
			if (items != null) {
				list.addAll(items);
			}
		}

		private void addQueues(JMSConfiguration configuration, String[] queues) {
			boolean persistent = this.properties.getEmbedded().isPersistent();
			for (String queue : queues) {
				configuration.getQueueConfigurations().add(
						new JMSQueueConfigurationImpl(queue, null, persistent, "/queue/"
								+ queue));
			}
		}

		private void addTopics(JMSConfiguration configuration, String[] topics) {
			for (String topic : topics) {
				configuration.getTopicConfigurations().add(
						new TopicConfigurationImpl(topic, "/topic/" + topic));
			}
		}

	}

}
