/**
 *
 */
package de.hybris.multicountry.backoffice.advisors;

import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.cockpitng.dataaccess.facades.permissions.custom.InstancePermissionAdvisor;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public abstract class AbstractMultiCountryInstancePermissionAdvisor<T> implements InstancePermissionAdvisor<T>
{
	private boolean lockForm = Boolean.parseBoolean(Config.getString("productcockpit.lockform", "false"));

	private UserService userService;

	@Override
	public boolean canDelete(final T model)
	{
		return canChange(model);
	}

	@Override
	public boolean canModify(final T model)
	{
		return canChange(model);
	}

	@Override
	public abstract boolean isApplicableTo(final Object instance);

	protected boolean canChange(final T model)
	{
		boolean canChange = true;
		final boolean isAdmin = getUserService().isAdmin(getUserService().getCurrentUser());
		if (!isAdmin && isLockForm() && model != null)
		{
			canChange = isObjectWritable(model);
		}

		return canChange;
	}

	protected abstract boolean isObjectWritable(T model);

	public void setLockForm(final boolean lockForm)
	{
		this.lockForm = lockForm;
	}

	public boolean isLockForm()
	{
		return lockForm;
	}


	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public UserService getUserService()
	{
		return userService;
	}

}
