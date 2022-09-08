package ar.edu.jdynalloy;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public abstract class JDynAlloyConfig {


	public enum LoopResolutionEnum {
		AnnotatedLoop, Invariant, InvariantWithValidation;
	}

	protected static IJDynAlloyConfig instance = null;

	protected static void setInstance(IJDynAlloyConfig instance) {
		JDynAlloyConfig.instance = instance;
	}

	public static IJDynAlloyConfig getInstance() {
		if (instance == null)
			throw new IllegalStateException("Configuration has not been set");

		return instance;
	}

	public static IJDynAlloyConfig buildConfig(String configFilename) {
		return buildConfig(configFilename, new Properties());
	}

	/**
	 * 
	 * @param configFilename
	 * @param overridingProperties Properties that overrides properties file's values
	 * @return
	 */
	public static IJDynAlloyConfig buildConfig(String configFilename, Properties overridingProperties) {
		if (instance != null)
			throw new IllegalStateException("Configuration has been already set");

		Properties factoryConfigurationProperties = new Properties();

		try {
			factoryConfigurationProperties.load(new FileReader(configFilename));
		} catch (IOException e) {
			throw new IllegalConfigurationException("Error loading property file: " + configFilename, e);
		}

		String configurationManagerImplementationClassName = factoryConfigurationProperties.getProperty("DynJAlloyConfig.Class");
		if (configurationManagerImplementationClassName == null) {
			throw new IllegalConfigurationException("The resource configuration file '" + configFilename
					+ "' was found, but the key 'DynJAlloyConfig.Class' wasn't found");
		}

		Class configurationManagerImplementationClass;
		try {
			configurationManagerImplementationClass = (Class<?>) Class.forName(configurationManagerImplementationClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalConfigurationException("Problem instantiating DynJAlloyConfig", e);
		}

		if (!IJDynAlloyConfig.class.isAssignableFrom(configurationManagerImplementationClass)) {
			throw new IllegalConfigurationException("Class " + configurationManagerImplementationClassName + " don't inherits from DynJAlloyConfig. Check '"
					+ configFilename + "' file for key 'DynJAlloyConfig.Class' wasn't found");
		}

		Constructor<? extends IJDynAlloyConfig> jDynAlloyConfigconstructor;
		try {
			jDynAlloyConfigconstructor = configurationManagerImplementationClass.getConstructor(String.class, Properties.class);
		} catch (SecurityException e) {
			throw new IllegalConfigurationException("Problem instansiating DynJAlloyConfig", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalConfigurationException("Problem instansiating DynJAlloyConfig", e);
		}

		try {
			instance = jDynAlloyConfigconstructor.newInstance(configFilename, overridingProperties);
			return instance;
		} catch (SecurityException e) {
			throw new IllegalConfigurationException("Problem instansiating DynJAlloyConfig", e);
		} catch (IllegalArgumentException e) {
			throw new IllegalConfigurationException("Problem instansiating DynJAlloyConfig", e);
		} catch (InstantiationException e) {
			throw new IllegalConfigurationException("Problem instansiating DynJAlloyConfig", e);
		} catch (IllegalAccessException e) {
			throw new IllegalConfigurationException("Problem instansiating DynJAlloyConfig", e);
		} catch (InvocationTargetException e) {
			throw new IllegalConfigurationException("JDynalloyConfig class " + configurationManagerImplementationClassName
					+ " throw an exception in it constructor that takes a String parameter", e);
		}

	}

	/**
	 * Restart configuration state
	 */
	public static void reset() {
		instance = null;
	}

}
