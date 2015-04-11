package hornetq.autoconfigure;

import java.io.File;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.server.JournalType;

class HornetQEmbeddedConfigurationFactory {

	private HornetQProperties.Embedded properties;

	public HornetQEmbeddedConfigurationFactory(HornetQProperties properties) {
		this.properties = properties.getEmbedded();
	}

	public Configuration createConfiguration() {
		ConfigurationImpl configuration = new ConfigurationImpl();
		configuration.setSecurityEnabled(false);
		configuration.setPersistenceEnabled(this.properties.isPersistent());

		String dataDir = getDataDir();
		configuration.setJournalDirectory(dataDir + "/journal");
		if (this.properties.isPersistent()) {
			configuration.setJournalType(JournalType.NIO);
			configuration.setLargeMessagesDirectory(dataDir + "/largemessages");
			configuration.setBindingsDirectory(dataDir + "/bindings");
			configuration.setPagingDirectory(dataDir + "/paging");
		}

		TransportConfiguration transportConfiguration = new TransportConfiguration(
				InVMAcceptorFactory.class.getName());
		configuration.getAcceptorConfigurations().add(transportConfiguration);

		configuration.setClusterPassword("SpringBootRulez");

		return configuration;
	}

	private String getDataDir() {
		if (this.properties.getDataDirectory() != null) {
			return this.properties.getDataDirectory();
		}
		String tempDirectory = System.getProperty("java.io.tmpdir");
		return new File(tempDirectory, "hornetq-data").getAbsolutePath();
	}

}