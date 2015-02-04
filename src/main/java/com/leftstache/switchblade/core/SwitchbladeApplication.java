package com.leftstache.switchblade.core;

import com.google.common.collect.*;
import com.google.inject.*;
import org.apache.commons.lang3.exception.*;
import org.reflections.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

/**
 * @author Joel Johnson
 */
public class SwitchbladeApplication<T> implements Closeable, AutoCloseable, Module {
	private static final Logger log = Logger.getLogger(SwitchbladeApplication.class.getName());

	private final Object $start_lock$ = new Object();
	private volatile boolean started = false;

	private final Class<T> applicationClass;
	private T application;

	private final Set<String> packagesToScan;
	private Set<Class<?>> appClasses;

	private Injector injector;
	private List<? extends ApplicationListener> applicationListeners;

	public static <T> SwitchbladeApplication<T> create(Class<T> app) throws IOException {
		Set<String> packagesToScan = findPackagesToScan(app.getPackage().getName());

		SwitchbladeApplication<T> switchbladeApplication = new SwitchbladeApplication<>(app, packagesToScan);
		switchbladeApplication.init();
		return switchbladeApplication;
	}

	private SwitchbladeApplication(Class<T> applicationClass, Set<String> packagesToScan) {
		this.applicationClass = applicationClass;
		this.packagesToScan = packagesToScan;
	}

	private void init() throws IOException {
		ImmutableSet.Builder<Class<?>> appClassesBuilder = ImmutableSet.builder();
		ImmutableSet.Builder<Class<? extends ApplicationListener>> applicationListenerClassesBuilder = ImmutableSet.builder();
		ImmutableSet.Builder<Class<? extends BeanListener>> beanListenerClassesBuilder = ImmutableSet.builder();

		scanExternalClasses(packagesToScan, appClassesBuilder, applicationListenerClassesBuilder, beanListenerClassesBuilder);

		this.appClasses = appClassesBuilder.build();
		Set<Class<? extends ApplicationListener>> applicationListenerClasses = applicationListenerClassesBuilder.build();

		this.injector = Guice.createInjector(this);
		this.application = injector.getInstance(applicationClass);

		@SuppressWarnings("RedundantCast")
		// IntelliJ insists I don't need it, but the compiler insists I do
			List<Object> appInstances = appClasses.stream()
			.map(i -> (Object)injector.getInstance(i))
			.collect(Collectors.<Object>toList());


		@SuppressWarnings("RedundantCast")
		List<? extends ApplicationListener> applicationListeners = applicationListenerClasses.stream()
			.map(i -> (ApplicationListener)injector.getInstance(i))
			.collect(Collectors.toList());

		ImmutableSet<Class<? extends BeanListener>> beanListenerClasses = beanListenerClassesBuilder.build();
		fireConstructBeanEvent(injector, appInstances, beanListenerClasses);

		this.applicationListeners = applicationListeners;
	}

	private static void fireConstructBeanEvent(Injector injector, List<Object> appInstances, ImmutableSet<Class<? extends BeanListener>> beanListenerClasses) {
		for (Object appInstance : appInstances) {
			Method[] methods = appInstance.getClass().getMethods();
			for (Method method : methods) {
				if(method.getAnnotation(PostConstruct.class) != null) {
					try {
						method.invoke(appInstance);
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new SwitchbladeException("Unable to invoke PostConstruct method", e);
					}
				}
			}

			for (Class<? extends BeanListener> beanListenerClass : beanListenerClasses) {
				BeanListener beanListener = injector.getInstance(beanListenerClass);
				try {
					beanListener.postConstruct(appInstance);
				} catch (Exception e) {
					throw new SwitchbladeException("Exception while executing bean listener " + beanListenerClass.getName(), e);
				}
			}
		}
	}

	@Override
	public void configure(Binder binder) {
		//TODO: allow the constructor to set overrides for bindings
	}

	private static Set<String> findPackagesToScan(String appPackage) throws IOException {
		Enumeration<URL> resources = SwitchbladeApplication.class.getClassLoader().getResources("blade.factory");
		Set<String> packagesToScan = new HashSet<>();
		packagesToScan.add(appPackage);

		if(resources != null) {
			while(resources.hasMoreElements()) {
				URL url = resources.nextElement();
				try(InputStream inputStream = url.openStream()) {
					try(Scanner scanner = new Scanner(inputStream)) {
						while(scanner.hasNextLine()) {
							String line = scanner.nextLine();
							packagesToScan.add(line);
						}
					}
				}
			}
		}
		return packagesToScan;
	}

	private static void scanExternalClasses(Set<String> packagesToScan, ImmutableSet.Builder<Class<?>> moduleClasses, ImmutableSet.Builder<Class<? extends ApplicationListener>> applicationListenerClasses, ImmutableSet.Builder<Class<? extends BeanListener>> beanListenerClassesBuilder) throws IOException {
		for (String packageToScan : packagesToScan) {
			Reflections reflections = new Reflections(packageToScan);
			Set<Class<?>> allTypes = reflections.getTypesAnnotatedWith(Component.class);

			for (Class externalClass : allTypes) {
				moduleClasses.add(externalClass);

				if(ApplicationListener.class.isAssignableFrom(externalClass)) {
					applicationListenerClasses.add(externalClass);
				}

				if(BeanListener.class.isAssignableFrom(externalClass)) {
					beanListenerClassesBuilder.add(externalClass);
				}
			}
		}
	}

	/**
	 * Starts the app, notifying all listeners that the app has started.
	 * Blocks until {@link #close} is called, then notifies all listeners the app has stopped.
	 */
	public void start() {
		if(!started) {
			synchronized ($start_lock$) {
				if(!started) {
					started = true;

					try {
						fireStartedEvent();

						// In case a started event calls close
						if (started) {
							try {
								Runtime.getRuntime().addShutdownHook(new Thread(this::close));
								$start_lock$.wait();
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}

						started = false;
					} finally {
						fireStoppedEvent();
					}

				}
			}
		}
	}

	private void fireStartedEvent() {
		log.info("Starting app " + applicationClass.getName());

		for (ApplicationListener applicationListener : applicationListeners) {
			try {
				applicationListener.started(this);
			} catch (Exception e) {
				throw new SwitchbladeException("Exception while calling application listener", e);
			}
		}
	}

	private void fireStoppedEvent() {
		log.info("Shutting down app " + applicationClass.getName());

		for (ApplicationListener applicationListener : applicationListeners) {
			try {
				applicationListener.ended(this);
			} catch (Exception e) {
				// allow for clean shutdown.
				// This method is called by a finally block, we need to make sure we don't mask any other exceptions.
				// Besides, we're shutting down anyway.
				log.severe("Exception while calling application listener:\n" + ExceptionUtils.getStackTrace(e));
			}
		}
	}

	@Override
	public void close() {
		if(started) {
			started = false;
			synchronized ($start_lock$) {
				$start_lock$.notifyAll();
			}
		}
	}

	public Injector getObjectGraph() {
		return injector;
	}

	public T getApplication() {
		return application;
	}

	public Class<T> getApplicationClass() {
		return applicationClass;
	}
}
