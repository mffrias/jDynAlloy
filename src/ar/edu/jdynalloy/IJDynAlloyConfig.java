package ar.edu.jdynalloy;

import java.util.List;

import ar.edu.jdynalloy.JDynAlloyConfig.LoopResolutionEnum;

public interface IJDynAlloyConfig {

    public List<String> getBuiltInModules();

    public String getMethodToCheck();

    public String getClassToCheck();

    public boolean getDynamicJavaLangFields();

    public String getProject();

    public String[] getClasses();

    public String getOutput();

    public boolean getClassExtendsObject();

    public boolean getTypeSafety();

    public String getOutputDynJAlloy();

    public boolean getDynJAlloyAnnotations();

    public boolean getQuantifierIncludesNull();

    public boolean getCheckNullDereference();
    
    public boolean getCheckDivisionByZero();

    public boolean getAbstractSignatureObject();

    public boolean hasClassToCheck();

    public boolean hasMethodToCheck();

    public boolean getUseClassSingletons();

    public boolean getIgnoreJmlAnnotations();

    public boolean getIgnoreJsflAnnotations();

    public boolean getAssertIsAssume();

    public LoopResolutionEnum getLoopResolution();

    public boolean getSimPredicates();

    public boolean getUseCustomRelationalOverride();

    public List<String> getRelevantClasses();
    
    public boolean getUseQualifiedNamesForJTypes(); 

    public boolean getSkolemizeInstanceInvariant();
    
    public boolean getSkolemizeInstanceAbstraction(); 

    public boolean getJMLObjectSequenceToAlloySequence();
    
    public boolean getNewExceptionsAreLiterals();
    
    public boolean getJMLObjectSetToAlloySet();
    
    public String getAssertionArguments();

    public boolean getGenerateCheck();

    public boolean getGenerateRun(); 
    
    public boolean getArrayIsInt();
    
    public boolean getModularReasoning();

    public boolean getIncludeSimulationProgramDeclaration();
    
    public String getOutputModularJDynalloy();
    
    public boolean getIncludeBranchLabels();
    
    public boolean getDisableIntegerLiteralReduction();
    
	public boolean getNestedLoopUnroll();

}
