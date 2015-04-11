package hornetq.autoconfigure;

import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(JmsAutoConfiguration.class)
@ConditionalOnClass({ConnectionFactory.class, HornetQJMSClient.class})
@ConditionalOnMissingBean(ConnectionFactory.class)
public class HornetQAutoConfiguration {

	@Bean
	public ConnectionFactory connectionFactory() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(TransportConstants.HOST_PROP_NAME, "localhost");
		params.put(TransportConstants.PORT_PROP_NAME, 5445);
		TransportConfiguration transportConfiguration = new TransportConfiguration(
				NettyConnectorFactory.class.getName(), params);
		return HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF,
				transportConfiguration);
	}

}