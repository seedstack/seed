This module provides a support for Expression Language (JSR 245).

    <dependency>
        <groupId>org.seedstack.seed</groupId>
        <artifactId>seed-el-support</artifactId>
    </dependency>

This API is intended for function developers only.

# Provide an annotation with Expression Language

To easily create an annotation which supports EL, the `seed-el-support` provides an `ExpressionLanguageHandler` and 
an `ELBinder`.

Implements an `ExpressionLanguageHandler` as follow:

    @Target({ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MyELAnnotation {

        String value();
    }

    public class MyELHandler implements ExpressionLanguageHandler<MyELAnnotation> {
    
        @Override
        public void handle(Object value) {
            // Get 42
        }
    }
    
    public class MyClass {
    
        @MyELAnnotation("${21*2}")
        public void myMethod() {
            ...
        }
    }

The result and the parameters of the annotated method will be available in the expression language with `${result}` 
and `${args[0]}`.

**Limitation**: the expression language should only be in a method `String value()`.

A new instance of handler will be called each time a method annotated by an EL annotation is called.

In order to intercept the annotation, it should be bind with the ELBinder as follow:

    public class MyModule extends AbstractModule {

        @Override
        protected void configure() {
            // the EL will be evaluated before the method proceed
            new ELBinder(this.binder())
                .bindELAnnotation(MyELAnnotation.class, ELBinder.ExecutionPolicy.BEFORE);
        }
    }
    
The binder takes a policy which defines if the expression language will be executed before, after, or before and after 
the method is proceed.

# Evaluate an EL with the ELService

If you don't need to used EL in an annotation, it is possible to use `ELService` which provides a DSL wrapping the JSR.

Here is some examples of usage:

    Integer response = elService.el("${21*2}", Integer.class).defaultContext().withValueExpression().eval();

Passing properties to the el:

    boolean hasAuthorization = elService.el("${isAuthenticated && isNice}", Boolean.class).defaultContext()
                    .withProperty("isAuthenticated", new User().isAuthenticated)
                    .withProperty("isNice", new User().isNice).withValueExpression().eval();
                    
Passing functions to the el:                    
                   
    double response = elService.el("${math:max(24,42)}", double.class).defaultContext()
                    .withFunction("math", "max", Math.class.getMethod("max", double.class, double.class)).withValueExpression().eval();

# External resources

- [https://uel.java.net/get-started.html](https://uel.java.net/get-started.html)
- Implementation used: [https://github.com/beckchr/juel](https://github.com/beckchr/juel)


