package de.hybris.commercesearchbackoffice.services.impl;

import de.hybris.platform.basecommerce.exceptions.BaseSiteActivationException;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.impersonation.ImpersonationContext;
import de.hybris.platform.commerceservices.impersonation.impl.DefaultImpersonationService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.europe1.enums.UserTaxGroup;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.servicelayer.internal.i18n.I18NConstants;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.store.BaseStoreModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * Created by luca.zangari on 02/04/2014.
 */
public class DefaultMulticountryImpersonationService extends DefaultImpersonationService
{
	private static final Logger LOG = Logger.getLogger(DefaultMulticountryImpersonationService.class);
	public static final String AVAILABILITY_GROUPS = "availabilityGroups";


	@Override
	protected void configureSession(final ImpersonationContext context)
	{
		custumConfigSession(context);
		getSessionService().setAttribute("enable.language.fallback.serviceLayer", Boolean.TRUE);
		getSessionService().setAttribute(I18NConstants.LANGUAGE_FALLBACK_ENABLED, Boolean.TRUE);
		addAvailabilityGroupOnSession(context.getSite().getStores());
	}

	private void addAvailabilityGroupOnSession(final List<BaseStoreModel> baseStores)
	{
		final Collection<ProductAvailabilityGroupModel> currentAVG = new ArrayList<ProductAvailabilityGroupModel>();

		for (final BaseStoreModel baseStore : baseStores)
		{
			if (baseStore.getAvailabilityGroups() != null)
			{
				currentAVG.addAll(baseStore.getAvailabilityGroups());
			}
		}

		if (currentAVG.isEmpty())
		{
			getSessionService().getCurrentSession().setAttribute(AVAILABILITY_GROUPS, "0");
		}
		else
		{
			getSessionService().getCurrentSession().setAttribute(AVAILABILITY_GROUPS, currentAVG);
		}

	}

	//add to fix problem

	@Override
	protected BaseSiteModel determineSessionBaseSite(final ImpersonationContext context)
	{
		final UserModel user = getUserService().getCurrentUser();
		if (!(user instanceof EmployeeModel))
		{
			return super.determineSessionBaseSite(context);
		}

		final EmployeeModel employee = (EmployeeModel) user;
		if (CollectionUtils.isEmpty(employee.getManagedStores()))
		{
			return super.determineSessionBaseSite(context);
		}

		final BaseStoreModel store = employee.getManagedStores().iterator().next();
		if (CollectionUtils.isEmpty(store.getCmsSites()))
		{
			return super.determineSessionBaseSite(context);
		}

		return store.getCmsSites().iterator().next();
	}


	private void custumConfigSession(final ImpersonationContext context)
	{
		final BaseSiteModel site = determineSessionBaseSite(context);

		if (site != null)
		{
			try
			{
				getBaseSiteService().setCurrentBaseSite(site, true);
			}
			catch (final BaseSiteActivationException e)
			{
				LOG.error("Failed to activate BaseSite [" + site + "] in the session context.", e);
			}

		}
		else
		{
			LOG.error("Couldn't determine the site from the context. Hence the site related session state won't be adjusted.");
		}

		final UserModel user = determineSessionUser(context);

		if (user != null)
		{
			getUserService().setCurrentUser(user);
		}

		final UserTaxGroup taxGroup = determineSessionTaxGroup(context);

		if (taxGroup != null)
		{
			getSessionService().setAttribute("Europe1PriceFactory_UTG", taxGroup);
		}

		final Collection catalogVersions = determineSessionCatalogVersions(context);

		if ((catalogVersions != null) && (!(catalogVersions.isEmpty())))
		{
			getCatalogVersionService().setSessionCatalogVersions(catalogVersions);
		}

		final LanguageModel language = determineSessionLanguage(context);

		if (language != null)
		{
			getI18nService().setCurrentLanguage(language);
		}

		final CurrencyModel currency = determineSessionCurrency(context);

		if (currency == null)
		{
			return;
		}
		getI18nService().setCurrentCurrency(currency);

	}

	@Override
	public <R, T extends Throwable> R executeInContext(final ImpersonationContext context, final Executor<R, T> wrapper) throws T
	{
		final Object result = getSessionService().executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				try
				{
					//impersonate
					configureSession(context);
					//execute the injected code
					return wrapper.execute();
				}
				catch (final Throwable e)//NOSONAR
				{
					return e;
				}
			}
		}, getUserService().getCurrentUser());

		if (result instanceof Throwable)//NOSONAR
		{
			throw (T) result;
		}
		else
		{
			return (R) result;
		}
	}

}