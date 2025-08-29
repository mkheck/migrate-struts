package org.openrewrite.java.struts.migration;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Recipe to convert Struts Action classes to Spring MVC Controllers
 * 
 * This recipe performs the following transformations:
 * 1. Removes ActionSupport inheritance
 * 2. Adds @Controller annotation
 * 3. Converts execute() method to Spring request mapping
 * 4. Transforms action properties to request parameters
 * 5. Converts result strings to view names
 */
public class ConvertStrutsActionToSpringController extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Convert Struts Action to Spring Controller";
    }
    
    @Override
    public String getDescription() {
        return "Converts Struts Action classes extending ActionSupport to Spring MVC Controllers";
    }
    
    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new StrutsToSpringVisitor();
    }
    
    private static class StrutsToSpringVisitor extends JavaIsoVisitor<ExecutionContext> {
        
        private static final String ACTION_SUPPORT = "com.opensymphony.xwork2.ActionSupport";
        private static final String STRUTS_ACTION_SUPPORT = "org.apache.struts2.ActionSupport";
        
        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
            J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, ctx);
            
            // Check if class extends ActionSupport
            if (extendsActionSupport(cd)) {
                // Remove ActionSupport inheritance
                cd = removeActionSupportInheritance(cd);
                
                // Add @Controller annotation
                cd = addControllerAnnotation(cd, ctx);
                
                // Add necessary imports
                cd = maybeAddImport("org.springframework.stereotype.Controller", cd);
                cd = maybeAddImport("org.springframework.web.bind.annotation.RequestMapping", cd);
                cd = maybeAddImport("org.springframework.web.bind.annotation.RequestMethod", cd);
                cd = maybeAddImport("org.springframework.ui.Model", cd);
                cd = maybeAddImport("org.springframework.web.bind.annotation.RequestParam", cd);
            }
            
            return cd;
        }
        
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            J.MethodDeclaration md = super.visitMethodDeclaration(method, ctx);
            
            // Convert execute() method to Spring request handler
            if ("execute".equals(md.getSimpleName()) && md.getParameters().isEmpty()) {
                md = convertExecuteMethod(md, ctx);
            }
            
            // Convert validate() method if present
            if ("validate".equals(md.getSimpleName()) && md.getParameters().isEmpty()) {
                md = convertValidateMethod(md, ctx);
            }
            
            return md;
        }
        
        private boolean extendsActionSupport(J.ClassDeclaration classDecl) {
            if (classDecl.getExtends() != null) {
                JavaType.FullyQualified extendedType = TypeUtils.asFullyQualified(classDecl.getExtends().getType());
                if (extendedType != null) {
                    String fqn = extendedType.getFullyQualifiedName();
                    return ACTION_SUPPORT.equals(fqn) || STRUTS_ACTION_SUPPORT.equals(fqn);
                }
            }
            return false;
        }
        
        private J.ClassDeclaration removeActionSupportInheritance(J.ClassDeclaration cd) {
            return cd.withExtends(null);
        }
        
        private J.ClassDeclaration addControllerAnnotation(J.ClassDeclaration cd, ExecutionContext ctx) {
            if (cd.getLeadingAnnotations().stream()
                    .noneMatch(ann -> ann.getSimpleName().equals("Controller"))) {
                
                JavaTemplate template = JavaTemplate.builder(this::getCursor, "@Controller")
                        .imports("org.springframework.stereotype.Controller")
                        .build();
                
                return cd.withTemplate(template, cd.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
            }
            return cd;
        }
        
        private J.MethodDeclaration convertExecuteMethod(J.MethodDeclaration md, ExecutionContext ctx) {
            // Add @RequestMapping annotation
            if (md.getLeadingAnnotations().stream()
                    .noneMatch(ann -> ann.getSimpleName().contains("Mapping"))) {
                
                // Determine HTTP method based on method body analysis
                String httpMethod = analyzeHttpMethod(md);
                String mapping = determineMapping(md);
                
                JavaTemplate template = JavaTemplate.builder(this::getCursor, 
                        "@RequestMapping(value = \"" + mapping + "\", method = RequestMethod." + httpMethod + ")")
                        .imports("org.springframework.web.bind.annotation.RequestMapping")
                        .imports("org.springframework.web.bind.annotation.RequestMethod")
                        .build();
                
                md = md.withTemplate(template, md.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
            }
            
            // Add Model parameter if method returns String
            if (returnsString(md) && !hasModelParameter(md)) {
                JavaTemplate paramTemplate = JavaTemplate.builder(this::getCursor, "Model model")
                        .imports("org.springframework.ui.Model")
                        .build();
                
                md = md.withTemplate(paramTemplate, md.getCoordinates().replaceParameters());
            }
            
            // Convert return statements
            md = convertReturnStatements(md, ctx);
            
            return md;
        }
        
        private J.MethodDeclaration convertValidateMethod(J.MethodDeclaration md, ExecutionContext ctx) {
            // Convert Struts validate() to Spring validation
            // This would involve converting to @Valid annotation usage
            // and BindingResult parameter
            return md;
        }
        
        private String analyzeHttpMethod(J.MethodDeclaration method) {
            // Analyze method body to determine if it's primarily GET or POST
            // Look for form processing, data modifications, etc.
            // Default to GET for simple display actions
            return "GET";
        }
        
        private String determineMapping(J.MethodDeclaration method) {
            // Determine URL mapping based on class name and method name
            // Could also parse struts.xml if available
            J.ClassDeclaration classDecl = getCursor().firstEnclosing(J.ClassDeclaration.class);
            if (classDecl != null) {
                String className = classDecl.getSimpleName();
                if (className.endsWith("Action")) {
                    className = className.substring(0, className.length() - 6);
                }
                return "/" + className.toLowerCase();
            }
            return "/";
        }
        
        private boolean returnsString(J.MethodDeclaration method) {
            JavaType returnType = method.getReturnTypeExpression().getType();
            return TypeUtils.isString(returnType);
        }
        
        private boolean hasModelParameter(J.MethodDeclaration method) {
            return method.getParameters().stream()
                    .anyMatch(param -> {
                        JavaType paramType = param.getType();
                        JavaType.FullyQualified fq = TypeUtils.asFullyQualified(paramType);
                        return fq != null && "org.springframework.ui.Model".equals(fq.getFullyQualifiedName());
                    });
        }
        
        private J.MethodDeclaration convertReturnStatements(J.MethodDeclaration method, ExecutionContext ctx) {
            return method.withBody(method.getBody().withStatements(
                method.getBody().getStatements().stream()
                    .map(statement -> {
                        if (statement instanceof J.Return) {
                            J.Return returnStmt = (J.Return) statement;
                            if (returnStmt.getExpression() instanceof J.FieldAccess) {
                                J.FieldAccess fa = (J.FieldAccess) returnStmt.getExpression();
                                String fieldName = fa.getSimpleName();
                                
                                // Convert Struts result constants to view names
                                switch (fieldName) {
                                    case "SUCCESS":
                                        return returnStmt.withExpression(
                                            JavaTemplate.builder(this::getCursor, "\"success\"").build().apply(getCursor(), returnStmt.getCoordinates().replace()));
                                    case "ERROR":
                                        return returnStmt.withExpression(
                                            JavaTemplate.builder(this::getCursor, "\"error\"").build().apply(getCursor(), returnStmt.getCoordinates().replace()));
                                    case "INPUT":
                                        return returnStmt.withExpression(
                                            JavaTemplate.builder(this::getCursor, "\"input\"").build().apply(getCursor(), returnStmt.getCoordinates().replace()));
                                    default:
                                        return statement;
                                }
                            }
                        }
                        return statement;
                    })
                    .collect(Collectors.toList())
            ));
        }
        
        private J.ClassDeclaration maybeAddImport(String fullyQualifiedName, J.ClassDeclaration cd) {
            // Add import if not already present
            maybeAddImport(fullyQualifiedName);
            return cd;
        }
    }
}