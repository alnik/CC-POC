/**
 *
 */
package de.hybris.platform.multicountry.jalo;



import de.hybris.platform.europe1.jalo.Europe1PriceFactory;
import de.hybris.platform.europe1.jalo.TaxRow;
import de.hybris.platform.impex.jalo.header.HeaderValidationException;
import de.hybris.platform.impex.jalo.header.StandardColumnDescriptor;
import de.hybris.platform.impex.jalo.translators.AbstractValueTranslator;
import de.hybris.platform.impex.jalo.translators.CollectionValueTranslator;
import de.hybris.platform.impex.jalo.translators.SingleValueTranslator;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.enumeration.EnumerationManager;
import de.hybris.platform.jalo.enumeration.EnumerationType;
import de.hybris.platform.jalo.enumeration.EnumerationValue;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearch;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.jalo.order.price.Tax;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.type.CollectionType;
import de.hybris.platform.jalo.type.TypeManager;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.util.DateRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


@SuppressWarnings("deprecation")
public class TaxesTranslator extends CollectionValueTranslator
{


	@SuppressWarnings("deprecation")
	public TaxesTranslator()
	{
		super((CollectionType) TypeManager.getInstance().getType("TaxRowCollectionType"), new TaxesRowTranslator());
	}

	@SuppressWarnings("deprecation")
	public TaxesTranslator(final AbstractValueTranslator elementTranslator)
	{
		super((CollectionType) TypeManager.getInstance().getType("TaxRowCollectionType"), elementTranslator);
	}

	@Override
	protected List splitAndUnescape(final String valueExpr)
	{
		final List tokens = super.splitAndUnescape(valueExpr);
		if ((tokens == null) || (tokens.size() < 2))
		{
			return tokens;
		}

		final List ret = new ArrayList(tokens.size());

		ret.add(tokens.get(0));
		for (int i = 1; i < tokens.size(); ++i)
		{
			final String prev = (String) tokens.get(i - 1);
			final String current = (String) tokens.get(i);

			if (Character.isDigit(prev.charAt(prev.length() - 1)))
			{
				final String sxx = (String) ret.get(ret.size() - 1);
				ret.set(ret.size() - 1, sxx + getCollectionValueDelimiter() + current);
			}
			else
			{
				ret.add(current);
			}
		}
		return ret;
	}

	public static class TaxesRowTranslator extends SingleValueTranslator
	{

		private static final Logger LOG = Logger.getLogger(TaxesRowTranslator.class);
		private final Map taxUserGroupsMap;
		@SuppressWarnings("deprecation")
		private final EnumerationType utg;

		@SuppressWarnings("deprecation")
		public TaxesRowTranslator()
		{

			this.taxUserGroupsMap = new HashMap();
			this.utg = EnumerationManager.getInstance().getEnumerationType("UserTaxGroup");
		}

		@SuppressWarnings("deprecation")
		@Override
		public void validate(final StandardColumnDescriptor standardcd) throws HeaderValidationException
		{
			super.validate(standardcd);
			if (Product.class.isAssignableFrom(standardcd.getHeader().getConfiguredComposedType().getJaloClass()))
			{
				return;
			}
			throw new HeaderValidationException(standardcd.getHeader(), "TaxRowTranslator may only be used within product headers",
					0);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void init(final StandardColumnDescriptor columnDescriptor)
		{
			super.init(columnDescriptor);


			@SuppressWarnings("deprecation")
			final List taxUserGroupsList = this.utg.getValues();

			for (int i = 0; i < taxUserGroupsList.size(); ++i)
			{
				@SuppressWarnings("deprecation")
				final EnumerationValue enumval = (EnumerationValue) taxUserGroupsList.get(i);
				this.taxUserGroupsMap.put(enumval.getCode().toLowerCase(), enumval);
			}


		}


		private Tax findExistingTax(final String code)
		{
			@SuppressWarnings("deprecation")
			final List<Tax> result = FlexibleSearch.getInstance()
					.search("SELECT {PK} FROM {Tax} WHERE {code}=?code", Collections.singletonMap("code", code), Tax.class)
					.getResult();
			if (null != result)
			{
				return result.get(0);
			}
			else
			{
				return null;
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		public Object convertToJalo(final String valueExpr, final Item forItem)
		{

			User user = null;
			Tax tax = null;
			@SuppressWarnings("deprecation")
			EnumerationValue userTaxGroup = null;
			@SuppressWarnings("deprecation")
			final Product product = (Product) forItem;

			final String[] tokens = valueExpr.split(" ");
			if (tokens.length > 0)
			{
				final String taxValueExpr = tokens[0].trim();

				tax = findExistingTax(taxValueExpr);

				if (tokens.length > 1)
				{
					final String userOrUsergroupExpr = tokens[1].trim();
					try
					{
						user = UserManager.getInstance().getUserByLogin(userOrUsergroupExpr);
					}
					catch (@SuppressWarnings("unused") final JaloItemNotFoundException localJaloItemNotFoundException)
					{
						LOG.debug("localJaloItemNotFoundException", localJaloItemNotFoundException);
					}

					if (user == null)
					{

						if (this.taxUserGroupsMap.containsKey(userOrUsergroupExpr.toLowerCase()))
						{
							userTaxGroup = (EnumerationValue) this.taxUserGroupsMap.get(userOrUsergroupExpr.toLowerCase());
						}
						if (userTaxGroup == null)
						{
							throw new JaloSystemException("Missing user|group definition within " + valueExpr, 123);
						}
					}
				}
			}
			final Europe1PriceFactory epf = Europe1PriceFactory.getInstance();
			try
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("### creating TaxRow with...");
					LOG.debug("forItem: " + forItem.getPK());
					LOG.debug("user: " + user);
					LOG.debug("userTaxGroup: " + userTaxGroup);
					LOG.debug("tax: " + tax);
				}

				for (final TaxRow taxrow : epf.getEurope1Taxes(product))
				{

					final User oldUser = taxrow.getUser();
					if (oldUser != user)
					{
						if (oldUser == null)
						{
							continue;
						}
						if (!(oldUser.equals(user)))
						{
							continue;
						}
					}
					final EnumerationValue old_usergroup = taxrow.getUserGroup();
					if (old_usergroup != userTaxGroup)
					{
						if (old_usergroup == null)
						{
							continue;
						}
						if (!(old_usergroup.equals(userTaxGroup)))
						{
							continue;
						}
					}


					final Tax old_tax = taxrow.getTax();
					if (old_tax != tax)
					{
						if (old_tax == null)
						{
							continue;
						}
						if (!(old_tax.equals(tax)))
						{
							continue;
						}
					}

					return taxrow;

				}

				final EnumerationValue productTaxGroup = null;
				final DateRange dateRange = null;
				final Double value = null;

				return epf.createTaxRow((Product) forItem, productTaxGroup, user, userTaxGroup, tax, dateRange, value);
			}
			catch (final JaloPriceFactoryException e)
			{
				LOG.error(e);
			}
			return null;
		}

		@SuppressWarnings("deprecation")
		@Override
		protected String convertToString(final Object source)
		{
			final TaxRow taxrow = (TaxRow) source;
			final Tax tax = taxrow.getTax();
			final User user = taxrow.getUser();
			@SuppressWarnings("deprecation")
			final EnumerationValue userTaxGroup = taxrow.getUserGroup();


			final StringBuilder text = new StringBuilder();

			if (user != null)
			{
				text.append(user.getUID()).append(" ");
			}
			else if (userTaxGroup != null)
			{
				text.append(userTaxGroup.getCode()).append(" ");
			}
			if (tax != null)
			{
				text.append(tax.getCode()).append(" ").append('=').append(" ");
			}

			return text.toString();
		}
	}

}