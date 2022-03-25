/**
 *
 */
package de.hybris.platform.multicountry.promotions.translators;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionValidator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author i307088
 *
 */
public class RuleLocalTimeConditionTranslator implements RuleConditionTranslator, RuleConditionValidator
{
	private static final String LOCAL_DATE = "localTime";

	public static final String DATE_FROM_PARAM = "fromDate";
	public static final String DATE_TO_PARAM = "toDate";

	@Override
	public void validate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition) throws RuleCompilerException
	{
		final RuleParameterData fromDateParam = condition.getParameters().get(DATE_FROM_PARAM);
		final RuleParameterData toDateParam = condition.getParameters().get(DATE_TO_PARAM);

		if (fromDateParam.getValue() == null && toDateParam.getValue() == null)
		{
			throw new RuleCompilerException("Provide at least one date");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition) throws RuleCompilerException
	{
		// Read the value specified on the condition
		final RuleParameterData fromDateParam = condition.getParameters().get(DATE_FROM_PARAM);
		final RuleParameterData toDateParam = condition.getParameters().get(DATE_TO_PARAM);

		final Date fromDate = (Date) fromDateParam.getValue();
		final Date toDate = (Date) toDateParam.getValue();

		return buildCondition(context, fromDate, toDate);
	}

	protected RuleIrCondition buildCondition(final RuleCompilerContext context, final Date fromDate, final Date toDate)
	{
		// declare $v1 := CartRAO()
		final String cartRaoVariable = context.generateVariable(CartRAO.class);

		final List<RuleIrCondition> irConditions = new ArrayList<>();


		if (fromDate != null)
		{
			// $v1 := CartRAO(localDate >= fromDate)
			final RuleIrAttributeCondition irDateGreaterThen = new RuleIrAttributeCondition();
			irDateGreaterThen.setVariable(cartRaoVariable);
			irDateGreaterThen.setAttribute(LOCAL_DATE);
			irDateGreaterThen.setOperator(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL);
			irDateGreaterThen.setValue(fromDate.getTime());
			irConditions.add(irDateGreaterThen);
		}

		if (toDate != null)
		{
			// $v1 := CartRAO(localDate <= toDate)
			final RuleIrAttributeCondition irDateLessThen = new RuleIrAttributeCondition();
			irDateLessThen.setVariable(cartRaoVariable);
			irDateLessThen.setAttribute(LOCAL_DATE);
			irDateLessThen.setOperator(RuleIrAttributeOperator.LESS_THAN_OR_EQUAL);
			irDateLessThen.setValue(toDate.getTime());
			irConditions.add(irDateLessThen);
		}

		final RuleIrGroupCondition irGroupCondition = new RuleIrGroupCondition();
		irGroupCondition.setOperator(RuleIrGroupOperator.AND);
		irGroupCondition.setChildren(irConditions);

		return irGroupCondition;
	}
}