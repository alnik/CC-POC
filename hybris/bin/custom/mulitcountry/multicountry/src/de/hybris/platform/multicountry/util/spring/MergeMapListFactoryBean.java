/**
 *
 */
package de.hybris.platform.multicountry.util.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * Merges two map of lists (baseMap and additionaMap) into a new map. Supports also cloning and merging the lists.
 *
 * @author i307088
 */
public class MergeMapListFactoryBean extends org.springframework.beans.factory.config.AbstractFactoryBean<Map<Object, List<Object>>>
{
	private Map<Object, List<Object>> baseMap;
	private Map<Object, List<Object>> additionalMap;
	private boolean cloneLists = true;
	private boolean mergeLists = true;

	@Override
	protected Map<Object, List<Object>> createInstance() throws Exception
	{
		final Map<Object, List<Object>> newMap = copy(baseMap, cloneLists);
		merge(newMap, additionalMap, mergeLists);
		return newMap;
	}

	/**
	 * Merge two maps.
	 *
	 * @param arg1
	 *           A map
	 * @param arg2
	 *           Another map
	 * @param mergeValues
	 *           If true,
	 */
	protected void merge(final Map<Object, List<Object>> arg1, final Map<Object, List<Object>> arg2, final boolean mergeValues)
	{
		if (mergeValues)
		{
			arg2.entrySet().stream().forEach(e -> {
				final List<Object> value = arg1.get(e.getKey());
				if (value != null)
				{
					value.addAll(e.getValue());
				}
				else
				{
					arg1.put(e.getKey(), e.getValue());
				}
			});
		}
		else
		{
			arg1.putAll(arg2);
		}
	}

	/**
	 * Copy a map.
	 *
	 * @param aMap
	 *           An input map
	 * @param cloneValues
	 *           if 'true' each entry value will be copied into a new list.
	 * @return A copy of the input map.
	 */
	protected Map<Object, List<Object>> copy(final Map<Object, List<Object>> aMap, final boolean cloneValues)
	{
		final Map<Object, List<Object>> newMap;
		if (cloneValues)
		{
			newMap = new HashMap<>();
			aMap.entrySet().stream().forEach(e -> newMap.put(e.getKey(), new ArrayList<>(e.getValue())));
		}
		else
		{
			newMap = new HashMap<>(aMap);
		}
		return newMap;
	}

	@Override
	public Class<?> getObjectType()
	{
		return java.util.HashMap.class;
	}

	/**
	 * @param baseMap
	 *           the baseMap to set
	 */
	@Required
	public void setBaseMap(final Map<Object, List<Object>> baseMap)
	{
		this.baseMap = baseMap;
	}

	/**
	 * @param additionalMap
	 *           the additionalMap to set
	 */
	@Required
	public void setAdditionalMap(final Map<Object, List<Object>> additionalMap)
	{
		this.additionalMap = additionalMap;
	}

	/**
	 * @param mergeLists
	 *           the mergeLists to set
	 */
	public void setMergeLists(final boolean mergeLists)
	{
		this.mergeLists = mergeLists;
	}

	/**
	 * @param cloneLists
	 *           the cloneLists to set
	 */
	public void setCloneLists(final boolean cloneLists)
	{
		this.cloneLists = cloneLists;
	}
}
