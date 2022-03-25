/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.multicountry.backoffice.as;

import java.io.Serializable;
import java.util.Objects;


/**
 * View model for index configurations.
 */
public class StoreConfigurationModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String code;
	private String name;


	public StoreConfigurationModel()
	{

	}

	public StoreConfigurationModel(final String code, final String name)
	{
		this.code = code;
		this.name = name;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(final String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null || (obj.getClass() != this.getClass()))
		{
			return false;
		}

		final StoreConfigurationModel other = (StoreConfigurationModel) obj;
		return Objects.equals(other.code, this.code);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.code);
	}
}
