# switchblade-core
The core to the java autoconfiguration library "Switchblade".

- `mvn install`
- include dependency in another project
- Initialze and start app:

  ```java
  SwitchbladeApplication<TestApplication> switchbladeApplication = SwitchbladeApplication.create(TestApplication.class);
  switchbladeApplication.start();
  ```

- Whatever class is passed in, will have its package scanned for classes annotated with `@Component`.
- Any packages listed in any resources/blade.factory class will also be scanned.
- `@Component` classes will have any method annotated with `@PostConstruct` invoked after they're created.
- `@Component` classes implementing the `BeanListener` interface will have its `postConstruct` invoked immediately after any `@Component`'s `@PostConstruct` method is called.
- `@Component` classes implementing the `ApplicationListener` interface will have the appropriate methods invoked when the app has started or been closed.
- Dependency Injection is handled by Guice. So use the @Inject annotations as you normally would. `@Component` is SINGLETON scoped.

TODO
----

- Need a way to create configurators
- Annotating an annotation with `@Component` should treat that annotation as `@Component`. For example, in swiwtchblade-jetty classes annotated with `@Endpoint` shouldn't need to be annotated with `@Component` as well.
