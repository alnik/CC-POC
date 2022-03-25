/**
 *
 */
package de.hybris.platform.multicountry.services;

import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;

import java.util.Collection;


/**
 * @author marco.rolando
 *
 */
public interface MulticountryRestrictionService
{
	void setCurrentProductAvailabilityGroups(Collection<ProductAvailabilityGroupModel> groups);

	Collection<ProductAvailabilityGroupModel> getCurrentProductAvailabilityGroup();

	void disableOnlineDateRestriction();

	void disableApprovalStatusRestriction();
}
