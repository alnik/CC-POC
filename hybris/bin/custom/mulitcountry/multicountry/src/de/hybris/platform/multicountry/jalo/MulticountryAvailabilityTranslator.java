/**
 *
 */
package de.hybris.platform.multicountry.jalo;


import de.hybris.platform.impex.constants.ImpExConstants;
import de.hybris.platform.impex.jalo.header.HeaderValidationException;
import de.hybris.platform.impex.jalo.header.StandardColumnDescriptor;
import de.hybris.platform.impex.jalo.translators.AbstractValueTranslator;
import de.hybris.platform.impex.jalo.translators.CollectionValueTranslator;
import de.hybris.platform.impex.jalo.translators.SingleValueTranslator;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.JaloSystemException;
import de.hybris.platform.jalo.enumeration.EnumerationManager;
import de.hybris.platform.jalo.enumeration.EnumerationType;
import de.hybris.platform.jalo.enumeration.EnumerationValue;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearch;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.type.*;
import de.hybris.platform.multicountry.jalo.productavailabilitygroup.ProductAvailabilityAssignment;
import de.hybris.platform.multicountry.jalo.productavailabilitygroup.ProductAvailabilityGroup;
import de.hybris.platform.util.StandardDateRange;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



public class MulticountryAvailabilityTranslator extends CollectionValueTranslator
{



	@SuppressWarnings("deprecation")
	public MulticountryAvailabilityTranslator()
	{
		super((CollectionType) TypeManager.getInstance().getType("ProductAvailabilityAssignmentCollection"), new SingleAvailabilityAssignementTranslator());
	}

	@SuppressWarnings("deprecation")
	public MulticountryAvailabilityTranslator(final AbstractValueTranslator elementTranslator)
	{
		super((CollectionType) TypeManager.getInstance().getType("ProductAvailabilityAssignmentCollection"), elementTranslator);
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

	public static class SingleAvailabilityAssignementTranslator extends SingleValueTranslator
	{


		private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		private static final Logger LOG = Logger.getLogger(MulticountryAvailabilityTranslator.class);
		private final Map approvalStatusMap;
		private final EnumerationType approvalStatus;


		public SingleAvailabilityAssignementTranslator()
		{
			this.approvalStatusMap = new HashMap();
			this.approvalStatus = EnumerationManager.getInstance().getEnumerationType("articleApprovalStatus");
		}


		@Override
		public void validate(final StandardColumnDescriptor standardcd) throws HeaderValidationException
		{
			super.validate(standardcd);
			if (Product.class.isAssignableFrom(standardcd.getHeader().getConfiguredComposedType().getJaloClass()))
			{
				return;
			}
			throw new HeaderValidationException(standardcd.getHeader(), "MulticountryAvailabilityTranslator may only be used within product headers",
					0);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void init(final StandardColumnDescriptor columnDescriptor)
		{
			super.init(columnDescriptor);


			final List approvalStatusList = this.approvalStatus.getValues();

			for (int i = 0; i < approvalStatusList.size(); ++i)
			{
				@SuppressWarnings("deprecation")
				final EnumerationValue enumval = (EnumerationValue) approvalStatusList.get(i);
				this.approvalStatusMap.put(enumval.getCode().toLowerCase(), enumval);
			}


		}


		@Override
		public Object convertToJalo(final String valueExpr, final Item forItem)
		{

			ProductAvailabilityGroup availabilityGroup = null;
			Date offlineDate = null;
			Date onlineDate = null;

			EnumerationValue approvalStatus = null;
			final Product product = (Product) forItem;

			final String[] tokens = valueExpr.split(" ");
			if (tokens.length > 0)
			{
				final String availabilityValueExpr = tokens[0].trim();

				availabilityGroup = findExistingAvailabilityGroup(availabilityValueExpr);
				if (availabilityGroup == null)
				{
					throw new JaloSystemException("Missing/Wrong availabilityGroup definition within:" + valueExpr, 123);
				}


				if (tokens.length > 1)
				{
					final String approvalStatusExpr = tokens[1].trim();


					if (this.approvalStatusMap.containsKey(approvalStatusExpr.toLowerCase()))
					{
						approvalStatus = (EnumerationValue) this.approvalStatusMap.get(approvalStatusExpr.toLowerCase());
					}
					if (approvalStatus == null)
					{
						throw new JaloSystemException("Missing/Wrong approvalStatus definition within:" + valueExpr, 123);
					}

				}
				if (tokens.length > 2)
				{
					StandardDateRange dateRange=parseDateRange(valueExpr);
					if(dateRange != null) {
						onlineDate = dateRange.getStart();
						offlineDate = dateRange.getEnd();
					}
				}
			}

			try
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("### creating ProductAvailabilityAssignment with...");
					LOG.debug("forItem: " + product.getCode());
					LOG.debug("status: " + approvalStatus);
					LOG.debug("availabilityGroup: " + availabilityGroup);
					LOG.debug("offlineDate: " + offlineDate);
					LOG.debug("onlineDate: " + onlineDate);
				}

				for (final ProductAvailabilityAssignment productAvailibilityAssignment : (List<ProductAvailabilityAssignment>)product.getLinkedItems(false,"availability",null))
				{

					final ProductAvailabilityGroup oldAvailabilityGroup = productAvailibilityAssignment.getAvailabilityGroup();
					if (oldAvailabilityGroup != availabilityGroup)
					{
						if (oldAvailabilityGroup == null)
						{
							continue;
						}
						if (!(availabilityGroup.equals(availabilityGroup)))
						{
							continue;
						}
					}
					final EnumerationValue oldApprovalStatus = productAvailibilityAssignment.getStatus();
					if (oldApprovalStatus != approvalStatus)
					{
						if (oldApprovalStatus == null)
						{
							continue;
						}
						if (!(oldApprovalStatus.equals(approvalStatus)))
						{
							continue;
						}
					}
					final Date oldOfflineDate = productAvailibilityAssignment.getOfflineDate();
					if (oldOfflineDate != offlineDate)
					{
						if (!(oldOfflineDate.equals(offlineDate)))
						{
							continue;
						}
					}
					final Date oldOnlineDate = productAvailibilityAssignment.getOnlineDate();
					if (oldOnlineDate != onlineDate)
					{
						if (!(oldOnlineDate.equals(onlineDate)))
						{
							continue;
						}
					}


					return productAvailibilityAssignment;

				}


				return (ProductAvailabilityAssignment) ComposedType.newInstance(product.getSession().getSessionContext(), ProductAvailabilityAssignment.class, ProductAvailabilityAssignment.PRODUCT, product,
						ProductAvailabilityAssignment.AVAILABILITYGROUP, availabilityGroup, ProductAvailabilityAssignment.STATUS, approvalStatus, ProductAvailabilityAssignment.OFFLINEDATE, offlineDate,
						ProductAvailabilityAssignment.ONLINEDATE, onlineDate);



			}
			catch (final JaloGenericCreationException e)
			{
				LOG.error(e);
				e.printStackTrace();
			}
			catch (final JaloAbstractTypeException e)
			{
				LOG.error(e);
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected String convertToString(final Object source)
		{
			final ProductAvailabilityAssignment availibilityAssignment = (ProductAvailabilityAssignment) source;
			final ProductAvailabilityGroup availabilityGroup = availibilityAssignment.getAvailabilityGroup();
			final EnumerationValue availabilityStatus = availibilityAssignment.getStatus();
			final Date onlineDate = availibilityAssignment.getOnlineDate();
			final Date offlineDate = availibilityAssignment.getOfflineDate();


			final StringBuilder text = new StringBuilder();

			if (availabilityGroup != null)
			{
				text.append(availabilityGroup.getId()).append(" ");
			}
			else if (availabilityStatus != null)
			{
				text.append(availabilityStatus.getCode()).append(" ");
			}
			if (onlineDate != null && offlineDate != null)
			{
				text.append(onlineDate).append(",").append(offlineDate).append(" ");
			}

			return text.toString();
		}

		private ProductAvailabilityGroup findExistingAvailabilityGroup(final String id)
		{
			@SuppressWarnings("deprecation")
			final List<ProductAvailabilityGroup> result = FlexibleSearch.getInstance()
					.search("SELECT {PK} FROM {ProductAvailabilityGroup} WHERE {id}=?id", Collections.singletonMap("id", id), ProductAvailabilityGroup.class)
					.getResult();
			if (CollectionUtils.isNotEmpty(result))
			{
				return result.get(0);
			}
			else
			{
				return null;
			}
		}

		protected StandardDateRange parseDateRange(final String valueExpr)
		{
			StandardDateRange dateRange = null;
			final int startPosition = valueExpr.indexOf(ImpExConstants.Syntax.MODIFIER_START);
			final int endPosition = valueExpr.indexOf(ImpExConstants.Syntax.MODIFIER_END);

			boolean dateRangeIsWelldefined = true;

			if (startPosition != -1 || endPosition != -1) // daterange definition pattern found?
			{
				dateRangeIsWelldefined = false;
				if (endPosition > startPosition) // minimum requirement
				{
					final String dateRangeExpr = valueExpr.substring(startPosition + 1, endPosition);
					final int sepPos = dateRangeExpr.indexOf(ImpExConstants.Syntax.DATERANGE_DELIMITER);
					if (sepPos != -1)
					{
						try
						{
							final String start = dateRangeExpr.substring(0, sepPos).trim();
							final String end = dateRangeExpr.substring(sepPos + 1, dateRangeExpr.length()).trim();
							final Date startDate = DEFAULT_DATE_FORMAT.parse(start);
							final Date endDate = DEFAULT_DATE_FORMAT.parse(end);
							dateRange = new StandardDateRange(startDate, endDate);
							dateRangeIsWelldefined = true;
						}
						catch (final ParseException e)
						{
							e.printStackTrace();
						}
					}
				}
			}

			if (!dateRangeIsWelldefined)
			{
				throw new JaloInvalidParameterException("Invalid daterange definition!", 123);
			}

			return dateRange;
		}
	}

}
