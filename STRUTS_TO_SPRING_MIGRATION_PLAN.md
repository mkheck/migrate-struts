# Apache Struts to Spring Boot Migration Plan

## Overview
This document provides a comprehensive plan for migrating Apache Struts applications to the latest Spring Boot version. This approach leverages OpenRewrite and Moderne recipes for automated migration where available, and defines new recipes where gaps exist.

## Pre-Migration Analysis Checklist

### ✅ 1. Application Inventory
- [ ] Identify all Struts applications to migrate
- [ ] Document Struts version (currently 7.0.3 in examples)
- [ ] List all modules and their dependencies
- [ ] Identify shared libraries and common components
- [ ] Document external integrations (databases, APIs, etc.)

### ✅ 2. Technology Stack Assessment
- [ ] Current Java version (17)
- [ ] Build tool (Maven/Gradle)
- [ ] Application server requirements
- [ ] Security implementations
- [ ] Session management approach
- [ ] Database connectivity patterns

## Migration Execution Checklist

### Phase 1: Foundation Setup

#### ✅ 1.1 Upgrade to Latest Struts (if needed)
**Recipe**: `org.openrewrite.java.struts.migrate6.MigrateStruts6`
- [ ] Run recipe to upgrade to Struts 6.0 as intermediate step
- [ ] Fix any compilation issues
- [ ] Run tests to ensure functionality

#### ✅ 1.2 Prepare Spring Boot Foundation
**Recipe**: `io.moderne.java.spring.boot.SpringToSpringBoot`
- [ ] Add Spring Boot parent POM/plugin
- [ ] Configure Spring Boot version (3.4.x latest)
- [ ] Set up Spring Boot starter dependencies

### Phase 2: Core Migration

#### ✅ 2.1 Migrate Build Configuration
**Recipe**: `io.moderne.java.spring.boot.MigrateSpringFrameworkDependenciesToSpringBoot`
- [ ] Replace Struts dependencies with Spring Boot starters
- [ ] Add spring-boot-starter-web
- [ ] Add spring-boot-starter-validation
- [ ] Configure Spring Boot Maven/Gradle plugin

#### ✅ 2.2 Convert web.xml to Spring Boot
**Recipe**: `org.openrewrite.spring.webxml.RemoveWebXml`
- [ ] Migrate servlet configurations
- [ ] Convert filters to Spring components
- [ ] Migrate listeners to Spring event listeners
- [ ] Configure Spring Boot application properties

#### ✅ 2.3 Transform Struts Actions to Spring Controllers
**NEW RECIPE NEEDED**: `ConvertStrutsActionToSpringController`
- [ ] Convert ActionSupport classes to @RestController/@Controller
- [ ] Map execute() methods to @RequestMapping methods
- [ ] Transform action results to Spring ModelAndView or ResponseEntity
- [ ] Migrate action properties to method parameters

#### ✅ 2.4 Convert Struts Configuration
**NEW RECIPE NEEDED**: `MigrateStrutsXmlToSpringConfiguration`
- [ ] Parse struts.xml files
- [ ] Convert action mappings to @RequestMapping
- [ ] Transform interceptors to Spring AOP or HandlerInterceptors
- [ ] Migrate result types to Spring view resolvers

#### ✅ 2.5 Migrate Validation Framework
**NEW RECIPE NEEDED**: `MigrateStrutsValidationToSpringValidation`
- [ ] Convert validation XML files to Bean Validation annotations
- [ ] Transform ActionErrors to BindingResult
- [ ] Migrate custom validators to Spring Validators

#### ✅ 2.6 Transform View Layer
**NEW RECIPE NEEDED**: `MigrateStrutsTagsToSpringTags`
- [ ] Replace Struts tags in JSPs with Spring tags
- [ ] Convert OGNL expressions to Spring EL
- [ ] Migrate Tiles configuration if used
- [ ] Update form handling tags

### Phase 3: Advanced Features

#### ✅ 3.1 Migrate Interceptors
**NEW RECIPE NEEDED**: `ConvertStrutsInterceptorToSpringInterceptor`
- [ ] Convert Struts interceptors to HandlerInterceptors
- [ ] Migrate interceptor stacks to InterceptorRegistry
- [ ] Transform parameter interceptors to ArgumentResolvers

#### ✅ 3.2 Session Management
**NEW RECIPE NEEDED**: `MigrateStrutsSessionToSpringSession`
- [ ] Convert SessionAware to @SessionAttributes
- [ ] Migrate session-scoped actions to session-scoped beans
- [ ] Update session timeout configurations

#### ✅ 3.3 Security Migration
**Recipe**: `org.openrewrite.java.spring.security5.WebSecurityConfigurerAdapter`
- [ ] Add Spring Security dependencies
- [ ] Configure authentication providers
- [ ] Set up authorization rules
- [ ] Migrate custom security interceptors

#### ✅ 3.4 Exception Handling
**NEW RECIPE NEEDED**: `MigrateStrutsExceptionToSpringExceptionHandler`
- [ ] Convert global exception mappings to @ControllerAdvice
- [ ] Transform action-specific exception handlers
- [ ] Update error pages configuration

### Phase 4: Testing & Optimization

#### ✅ 4.1 Update Test Framework
- [ ] Convert StrutsTestCase to Spring Boot Test
- [ ] Update mock objects to Spring mocks
- [ ] Migrate integration tests

#### ✅ 4.2 Apply Spring Boot Best Practices
**Recipe**: `io.moderne.java.spring.boot3.SpringBoot3BestPractices`
- [ ] Apply Spring Boot 3.5 best practices
- [ ] Optimize configuration
- [ ] Enable actuator endpoints

#### ✅ 4.3 Performance Optimization
- [ ] Configure connection pooling
- [ ] Set up caching
- [ ] Optimize static resource handling

### Phase 5: Deployment & Monitoring

#### ✅ 5.1 Packaging Configuration
- [ ] Configure executable JAR/WAR
- [ ] Set up profiles for different environments
- [ ] Configure external configuration

#### ✅ 5.2 Monitoring Setup
- [ ] Add Spring Boot Actuator
- [ ] Configure health checks
- [ ] Set up metrics collection

## Existing OpenRewrite Recipes to Use

### Struts Migration
1. `org.openrewrite.java.struts.migrate6.MigrateStruts6` - Upgrade to Struts 6.0
2. `org.openrewrite.java.struts.search.FindStrutsActions` - Locate all Struts actions
3. `org.openrewrite.java.struts.search.FindStrutsXml` - Find Struts configuration files
4. `org.openrewrite.java.struts.MigrateStrutsDtd` - Update DTD versions

### Spring Boot Setup
1. `io.moderne.java.spring.boot.SpringToSpringBoot` - Convert to Spring Boot
2. `org.openrewrite.spring.webxml.RemoveWebXml` - Migrate web.xml
3. `io.moderne.java.spring.boot3.SpringBoot3BestPractices` - Apply best practices
4. `org.openrewrite.java.spring.boot3.SpringBootProperties_3_3` - Update properties

## New OpenRewrite Recipes Required

### 1. ConvertStrutsActionToSpringController
```yaml
type: specs.openrewrite.org/v1beta/recipe
name: org.openrewrite.java.struts.ConvertStrutsActionToSpringController
displayName: Convert Struts Action to Spring Controller
description: Converts Struts Action classes to Spring MVC Controllers
recipeList:
  - org.openrewrite.java.ChangeType:
      oldFullyQualifiedTypeName: com.opensymphony.xwork2.ActionSupport
      newFullyQualifiedTypeName: org.springframework.stereotype.Controller
  - org.openrewrite.java.AddAnnotation:
      annotationType: org.springframework.web.bind.annotation.Controller
  - org.openrewrite.java.ChangeMethodName:
      methodPattern: * execute()
      newMethodName: handleRequest
  - org.openrewrite.java.AddAnnotation:
      annotationType: org.springframework.web.bind.annotation.RequestMapping
      methodPattern: * handleRequest()
```

### 2. MigrateStrutsXmlToSpringConfiguration
```yaml
type: specs.openrewrite.org/v1beta/recipe
name: org.openrewrite.java.struts.MigrateStrutsXmlToSpringConfiguration
displayName: Migrate struts.xml to Spring Configuration
description: Converts Struts XML configuration to Spring annotations
recipeList:
  - Parse struts.xml action mappings
  - Generate @RequestMapping annotations
  - Create Spring configuration classes
  - Map result types to view resolvers
```

### 3. MigrateStrutsValidationToSpringValidation
```yaml
type: specs.openrewrite.org/v1beta/recipe
name: org.openrewrite.java.struts.MigrateStrutsValidationToSpringValidation
displayName: Migrate Struts Validation to Spring Validation
description: Converts Struts validation to Bean Validation
recipeList:
  - Parse validation XML files
  - Add Jakarta Bean Validation annotations
  - Convert to @Valid annotations on controllers
  - Transform custom validators
```

### 4. MigrateStrutsTagsToSpringTags
```yaml
type: specs.openrewrite.org/v1beta/recipe
name: org.openrewrite.java.struts.MigrateStrutsTagsToSpringTags
displayName: Migrate Struts Tags to Spring Tags
description: Converts Struts JSP tags to Spring tags
recipeList:
  - Replace <%@ taglib prefix="s" uri="/struts-tags" %>
  - Convert <s:form> to <form:form>
  - Transform <s:textfield> to <form:input>
  - Update OGNL expressions to Spring EL
```

### 5. ConvertStrutsInterceptorToSpringInterceptor
```yaml
type: specs.openrewrite.org/v1beta/recipe
name: org.openrewrite.java.struts.ConvertStrutsInterceptorToSpringInterceptor
displayName: Convert Struts Interceptor to Spring HandlerInterceptor
description: Transforms Struts interceptors to Spring interceptors
recipeList:
  - Implement HandlerInterceptor interface
  - Convert intercept() to preHandle/postHandle
  - Register in WebMvcConfigurer
  - Map interceptor stacks
```

### 6. MigrateStrutsSessionToSpringSession
```yaml
type: specs.openrewrite.org/v1beta/recipe
name: org.openrewrite.java.struts.MigrateStrutsSessionToSpringSession
displayName: Migrate Struts Session to Spring Session
description: Converts Struts session handling to Spring
recipeList:
  - Replace SessionAware with @SessionAttributes
  - Convert session maps to HttpSession
  - Update session scope configurations
```

### 7. MigrateStrutsExceptionToSpringExceptionHandler
```yaml
type: specs.openrewrite.org/v1beta/recipe
name: org.openrewrite.java.struts.MigrateStrutsExceptionToSpringExceptionHandler
displayName: Migrate Struts Exception to Spring ExceptionHandler
description: Converts Struts exception handling to Spring
recipeList:
  - Create @ControllerAdvice classes
  - Convert global exception mappings
  - Transform to @ExceptionHandler methods
  - Update error view mappings
```

## Migration Execution Order

1. **Analyze**: Run FindStrutsActions and FindStrutsXml recipes
2. **Prepare**: Upgrade to Struts 6.0 if needed
3. **Foundation**: Add Spring Boot dependencies
4. **Core Migration**: Execute action and configuration transformations
5. **View Layer**: Migrate JSPs and tags
6. **Advanced Features**: Convert interceptors and security
7. **Testing**: Update and run all tests
8. **Optimization**: Apply Spring Boot best practices
9. **Deploy**: Package and deploy to target environment

## Post-Migration Validation

- [ ] All endpoints accessible and functional
- [ ] Authentication and authorization working
- [ ] Form submissions and validations operational
- [ ] Session management functioning correctly
- [ ] Exception handling and error pages working
- [ ] Performance meets or exceeds original
- [ ] All integration tests passing
- [ ] Security scan completed
- [ ] Load testing performed
- [ ] Documentation updated

## Notes

- Each Struts application module should be migrated incrementally
- Maintain parallel deployments during transition
- Use feature toggles for gradual rollout
- Keep detailed migration logs for troubleshooting
- Consider containerization with Docker for deployment
- Plan for Spring Boot 3.x Jakarta EE namespace changes

## Resources

- [Spring Boot Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [OpenRewrite Struts Recipes](https://docs.openrewrite.org/recipes/java/struts)
- [Moderne Platform Documentation](https://docs.moderne.io/)
- [Spring Framework Documentation](https://docs.spring.io/spring-framework/reference/)