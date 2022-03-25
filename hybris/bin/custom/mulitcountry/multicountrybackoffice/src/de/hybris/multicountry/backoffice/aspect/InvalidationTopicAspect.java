/**
 *
 */
package de.hybris.multicountry.backoffice.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;


/**
 * @author i304602
 *
 */
@Aspect
public class InvalidationTopicAspect
{


	@Pointcut("execution (public * com.hybris.cockpitng.editor.extendedmultireferenceeditor.renderer.DefaultRowRenderer.render(..))")
	public void invalidationAspect()
	{
		/* EMPTY */
	}

	@Around("invalidationAspect()")
	public Object invalidate(final ProceedingJoinPoint pjp) throws Throwable
	{
		System.out.println("CIAOOOO");
		return pjp.proceed();
	}





}
