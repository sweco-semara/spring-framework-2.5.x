package org.springframework.ui.velocity;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.springframework.util.StringUtils;

/**
 * Utility class for working with a VelocityEngine.
 * Provides convenience methods to merge a Velocity template with a model.
 * @author Juergen Hoeller
 * @since 22.01.2004
 */
public abstract class VelocityEngineUtils {

	private static final Log logger = LogFactory.getLog(VelocityEngineUtils.class);

	/**
	 * Merge the specified Velocity template with the given model and write
	 * the result to the given Writer.
	 * @param velocityEngine VelocityEngine to work with
	 * @param templateLocation the location of template, relative to Velocity's
	 * resource loader path
	 * @param model the Map that contains model names as keys and model objects
	 * as values
	 * @param writer the Writer to write the result to
	 * @throws VelocityException if the template wasn't found or rendering failed
	 */
	public static void mergeTemplate(VelocityEngine velocityEngine, String templateLocation, Map model,
	                                 Writer writer) throws VelocityException {
		try {
			VelocityContext velocityContext = new VelocityContext();
			exposeModelAsContextAttributes(model, velocityContext);
			velocityEngine.mergeTemplate(templateLocation, velocityContext, writer);
		}
		catch (VelocityException ex) {
			throw ex;
		}
		catch (Exception ex) {
			logger.error("Why does VelocityEngine throw a generic checked exception, after all?", ex);
			throw new VelocityException(ex.getMessage());
		}
	}

	/**
	 * Merge the specified Velocity template with the given model into a String.
	 * @param velocityEngine VelocityEngine to work with
	 * @param templateLocation the location of template, relative to Velocity's
	 * resource loader path
	 * @param model the Map that contains model names as keys and model objects
	 * as values
	 * @return the result as String
	 * @throws VelocityException if the template wasn't found or rendering failed
	 */
	public static String mergeTemplateIntoString(VelocityEngine velocityEngine, String templateLocation, Map model)
	    throws VelocityException {
		StringWriter result = new StringWriter();
		mergeTemplate(velocityEngine, templateLocation, model, result);
		return result.toString();
	}
	
	/**
	 * Expose the models in the given map as Velocity context attributes.
	 * Names will be taken from the map and fixed for Velocity compliance as required.
	 * @param model Map of model data to expose
	 * @param velocityContext VelocityContext to add data to
	 */
	public static void exposeModelAsContextAttributes(Map model, Context velocityContext) {
		if (model != null) {
			Iterator itr = model.keySet().iterator();
			while (itr.hasNext()) {
				String modelName = (String) itr.next();
				Object modelObject = model.get(modelName);
				modelName = StringUtils.replace(modelName, ".", "_");
				if (logger.isDebugEnabled()) {
					logger.debug("Added model attribute with name [" + modelName + "] and value [" + modelObject +
											 "] to Velocity context");
				}
				velocityContext.put(modelName, modelObject);
			}
		}
		else {
			logger.debug("Model is null. Nothing to expose to Velocity context");
		}
	}

}
