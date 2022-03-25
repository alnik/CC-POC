/**
 *
 */
package de.hybris.contextualattributevaluesbackoffice.models;

/**
 * @author i307088
 *
 */
public class ContextModel
{
	private String code;
	private String name;
	private boolean selected;
	private boolean global;

	/**
	 * @return the global
	 */
	public boolean isGlobal()
	{
		return global;
	}

	/**
	 * @param global
	 *           the global to set
	 */
	public void setGlobal(final boolean global)
	{
		this.global = global;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * @param selected
	 *           the selected to set
	 */
	public void setSelected(final boolean selected)
	{
		this.selected = selected;
	}

	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @param code
	 *           the code to set
	 */
	public void setCode(final String code)
	{
		this.code = code;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *           the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}


}
