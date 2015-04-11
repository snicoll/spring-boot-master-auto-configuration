package hornetq.autoconfigure;

import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass({ConnectionFactory.class, HornetQJMSClient.class})
@ConditionalOnMissingBean(ConnectionFactory.class)
@EnableConfigurationProperties(HornetQProperties.class)
public class HornetQAutoConfiguration {

	@Autowired
	private HornetQProperties properties;

	@Bean
	public ConnectionFactory connectionFactory() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(TransportConstants.HOST_PROP_NAME, this.properties.getHost());
		params.put(TransportConstants.PORT_PROP_NAME, this.properties.getPort());
		TransportConfiguration transportConfiguration = new TransportConfiguration(
				NettyConnectorFactory.class.getName(), params);
		return HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF,
				transportConfiguration);
	}

}