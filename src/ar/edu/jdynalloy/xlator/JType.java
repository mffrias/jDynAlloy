package ar.edu.jdynalloy.xlator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.edu.jdynalloy.factory.JTypeFactory;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.dynalloy.visualization.utils.StringUtils;

/**
 * The type of a JDynAlloy variable, field or expression: a signature or a union of signatures,
 * a set, a sequence, a binary relation (the type of a field), or a special type. TACO builds
 * types with {@link #parse(String)}, the constructors and {@code JTypeFactory}; the parser of
 * {@code .djals} files parses every type it reads with {@link #parse(String)}; the bindings and
 * symbol tables compare them ({@link #equals(Object)}); and the translation prints them
 * ({@link #toString()}), as the types of the variables of the DynAlloy output
 * ({@code JDynAlloyTyping.put}). See docs/LANGUAGE.md for where types appear.
 *
 * <p>Representation. A type has a domain and an image, lists of signature ids (sorted when the
 * type is built from sets), and flags:
 * <ul>
 * <li>a signature or a union {@code A+B}: the domain, no image;</li>
 * <li>a set {@code set A}: the domain, with the set flag or the left multiplier {@code set};</li>
 * <li>a sequence {@code seq A}: the sequence flag, with its elements as the domain; a relation
 *     from exactly {@code Int} is built as a sequence of its image;</li>
 * <li>a binary relation {@code A->B}: domain and image, with the function flag for
 *     {@code ->one} and for {@code set->lone}, and the two multipliers for the latter; a field
 *     whose values are sequences ({@code A->seq B}) has the image-is-sequence flag;</li>
 * <li>a special type ({@link SpecialType}): the static constants, such as
 *     {@code ALLOCATED_OBJECTS_TYPE} ({@code set java_lang_Object}, the type of
 *     {@code usedObjects}) and {@code INT_ARRAY_CONTAINS_TYPE}, and the new ones {@code parse}
 *     builds for {@code java_util_Set->univ} and {@code java_util_List->Int->univ}. The domain
 *     is the container's signature or empty, and the image is empty, or {@code null} for those
 *     two.</li>
 * </ul>
 * {@link #isTernaryRelation()} is true only for {@code MAP_ENTRIES}: the private constructor
 * that would build other ternary types is not used.
 *
 * <p>Printing ({@link #toString()}): {@code A+B} for a union, in the domain's order,
 * {@code set(A)}, {@code seq(A)}, {@code (A)->(B)}, {@code (A)->one(B)},
 * {@code (A) set -> lone (B)}, {@code (A)->(seq(B))}, and a fixed text for each special type.
 * Not all of them parse back (see {@link #parse(String)}).
 *
 * <p>Equality ({@link #equals(Object)} and {@code hashCode}) compares the domain and the image
 * as lists, the function and sequence flags and the special type; not the set flag, the
 * multipliers or the image-is-sequence flag. So {@code set A} equals {@code A}, and
 * {@code A set->lone B} equals {@code A->one B}. The lists are compared in order: the
 * constructors that take sets sort them, the one that takes lists does not.
 *
 * <p>Configuration. {@code parse}, {@code dpdTypeNameExtract}, {@code to} when the image is
 * empty, and {@code toString} and {@code used_types} of special types read
 * {@code JDynAlloyConfig.getInstance()}
 * ({@code useQualifiedNamesForJTypes}, for the {@code java_lang_} and {@code java_util_}
 * prefixes of some special types; {@code JMLObjectSequenceToAlloySequence}), so they throw an
 * {@code IllegalStateException} before the configuration is set. {@code JSignatureFactory}
 * parses its type constants when it is initialised, and this class's initialisation triggers
 * that ({@code NULL_VALUE_SIGNATURE_ID}), so neither class can be initialised before the
 * configuration.
 *
 * <p>Static state: the constants, built once per JVM and shared by every analysis. Types are
 * mutable ({@link #setAsNonNull()}); nothing resets the constants.
 */
public final class JType {

	private List<String> dom;
	private final List<String> img;
	private final boolean isSequence;
	private final boolean isFunction;
	private boolean isSet = false;
	private final SpecialType specialType;
	private boolean imageIsSequence = false;
	private enum Multiplier {
		SET_MULTIPLIER, LONE_MULTIPLIER, ONE_MULTIPLIER;

		public String toString() {
			switch (this) {
			case SET_MULTIPLIER:
				return "set";
			case LONE_MULTIPLIER:
				return "lone";
			case ONE_MULTIPLIER:
				return "one";
			}
			return null;
		}
	}

	private Multiplier left_arrow_multiplier = null;
	private Multiplier right_arrow_multiplier = null;

	private JType ternary_type = null;

//	public List<String> getImg(){
//		if (isBinaryRelation()){
//			return img;
//		} else {
//			return null;
//		}
//	}

	/**
	 * The special types: relations of the built-in modules that a domain and an image cannot
	 * describe. {@code ALLOCATED_OBJECT} is {@code set java_lang_Object}, the type of
	 * {@code usedObjects}; {@code SYSTEM_ARRAY} is {@code java_lang_SystemArray->(seq univ)};
	 * {@code SET_CONTAINS}, {@code MAP_ENTRIES}, {@code ALLOY_LIST_CONTAINS},
	 * {@code JML_OBJECTSET_CONTAINS}, {@code JML_OBJECTSEQUENCE_CONTAINS} and
	 * {@code ITERATOR_CONTAINS} hold the contents of sets, maps, lists, JML sets and sequences and
	 * iterators; {@code OBJECT_ARRAY_CONTAINS}, {@code INT_ARRAY_CONTAINS},
	 * {@code LONG_ARRAY_CONTAINS} and {@code CHAR_ARRAY_CONTAINS} the contents of arrays, indexed by
	 * {@code JavaPrimitiveIntegerValue}, and the two {@code ALLOY_*_ARRAY_CONTAINS} those indexed by
	 * {@code Int}; {@code UNIV_TO_UNIV} is {@code univ->univ}.
	 */
	public enum SpecialType {
		ALLOCATED_OBJECT, SYSTEM_ARRAY, SET_CONTAINS, MAP_ENTRIES, ALLOY_LIST_CONTAINS, JML_OBJECTSET_CONTAINS, JML_OBJECTSEQUENCE_CONTAINS, 
		ITERATOR_CONTAINS, UNIV_TO_UNIV, OBJECT_ARRAY_CONTAINS, INT_ARRAY_CONTAINS, LONG_ARRAY_CONTAINS, ALLOY_OBJECT_ARRAY_CONTAINS, ALLOY_INT_ARRAY_CONTAINS, CHAR_ARRAY_CONTAINS
	};


	public static final String NULL_VALUE_SIGNATURE_ID = JSignatureFactory.NULL
			.getSignatureId();

	public static final String INT_SIGNATURE_ID = JSignatureFactory.INT
			.getSignatureId();

	public final static JType ALLOCATED_OBJECTS_TYPE = new JType(
			SpecialType.ALLOCATED_OBJECT);

	public static final JType SYSTEM_ARRAY_TYPE = new JType(
			SpecialType.SYSTEM_ARRAY);

	public static final JType SET_CONTAINS_TYPE = new JType(
			SpecialType.SET_CONTAINS);

	public static final JType MAP_ENTRIES_TYPE = new JType(
			SpecialType.MAP_ENTRIES);

	//	private static final JType ALLOY_LIST_CONTAINS_TYPE = new JType(
	//			SpecialType.ALLOY_LIST_CONTAINS);

	public static final JType JML_OBJECTSET_CONTAINS_TYPE = new JType(
			SpecialType.JML_OBJECTSET_CONTAINS);

	public static final JType JML_OBJECTSEQUENCE_CONTAINS_TYPE = new JType(
			SpecialType.JML_OBJECTSEQUENCE_CONTAINS);

	public static final JType ITERATOR_CONTAINS_TYPE = new JType(
			SpecialType.ITERATOR_CONTAINS);

	public static final JType OBJECT_ARRAY_CONTAINS_TYPE = new JType(
			SpecialType.OBJECT_ARRAY_CONTAINS, "java_lang_ObjectArray");

	public static final JType INT_ARRAY_CONTAINS_TYPE = new JType(
			SpecialType.INT_ARRAY_CONTAINS, "java_lang_IntArray");

	public static final JType LONG_ARRAY_CONTAINS_TYPE = new JType(
			SpecialType.LONG_ARRAY_CONTAINS, "java_lang_LongArray");

	public static final JType ALLOY_OBJECT_ARRAY_CONTAINS_TYPE = new JType(
			SpecialType.ALLOY_OBJECT_ARRAY_CONTAINS, "java_lang_ObjectArray");

	public static final JType ALLOY_INT_ARRAY_CONTAINS_TYPE = new JType(
			SpecialType.ALLOY_INT_ARRAY_CONTAINS, "java_lang_IntArray");
	
	public static final JType CHAR_ARRAY_CONTAINS_TYPE = new JType(
			SpecialType.CHAR_ARRAY_CONTAINS, "java_lang_CharArray");

	/**
	 * Parses a type as written in {@code .djals} files and by TACO. All parentheses are removed
	 * first, and {@code "set -> lone"}, {@code "-> one"} and {@code "-> seq"} lose their spaces;
	 * then the first rule that applies, in this order:
	 * <ol>
	 * <li>{@code A set->lone B}, with no other arrow: a function with those multipliers; neither
	 *     side is split at {@code +};</li>
	 * <li>{@code none->none}: {@code UNIV_TO_UNIV}, which prints as {@code univ->univ};</li>
	 * <li>anything that starts with {@code seq}: a sequence of the signatures of the rest, parsed;
	 *     so a signature whose name starts with {@code seq} is taken for a sequence;</li>
	 * <li>exact texts, spaces included, for the special types: the contents of object, int, char
	 *     and long arrays indexed by {@code JavaPrimitiveIntegerValue}, and of object and int arrays
	 *     indexed by {@code Int} (such as {@code java_lang_IntArray->(Int set->lone Int)});
	 *     {@code set java_lang_Object}; {@code java_lang_SystemArray->seq univ};
	 *     {@code java_util_Set->univ};
	 *     {@code java_util_Map->java_lang_Object set->lone java_lang_Object+null};
	 *     {@code java_util_List->Int->univ}; {@code org_jmlspecs_models_JMLObjectSet->set univ};
	 *     and {@code java_util_Iterator->univ}. The text for {@code JML_OBJECTSEQUENCE_CONTAINS}
	 *     has parentheses, so it never matches;</li>
	 * <li>{@code set A}: a set of {@code A}, not split at {@code +};</li>
	 * <li>{@code A->oneB}, that is {@code (A)->one(B)}: a function; both sides are split at
	 *     {@code +};</li>
	 * <li>{@code A->seqB}: a field of {@code A} whose values are sequences of {@code B}
	 *     ({@code JTypeFactory.buildFieldSeq}); {@code A} must be one signature (an
	 *     {@code assert});</li>
	 * <li>{@code A->B}: a relation that is not a function; both sides are split at {@code +}, and
	 *     whatever follows a second arrow is dropped;</li>
	 * <li>otherwise a signature, or a union split at {@code +}.</li>
	 * </ol>
	 * In rules 1, 6 and 8, a domain of exactly {@code Int} gives a sequence of the image instead.
	 * In rule 4, the {@code java_lang_} of {@code java_lang_Object} and
	 * {@code java_lang_SystemArray} and the {@code java_util_} prefixes depend on
	 * {@code useQualifiedNamesForJTypes} (TACO's is true); those of the arrays are fixed.
	 *
	 * <p>What {@link #toString()} prints does not always parse back: {@code set(A)} becomes the
	 * signature {@code setA}, and most special types print a text that matches no rule 4 text and
	 * falls to rule 8.
	 *
	 * @param source the type
	 * @return the type; for most rule 4 texts, the shared constant
	 * @throws IllegalStateException if {@code JDynAlloyConfig} is not set, for any text that rules
	 *         1 to 3 and the first text of rule 4 do not take
	 */
	public static JType parse(String source) {
//		if (source.contains("Classfields")){
//			System.out.println("HERE");
//		}
		String str = source.replaceAll("\\(", "").replaceAll("\\)", "");
		str = str.replace("set -> lone","set->lone");
		str = str.replace("-> one","->one");
		str = str.replace("-> seq","->seq");

		if (str.contains("set->lone") && str.split("->").length == 2) {

			String from = str.split("set->lone")[0].trim();
			String to = str.split("set->lone")[1].trim();

			return new JType(from, Multiplier.SET_MULTIPLIER,
					Multiplier.LONE_MULTIPLIER, to);

			//mfrias-mffrias-12/11/2012: Java fields are mapped to binary alloy fields. Ternary ones are used to
			//map special distinguished types. I will treat those as special.			
			//		} else if (str.split("->").length == 3) {
			//			String[] parts = str.split("->");
			//			String from = parts[0];
			//			String to = parts[1] + "->" + parts[2];
			//			
			//			JType to_type = JType.parse(to);
			//			JType from_type = JType.parse(from);
			//
			//			return new JType(from_type.singletonFrom(), to_type);
		} else if (str.equals("none->none")) {

			return new JType(SpecialType.UNIV_TO_UNIV);

		} else if (str.startsWith("seq")) {

			JType domType = JType.parse(str.substring("seq".length()));
			Set<String> from = domType.from();
			JType seqType = new JType(true, Collections
					.singleton(INT_SIGNATURE_ID), from);
			return seqType;
		} else if (str.equals("java_lang_ObjectArray->Int set->lone java_lang_Object+null")) {
			return JType.ALLOY_OBJECT_ARRAY_CONTAINS_TYPE;
		} else if (str.equals("set " + javaLangPackage() + "Object"))
			return JType.ALLOCATED_OBJECTS_TYPE;
		else if (str.equals("" + javaLangPackage() + "SystemArray->seq univ"))
			return JType.SYSTEM_ARRAY_TYPE;
		else if (str.equals("java_lang_ObjectArray->JavaPrimitiveIntegerValue set->lone java_lang_Object+null"))
			return JType.OBJECT_ARRAY_CONTAINS_TYPE;
//		else if (str.equals("java_lang_ObjectArray->one  seq java_lang_Object+null"))
//			return JType.ALLOY_OBJECT_ARRAY_CONTAINS_TYPE;
		else if (str.equals("java_lang_IntArray->JavaPrimitiveIntegerValue set->lone JavaPrimitiveIntegerValue"))
			return JType.INT_ARRAY_CONTAINS_TYPE;
		else if (str.equals("java_lang_CharArray->JavaPrimitiveIntegerValue set->lone JavaPrimitiveCharValue"))
			return JType.CHAR_ARRAY_CONTAINS_TYPE;
		else if (str.equals("java_lang_LongArray->JavaPrimitiveIntegerValue set->lone JavaPrimitiveLongValue"))
			return JType.LONG_ARRAY_CONTAINS_TYPE;
		else if (str.equals("java_lang_IntArray->Int set->lone Int"))
			return JType.ALLOY_INT_ARRAY_CONTAINS_TYPE;
		else if (str.equals("" + javaUtilPackage() + "Set->univ"))
			return new JType(SpecialType.SET_CONTAINS, javaUtilPackage() + "Set", "null");
		else if (str.equals("" + javaUtilPackage() + "Map->" + javaLangPackage() + "Object set->lone " + javaLangPackage() + "Object+null"))
			return JType.MAP_ENTRIES_TYPE;
		else if (str.equals("" + javaUtilPackage() + "List->Int->univ")) 
			return new JType(SpecialType.ALLOY_LIST_CONTAINS, javaUtilPackage() + "List", "null");
		else if (str.equals("org_jmlspecs_models_JMLObjectSet->set univ"))
			return JType.JML_OBJECTSET_CONTAINS_TYPE;
		else if (str.equals("org_jmlspecs_models_JMLObjectSequence->(Int -> univ)"))
			return JType.JML_OBJECTSEQUENCE_CONTAINS_TYPE;
		else if (str.equals("" + javaUtilPackage() + "Iterator->univ"))
			return JType.ITERATOR_CONTAINS_TYPE;
		else if (str.startsWith("set ")) {
			String setOf = str.substring("set ".length());
			JType t = new JType(Collections.<String> singleton(setOf));
			t.isSet = true;
			return t;
		} else if (str.contains("->one"))  {
			String[] fromTo = str.split("->one");

			String[] from = new String[] { fromTo[0] };
			if (fromTo[0].contains("+"))
				from = fromTo[0].split("\\+");

			String[] to = new String[] { fromTo[1] };
			if (fromTo[1].contains("+"))
				to = fromTo[1].split("\\+");

			return new JType(true, toTrimSet(from), toTrimSet(to));
		} else if (str.contains("->seq")) {
			String[] fromTo = str.split("->seq");

			String[] from = new String[] { fromTo[0] };
			if (fromTo[0].contains("+"))
				from = fromTo[0].split("\\+");

			// DPD Only supported for From with only a parameter
			assert (from.length == 1);

			String[] to = new String[] { fromTo[1] };
			if (fromTo[1].contains("+"))
				to = fromTo[1].split("\\+");

			// return new JType(true, toTrimSet(from), toTrimSet(to));
			return JTypeFactory
					.buildFieldSeq(from[0], new JType(toTrimSet(to)));
		} else if (str.contains("->")) {
			String[] fromTo = str.split("->");

			String[] from = new String[] { fromTo[0] };
			if (fromTo[0].contains("+"))
				from = fromTo[0].split("\\+");

			String[] to = new String[] { fromTo[1] };
			if (fromTo[1].contains("+"))
				to = fromTo[1].split("\\+");

			return new JType(false, toTrimSet(from), toTrimSet(to));
		} else {
			String[] from = new String[] { str };
			if (str.contains("+"))
				from = str.split("\\+");
			return new JType(toTrimSet(from));
		}
	}



	/**
	 * Creates the type of one signature.
	 *
	 * @param signatureId the signature id
	 */
	public JType(String signatureId) {
		this(Collections.<String> singleton(signatureId));
	}

	/**
	 * Creates a signature or a union of signatures; the signatures are sorted.
	 *
	 * @param signatures the signature ids
	 */
	public JType(Set<String> signatures) {
		super();
		dom = sortList(signatures);
		img = new LinkedList<String>();
		isSequence = false;
		isFunction = false;
		specialType = null;
	}


	/**
	 * Creates a function from one signature to another, as {@link #JType(Set, Set)} does (a
	 * sequence if {@code from} is {@code Int}).
	 *
	 * @param from the domain's signature id
	 * @param to the image's signature id
	 */
	public JType(String from, String to) {
		this(Collections.<String> singleton(from), Collections
				.<String> singleton(to));
	}

	private JType(String from, Multiplier from_multiplier,
			Multiplier to_multiplier, String to) {
		this(from, to);
		left_arrow_multiplier = from_multiplier;
		right_arrow_multiplier = to_multiplier;
	}


	/**
	 * Creates a union of signatures, or a set of them if {@code isSet}. The set is marked by the
	 * left multiplier, not by the set flag; it prints as {@code set(A+B)}.
	 *
	 * @param isSet true for a set
	 * @param domain the signature ids; they are sorted
	 */
	public JType(boolean isSet, Set<String> domain) {
		this(domain);
		if (isSet)
			left_arrow_multiplier = Multiplier.SET_MULTIPLIER;
		else
			left_arrow_multiplier = null;

		right_arrow_multiplier = null;
	}

	/**
	 * Creates a function from the union {@code from} to the union {@code to}, printed
	 * {@code (A)->one(B)}; if {@code from} is exactly {@code {Int}}, a sequence of {@code to}
	 * instead. Both are sorted.
	 *
	 * @param from the domain's signature ids
	 * @param to the image's signature ids
	 */
	public JType(Set<String> from, Set<String> to) {
		//		super();
		if ((from.size() == 1) && (from.contains(INT_SIGNATURE_ID))) {
			isSequence = true;
			isFunction = false;
			dom = sortList(to);
			img = new LinkedList<String>();
		} else {
			dom = sortList(from);
			img = sortList(to);
			isSequence = false;
			isFunction = true;
		}
		specialType = null;
	}

	/**
	 * Creates a type from its parts, as they are: the lists are neither copied nor sorted (equality
	 * then depends on their order), and there are no multipliers. {@code JTypeFactory.buildFieldSeq}
	 * uses it for fields whose values are sequences.
	 *
	 * @param dom the domain
	 * @param img the image
	 * @param imageIsSequence true for a field whose values are sequences
	 * @param isFunction the function flag
	 * @param isSequence the sequence flag
	 * @param isSet the set flag
	 * @param specialType the special type, or null
	 */
	public JType(List<String> dom, List<String> img, boolean imageIsSequence,
			boolean isFunction, boolean isSequence, boolean isSet,
			SpecialType specialType) {
		super();
		this.dom = dom;
		this.img = img;
		this.imageIsSequence = imageIsSequence;
		this.isFunction = isFunction;
		this.isSequence = isSequence;
		this.isSet = isSet;
		this.specialType = specialType;
	}

	private JType(SpecialType _specialType) {
		dom = new LinkedList<String>();
		img = new LinkedList<String>();
		isSequence = false;
		isFunction = false;
		isSet = false;
		specialType = _specialType;
	}

	private JType(SpecialType _specialType, String from, String to) {
		dom = new LinkedList<String>(Collections.<String> singleton(from));
		img = new LinkedList<String>(Collections.<String> singleton(to));
		isSequence = false;
		isFunction = false;
		isSet = false;
		specialType = _specialType;
	}


	private JType(boolean isFunction, Set<String> from, Set<String> to) {
		super();
		if ((from.size() == 1) && (from.contains(INT_SIGNATURE_ID))) {
			this.isSequence = true;
			this.isFunction = false;
			this.dom = sortList(to);
			this.img = new LinkedList<String>();
		} else {
			this.dom = sortList(from);
			this.img = sortList(to);
			this.isFunction = isFunction;
			this.isSequence = false;
			this.isSet = false;
		}
		this.specialType = null;
	}

	private JType(String singletonFrom, JType toType) {
		this(singletonFrom);
		ternary_type = toType;
	}


	/**
	 * Creates a special type whose domain is the container's signature, with no image; the
	 * constants for the contents of arrays are built so.
	 *
	 * @param specialType the special type
	 * @param singletonFrom the container's signature id
	 */
	public JType(SpecialType specialType, String singletonFrom) {
		this.specialType = specialType;
		this.dom = new ArrayList<String>();
		dom.add(singletonFrom);
		this.img = new ArrayList<String>();
		this.isSequence = false;
		this.isFunction = false;
	}



	public boolean isSpecialType() {
		return this.specialType != null;
	}

	public SpecialType getSpecialType(){
		return this.specialType;
	}

	/**
	 * Tells whether this is a relation: true when the image is not empty, and for every special type
	 * but {@code MAP_ENTRIES}, including {@code ALLOCATED_OBJECT} (a set), {@code UNIV_TO_UNIV} and
	 * the contents of arrays (ternary). False for a sequence.
	 *
	 * @return true for a relation
	 */
	public boolean isBinaryRelation() {
		return ((this.img!=null && this.img.size() > 0) || (this.specialType != null && !specialType.equals(SpecialType.MAP_ENTRIES)));
	}

	public boolean isTernaryRelation() {
		return (specialType != null && 
				specialType.equals(SpecialType.MAP_ENTRIES))
				|| (ternary_type != null);
	}

	/**
	 * Returns the domain, as a new set: the signatures of a signature, union or set, the elements of
	 * a sequence, the left side of a relation, the container of a special type (none for the
	 * constants other than those of arrays).
	 *
	 * @return the signature ids
	 */
	public Set<String> from() {
		return new HashSet<String>(dom);
	}

	/**
	 * Returns the image, as a new set. When the image is empty, the test for the exception parses
	 * a type, so it reads the configuration.
	 *
	 * @return the signature ids; empty for {@code ALLOY_INT_ARRAY_CONTAINS_TYPE}
	 * @throws IllegalStateException if the image is empty ("Cannot ask for image when type ... is
	 *         not a relation."), except for {@code ALLOY_INT_ARRAY_CONTAINS_TYPE}
	 */
	public Set<String> to() {
		if (img.isEmpty() && !this.equals(JType.parse("java_lang_IntArray->(Int set->lone Int)")))
			throw new IllegalStateException("Cannot ask for image when type "
					+ this.toString() + " is not a relation.");

		Set<String> toSignatures = new HashSet<String>(img);
		return toSignatures;
	}

	/**
	 * Returns a signature of the domain other than {@code null}: the first one {@link #from()}
	 * yields, a {@code HashSet}, so with several the choice is arbitrary.
	 *
	 * @return the signature id, or {@code "null"} if that is the whole domain
	 * @throws java.util.NoSuchElementException if the domain is empty
	 */
	public String singletonFrom() {
		for (String elem : from()) {
			if (!elem.equals(NULL_VALUE_SIGNATURE_ID)) {
				return elem;
			}
		}
		return from().iterator().next();
	}

	/**
	 * Returns a signature of the image other than {@code null}, as {@link #singletonFrom()} does for
	 * the domain: for a field, the type of its values.
	 *
	 * @return the signature id, or {@code "null"} if that is the whole image
	 * @throws IllegalStateException if the image is empty (see {@link #to()})
	 */
	public String singletonTo() {
		for (String elem : to()) {
			if (!elem.equals(NULL_VALUE_SIGNATURE_ID)) {
				return elem;
			}
		}
		return to().iterator().next();
	}

	/**
	 * Tells whether this is a set: parsed from {@code set A}, or built with
	 * {@code JType(true, ...)}. Also true for a relation {@code A set->lone B}, whose left
	 * multiplier is {@code set}; false for {@code ALLOCATED_OBJECTS_TYPE}, which prints as
	 * {@code set (java_lang_Object)}.
	 *
	 * @return true for a set
	 */
	public boolean isSet() {
		return isSet || this.left_arrow_multiplier==Multiplier.SET_MULTIPLIER;
	}

	public boolean isSequence() {
		return isSequence;
	}

	/**
	 * Tells whether this is a relation to sequences: a field whose values are sequences
	 * ({@code A->seq B}), or {@code SYSTEM_ARRAY}.
	 *
	 * @return true for such a relation
	 */
	public boolean isBinRelWithSeq() {
		if (specialType != null
				&& specialType == SpecialType.SYSTEM_ARRAY)
			return true;

		if (this.imageIsSequence)
			return true;

		return false;
	}


	/**
	 * Tells whether this holds the contents of a collection: of a JML set or sequence, and also, in
	 * spite of the name, of a {@code java_util_Set} or {@code java_util_List}
	 * ({@code SET_CONTAINS}, {@code ALLOY_LIST_CONTAINS}).
	 *
	 * @return true for those four special types
	 */
	public boolean isJML() {
		if (specialType != null 
				&& (specialType == SpecialType.JML_OBJECTSET_CONTAINS
				|| specialType == SpecialType.JML_OBJECTSEQUENCE_CONTAINS
				|| specialType == SpecialType.ALLOY_LIST_CONTAINS
				|| specialType == SpecialType.SET_CONTAINS)){
			return true;
		}
		return false;
	}

	/**
	 * Tells whether this is a relation from {@code null} alone: the domain is just {@code null} and
	 * {@link #isBinaryRelation()} holds. So the type of the literal {@code null}
	 * ({@code new JType("null")}, a signature) is not {@code isNull()}, although
	 * {@code CallBindingResolver.isAssignable} calls it to let {@code null} fit every type ("null is
	 * compatible with everytype").
	 *
	 * @return true for a relation from {@code null}
	 */
	public boolean isNull() {
		return this.from().size() == 1 && this.isBinaryRelation()
				&& this.from().contains(NULL_VALUE_SIGNATURE_ID);
	}

	/**
	 * Removes {@code null} and {@code AssertionFailure} from the domain, in place; the image is
	 * kept. The hash code changes, so a type changed while it is in a hash set or map (or in the
	 * parameters of a {@code JProgramDeclaration} that is) is no longer found there; and on a shared
	 * type ({@code JSignatureFactory}'s or this class's constants) the change would last for the
	 * rest of the JVM. TACO's {@code JmlExpressionVisitor} and {@code ReplaceModifiesModuleVisitor}
	 * call it.
	 */
	public void setAsNonNull() {
		List<String> newDom = new ArrayList<String>();
		for (String type : this.dom) {
			if (!type.equals(NULL_VALUE_SIGNATURE_ID)
					&& !type.equals("AssertionFailure")) {
				newDom.add(type);
			}
		}

		this.dom = newDom;
	}

	/**
	 * Returns a signature or union as an Alloy expression: one constant, or the union of the
	 * constants in the domain's order.
	 *
	 * @return the expression
	 * @throws IllegalStateException for a special type, a set, a sequence or a relation
	 * @throws IllegalArgumentException if the domain is empty
	 */
	public AlloyExpression toAlloyExpr() {
		if (specialType != null) {
			throw new IllegalStateException(
					"JType::toAlloyExpr() not supported when JType is special type");
		} else {
			if (isSet()) {
				throw new IllegalStateException(
						"JType::toAlloyExpr() not supported when JType is set");
			} else if (isSequence())
				throw new IllegalStateException(
						"JType::toAlloyExpr() not supported when JType is seq");
			else if (isBinaryRelation()) {
				throw new IllegalStateException(
						"JType::toAlloyExpr() not supported when JType is binary relation");
			} else {
				Vector<ExprConstant> constants = new Vector<ExprConstant>();
				for (String domStr : this.dom) {
					ExprConstant constant = new ExprConstant(null, domStr);
					constants.add(constant);
				}
				if (constants.isEmpty())
					throw new IllegalArgumentException(
							"cannot invoke toAlloyExpr on an empty JTyte");

				if (constants.size() == 1) {
					return constants.firstElement();
				} else
					return ExprUnion
							.buildExprUnion(constants
									.<AlloyExpression> toArray(new AlloyExpression[] {}));
			}
		}
	}

	/**
	 * Returns the signature ids the type mentions: the domain and the image (so {@code null} for
	 * {@code A+null}, and not {@code Int} for a sequence); for a special type, a fixed set: the
	 * container and {@code univ}, {@code Int} or the {@code JavaPrimitive*Value} signatures it
	 * uses, and nothing for {@code UNIV_TO_UNIV}.
	 *
	 * @return a new set
	 */
	public Set<String> used_types() {
		Set<String> used_types = new HashSet<String>();
		if (specialType != null) {

			switch (specialType) {
			case ALLOCATED_OBJECT:
				used_types.add(javaLangPackage() + "Object");
				break;
			case SYSTEM_ARRAY:
				used_types.add(javaLangPackage() + "SystemArray");
				used_types.add("univ");
				used_types.add("Int");
				break;
			case ALLOY_INT_ARRAY_CONTAINS:
				used_types.add(javaLangPackage() + "IntArray");
				used_types.add("Int");
				break;
			case ALLOY_OBJECT_ARRAY_CONTAINS:
				used_types.add(javaLangPackage() + "ObjectArray");
				used_types.add("Int");
				used_types.add(javaLangPackage() + "Object");
				break;	
			case INT_ARRAY_CONTAINS:
				used_types.add(javaLangPackage() + "IntArray");
				used_types.add("JavaPrimitiveIntegerValue");
				break;
			case CHAR_ARRAY_CONTAINS:
				used_types.add(javaLangPackage() + "CharArray");
				used_types.add("JavaPrimitiveIntegerValue");
				used_types.add("JavaPrimitiveCharValue");
				break;
			case LONG_ARRAY_CONTAINS:
				used_types.add(javaLangPackage() + "LongArray");
				used_types.add("JavaPrimitiveIntegerValue");
				used_types.add("JavaPrimitiveLongValue");
				break;
			case OBJECT_ARRAY_CONTAINS:
				used_types.add(javaLangPackage() + "ObjectArray");
				used_types.add("JavaPrimitiveIntegerValue");
				used_types.add(javaLangPackage() + "Object");
				break;	
			case SET_CONTAINS:
				used_types.add(javaUtilPackage() + "Set");
				used_types.add("univ");
				break;
			case MAP_ENTRIES:
				used_types.add(javaUtilPackage() + "Map");
				used_types.add("univ");
				break;
			case ALLOY_LIST_CONTAINS:
				used_types.add(javaUtilPackage() + "List");
				used_types.add("univ");
				used_types.add("Int");
				break;
			case JML_OBJECTSET_CONTAINS:
				used_types.add("org_jmlspecs_models_JMLObjectSet");
				used_types.add("univ");
				break;
			case JML_OBJECTSEQUENCE_CONTAINS:
				used_types.add("org_jmlspecs_models_JMLObjectSequence");
				used_types.add("univ");
				used_types.add("Int");
				break;
			case ITERATOR_CONTAINS:
				used_types.add(javaUtilPackage() + "Iterator");
				used_types.add("univ");
				break;
			}

		} else {
			used_types.addAll(this.dom);
			used_types.addAll(this.img);
		}
		return used_types;
	}

	/**
	 * Returns the signature whose module holds the values of this type; the relevancy analysis, the
	 * symbol tables and the bindings use it to find the module of a type:
	 * <ul>
	 * <li>for a special type, its container ({@code java_lang_IntArray}, {@code java_util_Map},
	 *     ...);</li>
	 * <li>with {@code JMLObjectSequenceToAlloySequence}, for a field whose values are sequences,
	 *     {@code org_jmlspecs_models_JMLObjectSequence};</li>
	 * <li>for a relation, {@link #singletonTo()};</li>
	 * <li>otherwise (a signature, union, set or sequence), the domain without {@code null}, joined
	 *     with {@code +} in {@code HashSet} order: {@code A} for {@code A+null}, {@code A+B} or
	 *     {@code B+A} for {@code A+B+null}, and the empty string for {@code null}.</li>
	 * </ul>
	 *
	 * @return the signature id
	 * @throws RuntimeException for {@code ALLOCATED_OBJECT} and {@code UNIV_TO_UNIV}
	 *         ("DynJML4AlloyNotImplementedYet")
	 */
	public String dpdTypeNameExtract() {
		String s = null;
		if (specialType != null) {
			switch (specialType) {

			case ALLOCATED_OBJECT:
				throw new RuntimeException("DynJML4AlloyNotImplementedYet");

			case SYSTEM_ARRAY:
				s = "" + javaLangPackage() + "SystemArray";
				break;
			case INT_ARRAY_CONTAINS:
				s = "" + javaLangPackage() + "IntArray";
				break;
			case CHAR_ARRAY_CONTAINS:
				s = "" + javaLangPackage() + "CharArray";
				break;
			case LONG_ARRAY_CONTAINS:
				s = "" + javaLangPackage() + "LongArray";
				break;				
			case OBJECT_ARRAY_CONTAINS:
				s = "" + javaLangPackage() + "ObjectArray";
				break;
			case ALLOY_INT_ARRAY_CONTAINS:
				s = "" + javaLangPackage() + "IntArray";
				break;
			case ALLOY_OBJECT_ARRAY_CONTAINS:
				s = "" + javaLangPackage() + "ObjectArray";
				break;
			case SET_CONTAINS:
				s = "" + javaUtilPackage() + "Set";
				break;
			case MAP_ENTRIES:
				s = "" + javaUtilPackage() + "Map";
				break;
			case ALLOY_LIST_CONTAINS:
				s = "" + javaUtilPackage() + "List";
				break;
			case JML_OBJECTSET_CONTAINS:
				s = "org_jmlspecs_models_JMLObjectSet";
				break;
			case JML_OBJECTSEQUENCE_CONTAINS:
				s = "org_jmlspecs_models_JMLObjectSequence";
				break;
			case ITERATOR_CONTAINS:
				s = "" + javaUtilPackage() + "Iterator";
				break;
			}
		} else if (isTernaryRelation()) {
			s = new JType(ternary_type.from(), ternary_type.to()).toString();
		} else if (JDynAlloyConfig.getInstance()
				.getJMLObjectSequenceToAlloySequence()
				&& this.imageIsSequence) {
			return "org_jmlspecs_models_JMLObjectSequence";
		} else {
			if (this.img.size() > 0) {
				s = this.singletonTo();

				// if (isSet()) {
				// throw new DynJML4AlloyNotImplementedYet();
				// } else if (isSequence())
				// throw new DynJML4AlloyNotImplementedYet();
				// else if (isBinaryRelation()) {
				// throw new DynJML4AlloyNotImplementedYet();
			} else {
				if (isNull()) {
					s = NULL_VALUE_SIGNATURE_ID;
				} else {
					Set<String> fromTemp = new HashSet<String>();
					fromTemp.addAll(this.from());
					fromTemp.remove(NULL_VALUE_SIGNATURE_ID);

					s = toString(new ArrayList<String>(fromTemp));
				}
			}
		}
		if (s == null) {
			throw new RuntimeException("DynJML4AlloyNotImplementedYet()");
		}
		return s;

	}

	/**
	 * Returns the type in DynAlloy's syntax, the text that goes into the DynAlloy output (see the
	 * class comment). Special types print fixed texts, with the {@code java_lang_} and
	 * {@code java_util_} prefixes that the configuration asks for, such as
	 * {@code java_lang_IntArray -> (Int set -> lone Int)}.
	 *
	 * @return the text
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (specialType != null) {
			switch (specialType) {
			case UNIV_TO_UNIV:
				sb.append("univ->univ");
				break;
			case ALLOCATED_OBJECT:
				sb.append("set (" + javaLangPackage() + "Object)");
				break;
			case SYSTEM_ARRAY:
				sb.append("(" + javaLangPackage() + "SystemArray)->(seq univ)");
				break;
			case SET_CONTAINS:
				sb.append("(" + javaUtilPackage() + "Set)->set univ");
				break;
			case MAP_ENTRIES:
				sb.append("(" + javaUtilPackage() + "Map)->(univ->lone(univ))");
				break;
			case ALLOY_LIST_CONTAINS:
				sb.append("(" + javaUtilPackage() + "List)->(Int -> univ)");
				break;
			case JML_OBJECTSET_CONTAINS:
				sb.append("(org_jmlspecs_models_JMLObjectSet)->set univ");
				break;
			case JML_OBJECTSEQUENCE_CONTAINS:
				sb.append("(org_jmlspecs_models_JMLObjectSequence)->(Int -> univ)");
				break;
			case ITERATOR_CONTAINS:
				sb.append("" + javaUtilPackage() + "Iterator->univ");
				break;
			case INT_ARRAY_CONTAINS:
				sb.append(javaLangPackage() + "IntArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveIntegerValue)");
				break;
			case CHAR_ARRAY_CONTAINS:
				sb.append(javaLangPackage() + "CharArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveCharValue)");
				break;
			case LONG_ARRAY_CONTAINS:
				sb.append(javaLangPackage() + "LongArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveLongValue)");
				break;
			case OBJECT_ARRAY_CONTAINS:
				sb.append(javaLangPackage() + "ObjectArray -> (JavaPrimitiveIntegerValue set -> lone (" + javaLangPackage() + "Object + null))");
				break;
			case ALLOY_INT_ARRAY_CONTAINS:
				sb.append(javaLangPackage() + "IntArray -> (Int set -> lone Int)");
				break;
			case ALLOY_OBJECT_ARRAY_CONTAINS:
				sb.append(javaLangPackage() + "ObjectArray -> (Int set -> lone (" + javaLangPackage() + "Object + null))");
				break;
			}

		} else {

			if (this.left_arrow_multiplier != null
					&& this.right_arrow_multiplier != null
					&& this.isBinaryRelation()) {
				String domStr = toString(dom);
				String imgStr = toString(img);

				String result = String.format("(%s) %s -> %s (%s)", domStr,
						this.left_arrow_multiplier.toString(),
						this.right_arrow_multiplier.toString(), imgStr);
				sb.append(result);

			} else if (this.left_arrow_multiplier !=null) {
				String domStr = toString(dom);
				String result = this.left_arrow_multiplier.toString() + "(" + domStr +")";
				sb.append(result);

			} else if (this.imageIsSequence == true) {

				String domStr = toString(dom);
				String imgStr = toString(img);

				String result = String
						.format("(%s)->(seq(%s))", domStr, imgStr);
				sb.append(result);

			} else if (isSet()) {
				sb.append("set(" + toString(dom) + ")");
			} else if (isSequence())
				sb.append("seq(" + toString(dom) + ")");
			else if (isBinaryRelation()) {
				sb.append("(" + toString(dom) + ")");
				sb.append("->");
				if (this.isFunction)
					sb.append("one");
				sb.append("(" + toString(img) + ")");
			} else if (isTernaryRelation()) {
				sb.append("(" + toString(dom) + ")");
				sb.append("->(");
				sb.append(ternary_type.toString());
				sb.append(")");
			} else if (dom != null)
			{				
				sb.append(toString(dom));
			}

		}

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dom == null) ? 0 : dom.hashCode());
		result = prime * result + ((img == null) ? 0 : img.hashCode());
		result = prime * result + (isFunction ? 1231 : 1237);
		result = prime * result + (isSequence ? 1231 : 1237);
		result = prime * result
				+ ((specialType == null) ? 0 : specialType.hashCode());
		return result;
	}

	/**
	 * Compares the domain and the image (as lists), the function and sequence flags and the special
	 * type, but not the set flag, the multipliers or the image-is-sequence flag (see the class
	 * comment).
	 *
	 * @param obj the other object
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JType other = (JType) obj;
		if (dom == null) {
			if (other.dom != null)
				return false;
		} else if (!dom.equals(other.dom))
			return false;
		if (img == null) {
			if (other.img != null)
				return false;
		} else if (!img.equals(other.img))
			return false;
		if (isFunction != other.isFunction)
			return false;
		if (isSequence != other.isSequence)
			return false;
		if (specialType == null) {
			if (other.specialType != null)
				return false;
		} else if (!specialType.equals(other.specialType))
			return false;
		return true;
	}

	private String toString(List<String> signatures) {
		StringBuffer sb = new StringBuffer();
		for (String signatureId : signatures) {
			if (sb.length() != 0)
				sb.append("+");
			sb.append(signatureId);
		}
		return sb.toString();
	}

	private List<String> sortList(Set<String> elems) {
		TreeSet<String> treeSet = new TreeSet<String>(elems);
		return new LinkedList<String>(treeSet);
	}

	private static void applyTrim(String[] array) {
		for (int i = 0; i < array.length; i++)
			array[i] = array[i].trim();
	}

	private static HashSet<String> toTrimSet(String[] array) {
		applyTrim(array);
		return new HashSet<String>(Arrays.<String> asList(array));
	}

	// Qualified name of classes related methods
	private static String javaLangPackage() {
		if (JDynAlloyConfig.getInstance().getUseQualifiedNamesForJTypes()) {
			return "java_lang_";
		} else {
			return "";
		}
	}

	private static String javaUtilPackage() {
		if (JDynAlloyConfig.getInstance().getUseQualifiedNamesForJTypes()) {
			return "java_util_";
		} else {
			return "";
		}
	}




	/******** MOVED TO JTypeFactory ****************/

	// public static JType buildReferenceSeq(String signatureId) {
	// HashSet<String> to = new HashSet<String>();
	// to.add(signatureId);
	// to.add(NULL_VALUE_SIGNATURE_ID);
	// Set<String> from = Collections.<String> singleton(INT_SIGNATURE_ID);
	// return new JType(from, to);
	// }
	//
	// public static JType buildReferenceVariable(Set<String> s) {
	// HashSet<String> sigs = new HashSet<String>(s);
	// sigs.add(NULL_VALUE_SIGNATURE_ID);
	// return new JType(sigs);
	// }
	//
	// public static JType buildReferenceVariable(String... s) {
	// HashSet<String> sigs = new HashSet<String>();
	// for (String string : s) {
	// sigs.add(string);
	// }
	// sigs.add(NULL_VALUE_SIGNATURE_ID);
	// return new JType(sigs);
	// }
	//
	// public static JType buildReferenceField(Set<String> from, Set<String> to)
	// {
	// HashSet<String> toSigs = new HashSet<String>(to);
	// toSigs.add(NULL_VALUE_SIGNATURE_ID);
	// return new JType(from, toSigs);
	// }
	//
	// public static JType buildReferenceField(String from, String to) {
	// return buildReferenceField(Collections.<String> singleton(from),
	// Collections.<String> singleton(to));
	// }
	//
	// public static JType buildReference(String signatureId) {
	// return buildReferenceVariable(Collections.<String>
	// singleton(signatureId));
	// }
	//
	// public static String getBaseType(JType alloyType) {
	// Set<String> fromWithOutAssertionFailure = new HashSet<String>();
	// fromWithOutAssertionFailure.addAll(alloyType.from());
	// fromWithOutAssertionFailure.remove("AssertionFailure");
	//
	// if (fromWithOutAssertionFailure.size() != 2)
	// throw new IllegalStateException();
	// if (!fromWithOutAssertionFailure.contains(NULL_VALUE_SIGNATURE_ID))
	// throw new IllegalStateException();
	// for (String signature : fromWithOutAssertionFailure) {
	// if (!signature.equals(NULL_VALUE_SIGNATURE_ID))
	// return signature;
	// }
	// throw new IllegalStateException();
	// }
	//
	// public static JType buildFieldSeq(String fromSignatureId, JType
	// sequenceType) {
	//
	// JType fieldSeq = new JType(Collections.singletonList(fromSignatureId),
	// new LinkedList<String>(sequenceType.from()), true, false, false, false,
	// (SpecialType) null);
	//
	// return fieldSeq;
	// }
	//
	// /******** MOVED TO JTypeHelper ****************/
	// public static boolean fromIncludesNull(JType type) {
	// return type.from().contains(NULL_VALUE_SIGNATURE_ID);
	// }
	//
	// public static boolean toIncludesNull(JType type) {
	// return type.to().contains(NULL_VALUE_SIGNATURE_ID);
	// }

}
