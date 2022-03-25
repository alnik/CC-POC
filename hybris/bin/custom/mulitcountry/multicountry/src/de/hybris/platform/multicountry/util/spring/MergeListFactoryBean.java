/**
 *
 */
package de.hybris.platform.multicountry.util.spring;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


/**
 * Merges two lists (baseList and additionaList) into a new list.
 *
 * @author i307088
 *
 */
public class MergeListFactoryBean extends org.springframework.beans.factory.config.AbstractFactoryBean<LinkedList<Object>>
{
	private List<Object> baseList;
	private List<Object> additionalList;

	@Override
	protected LinkedList<Object> createInstance() throws Exception
	{
		final LinkedList<Object> newList = new LinkedList<>(baseList);
		newList.addAll(additionalList);
		return newList;
	}

	@Override
	public Class<?> getObjectType()
	{
		return java.util.LinkedList.class;
	}

	/**
	 * @param baseList
	 *           the baseList to set
	 */
	@Required
	public void setBaseList(final List<Object> baseList)
	{
		this.baseList = baseList;
	}

	/**
	 * @param additionalList
	 *           the additionalList to set
	 */
	@Required
	public void setAdditionalList(final List<Object> additionalList)
	{
		this.additionalList = additionalList;
	}
}
