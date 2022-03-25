/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2012 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package de.hybris.platform.multicountry.jalo;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.testframework.HybrisJUnit4TransactionalTest;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * JUnit Tests for the Multicountry extension
 */
public class MulticountryTest extends HybrisJUnit4TransactionalTest
{
	/** Edit the local|project.properties to change logging behaviour (properties log4j.*). */
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MulticountryTest.class.getName());

	@Before
	public void setUp()
	{
		// implement here code executed before each test
	}

	@After
	public void tearDown()
	{
		// implement here code executed after each test
	}

	/**
	 * This is a sample test method.
	 */
	@Test
	public void testMulticountry()
	{
		final boolean testTrue = true;
		assertTrue("true is not true", testTrue);
	}

	@Test
	public void testBusinesseventshub()
	{
		final boolean testTrue = true;
		assertThat(testTrue).isTrue();
		final UserModel userModel = new UserModel();
		//violation 1
		userModel.getGroups();
		//violation 2
		userModel.getCarts();

		final UserModel testModel = new UserModel();
		//violation 3
		testModel.getGroups();

		final CategoryModel cateogryModel = new CategoryModel();

		cateogryModel.getAllSupercategories();
	}
}
