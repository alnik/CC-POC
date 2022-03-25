package de.hybris.platform.multicountry;

/**
 * @author i303807
 *
 */

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;


public class MockScopeMap implements Scope
{
	private static final Logger log = Logger.getLogger(MockScopeMap.class);
	private final Map<String, Object> beans;
	private final Map<String, Runnable> destructionCallbacks;



	private void init()
	{
		final Tenant ctxTenant = Registry.getCurrentTenantNoFallback();
		if (ctxTenant == null)
		{
			throw new IllegalStateException("no tenant active");
		}
	}

	public MockScopeMap()
	{
		this(new HashMap(10), new HashMap(10));
	}

	public MockScopeMap(final int initialCapacity)
	{
		this(new HashMap(initialCapacity), new HashMap(10));
	}

	protected MockScopeMap(final Map<String, Object> beans, final Map<String, Runnable> destructionCallbacks)
	{
		this.beans = beans;
		this.destructionCallbacks = destructionCallbacks;
		init();
	}

	@Override
	public Object get(final String name, final ObjectFactory<?> factory)
	{
		Object ret = lookup(name);
		if (ret == null)
		{
			ret = factory.getObject();
			put(name, ret);
			if (log.isDebugEnabled())
			{
				log.debug("Created new object for " + name);
			}
		}
		else if (log.isDebugEnabled())
		{
			log.debug("Returned existing object for " + name);
		}
		return ret;
	}

	public Object lookup(final String name)
	{
		return this.beans.get(name);
	}

	public void put(final String name, final Object value)
	{
		this.beans.put(name, value);
	}

	@Override
	public Object remove(final String name)
	{
		removeDestructionCallback(name);
		return removeBean(name);
	}

	protected Object removeBean(final String name)
	{
		return this.beans.remove(name);
	}

	protected void removeDestructionCallback(final String name)
	{
		this.destructionCallbacks.remove(name);
	}

	void performDestructionCallbacks()
	{
		for (final Map.Entry e : this.destructionCallbacks.entrySet())
		{
			((Runnable) e.getValue()).run();
		}
		this.destructionCallbacks.clear();
	}

	int size()
	{
		return this.beans.size();
	}


	@Override
	public void registerDestructionCallback(final String name, final Runnable callback)
	{
		this.destructionCallbacks.put(name, callback);

	}

	@Override
	public Object resolveContextualObject(final String paramString)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getConversationId()
	{
		throw new UnsupportedOperationException();
	}

}