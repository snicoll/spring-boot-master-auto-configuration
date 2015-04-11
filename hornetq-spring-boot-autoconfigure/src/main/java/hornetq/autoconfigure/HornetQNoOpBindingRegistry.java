package hornetq.autoconfigure;

import org.hornetq.spi.core.naming.BindingRegistry;

public class HornetQNoOpBindingRegistry implements BindingRegistry {

	@Override
	public Object lookup(String name) {
		// This callback is used to check if an entry is present in the context before
		// creating a queue on the fly. This is actually never used to try to fetch a
		// destination that is unknown.
		return null;
	}

	@Override
	public boolean bind(String name, Object obj) {
		// This callback is used bind a Destination created on the fly by the embedded
		// broker using the JNDI name that was specified in the configuration. This does
		// not look very useful since it's used nowhere. It could be interesting to
		// autowire a destination to use it but the wiring is a bit "asynchronous" so
		// better not provide that feature at all.
		return false;
	}

	@Override
	public void unbind(String name) {
	}

	@Override
	public void close() {
	}

	@Override
	public Object getContext() {
		return this;
	}

	@Override
	public void setContext(Object ctx) {
	}

}
