package hornetq.autoconfigure;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.junit.After;
import org.junit.Test;

import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.Assert.*;

public class HornetQAutoConfigurationTest {

	private AnnotationConfigApplicationContext context;

	@After
	public void tearDown() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void defaultNativeConnectionFactory() {
		load(EmptyConfiguration.class, "hornetq.mode=native");
		JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);
		HornetQConnectionFactory connectionFactory = this.context
				.getBean(HornetQConnectionFactory.class);
		assertEquals(jmsTemplate.getConnectionFactory(), connectionFactory);
		assertNettyConnectionFactory(connectionFactory, "localhost", 5445);
	}

	@Test
	public void customNativeConnectionFactory() {
		load(EmptyConfiguration.class, "hornetq.mode=native",
				"hornetq.host=192.168.1.15", "hornetq.port=1234");
		JmsTemplate jmsTemplate = this.context.getBean(JmsTemplate.class);
		HornetQConnectionFactory connectionFactory = this.context
				.getBean(HornetQConnectionFactory.class);
		assertEquals(jmsTemplate.getConnectionFactory(), connectionFactory);
		assertNettyConnectionFactory(connectionFactory, "192.168.1.15", 1234);
	}

	@Test
	public void embeddedConnectionFactory() {
		load(EmptyConfiguration.class, "hornetq.mode=embedded");

		assertEquals(1, this.context.getBeansOfType(EmbeddedJMS.class).size());
		HornetQConnectionFactory connectionFactory = this.context
				.getBean(HornetQConnectionFactory.class);
		assertInVmConnectionFactory(connectionFactory);
	}

	@Test
	public void customizerIsApplied() {
		load(CustomHornetQConfiguration.class);
		org.hornetq.core.config.Configuration configuration = this.context
				.getBean(org.hornetq.core.config.Configuration.class);
		assertEquals("customFooBar", configuration.getName());
	}


	@Configuration
	static class EmptyConfiguration {}

	@Configuration
	static class CustomHornetQConfiguration {

		@Bean
		public HornetQConfigurationCustomizer myHornetQCustomize() {
			return new HornetQConfigurationCustomizer() {
				@Override
				public void customize(org.hornetq.core.config.Configuration configuration) {
					configuration.setName("customFooBar");
				}
			};
		}
	}

	private TransportConfiguration assertInVmConnectionFactory(
			HornetQConnectionFactory connectionFactory) {
		TransportConfiguration transportConfig = getSingleTransportConfiguration(connectionFactory);
		assertEquals(InVMConnectorFactory.class.getName(),
				transportConfig.getFactoryClassName());
		return transportConfig;
	}

	private TransportConfiguration assertNettyConnectionFactory(
			HornetQConnectionFactory connectionFactory, String host, int port) {
		TransportConfiguration transportConfig = getSingleTransportConfiguration(connectionFactory);
		assertEquals(NettyConnectorFactory.class.getName(),
				transportConfig.getFactoryClassName());
		assertEquals(host, transportConfig.getParams().get("host"));
		assertEquals(port, transportConfig.getParams().get("port"));
		return transportConfig;
	}

	private TransportConfiguration getSingleTransportConfiguration(
			HornetQConnectionFactory connectionFactory) {
		TransportConfiguration[] transportConfigurations = connectionFactory
				.getServerLocator().getStaticTransportConfigurations();
		assertEquals(1, transportConfigurations.length);
		return transportConfigurations[0];
	}

	private void load(Class<?> config, String... environment) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.register(config);
		applicationContext.register(HornetQAutoConfiguration.class,
				JmsAutoConfiguration.class);
		applicationContext.refresh();
		this.context = applicationContext;
	}

}
