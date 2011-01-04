/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.resource.impl;

import static com.google.common.collect.Iterables.*;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.junit.AbstractXtextTests;
import org.eclipse.xtext.linking.LangATestLanguageStandaloneSetup;
import org.eclipse.xtext.linking.langATestLanguage.LangATestLanguagePackage;
import org.eclipse.xtext.linking.langATestLanguage.Main;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.SimpleAttributeResolver;
import org.eclipse.xtext.util.StringInputStream;

import com.google.common.collect.Lists;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class DefaultReferenceDescriptionTest extends AbstractXtextTests {
	
	public void testgetReferenceDescriptions() throws Exception {
		with(new LangATestLanguageStandaloneSetup());
		XtextResource targetResource = getResource("type C", "bar.langatestlanguage");
		EObject typeC = targetResource.getContents().get(0).eContents().get(0);
		XtextResource resource = (XtextResource) targetResource.getResourceSet().createResource(URI.createURI("foo.langatestlanguage"));
		resource.load(new StringInputStream("type A extends C type B extends A"), null);
		EcoreUtil2.resolveLazyCrossReferences(resource, CancelIndicator.NullImpl);
		IResourceDescription resDesc = resource.getResourceServiceProvider().getResourceDescriptionManager().getResourceDescription(resource);
		Iterable<IReferenceDescription> descriptions = resDesc.getReferenceDescriptions();
		Collection<IReferenceDescription> collection = Lists.newArrayList(descriptions);
		assertEquals(1,collection.size());
		IReferenceDescription refDesc = descriptions.iterator().next();
		Main m = (Main) resource.getParseResult().getRootASTElement();
		assertEquals(m.getTypes().get(0),resource.getResourceSet().getEObject(refDesc.getSourceEObjectUri(),false));
		assertEquals(typeC, resource.getResourceSet().getEObject(refDesc.getTargetEObjectUri(),false));
		assertEquals(-1,refDesc.getIndexInList());
		assertEquals(LangATestLanguagePackage.Literals.TYPE__EXTENDS,refDesc.getEReference());
	}
	
	public void testgetReferenceDescriptionForMultiValue() throws Exception {
		with(new LangATestLanguageStandaloneSetup());
		XtextResource targetResource = getResource("type C type D", "bar.langatestlanguage");
		EObject typeC = targetResource.getContents().get(0).eContents().get(0);
		EObject typeD = targetResource.getContents().get(0).eContents().get(1);
		XtextResource resource = (XtextResource) targetResource.getResourceSet().createResource(URI.createURI("foo.langatestlanguage"));
		resource.load(new StringInputStream("type A implements B,C,D type B"), null);
		EcoreUtil2.resolveLazyCrossReferences(resource, CancelIndicator.NullImpl);
		IResourceDescription resDesc = resource.getResourceServiceProvider().getResourceDescriptionManager().getResourceDescription(resource);
		Iterable<IReferenceDescription> descriptions = resDesc.getReferenceDescriptions();
		Collection<IReferenceDescription> collection = Lists.newArrayList(descriptions);
		assertEquals(2,collection.size());
		Iterator<IReferenceDescription> iterator = descriptions.iterator();
		IReferenceDescription refDesc1 = iterator.next();
		IReferenceDescription refDesc2 = iterator.next();
		Main m = (Main) resource.getParseResult().getRootASTElement();
		assertEquals(m.getTypes().get(0),resource.getResourceSet().getEObject(refDesc1.getSourceEObjectUri(),false));
		assertEquals(typeC,resource.getResourceSet().getEObject(refDesc1.getTargetEObjectUri(),false));
		assertEquals(1,refDesc1.getIndexInList());
		assertEquals(LangATestLanguagePackage.Literals.TYPE__IMPLEMENTS,refDesc1.getEReference());
		assertEquals(m.getTypes().get(0),resource.getResourceSet().getEObject(refDesc2.getSourceEObjectUri(),false));
		assertEquals(typeD,resource.getResourceSet().getEObject(refDesc2.getTargetEObjectUri(),false));
		assertEquals(2,refDesc2.getIndexInList());
		assertEquals(LangATestLanguagePackage.Literals.TYPE__IMPLEMENTS,refDesc2.getEReference());
	}
	
	public void testSpecialReferences() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("test");
		ePackage.setNsPrefix("test");
		ePackage.setNsURI("test");

		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName("Test");
		eClass.getESuperTypes().add(EcorePackage.Literals.EPACKAGE);
		ePackage.getEClassifiers().add(eClass);

		EAttribute nameAttribute = EcoreFactory.eINSTANCE.createEAttribute();
		nameAttribute.setName("name");
		nameAttribute.setEType(EcorePackage.Literals.ESTRING);
		eClass.getEStructuralFeatures().add(nameAttribute);

		EReference eReference1 = EcoreFactory.eINSTANCE.createEReference();
		eReference1.setContainment(false);
		eReference1.setName("onlyExportedRef");
		eReference1.setEType(EcorePackage.Literals.EPACKAGE);
		eClass.getEStructuralFeatures().add(eReference1);

		EReference eReference2 = EcoreFactory.eINSTANCE.createEReference();
		eReference2.setContainment(true);
		eReference2.setName("containmentRef");
		eReference2.setEType(EcorePackage.Literals.EPACKAGE);
		eClass.getEStructuralFeatures().add(eReference2);

		EReference eReference3 = EcoreFactory.eINSTANCE.createEReference();
		eReference3.setContainment(false);
		eReference3.setTransient(true);
		eReference3.setName("transientRef");
		eReference3.setEType(EcorePackage.Literals.EPACKAGE);
		eClass.getEStructuralFeatures().add(eReference3);

		EReference eReference4 = EcoreFactory.eINSTANCE.createEReference();
		eReference4.setContainment(false);
		eReference4.setVolatile(true);
		eReference4.setName("volatileRef");
		eReference4.setEType(EcorePackage.Literals.EPACKAGE);
		eClass.getEStructuralFeatures().add(eReference4);

		EReference eReference5 = EcoreFactory.eINSTANCE.createEReference();
		eReference5.setContainment(false);
		eReference5.setDerived(true);
		eReference5.setName("derivedRef");
		eReference5.setEType(EcorePackage.Literals.EPACKAGE);
		eClass.getEStructuralFeatures().add(eReference5);

		EObject object = ePackage.getEFactoryInstance().create(eClass);
		object.eSet(nameAttribute, "testname");
		object.eSet(eReference1, EcorePackage.Literals.EPACKAGE);
		object.eSet(eReference2, ePackage.getEFactoryInstance().create(eClass));
		object.eSet(eReference3, EcorePackage.Literals.EPACKAGE);
		object.eSet(eReference4, EcorePackage.Literals.EPACKAGE);
		object.eSet(eReference5, EcorePackage.Literals.EPACKAGE);

		Resource testResource = new XMIResourceImpl(URI.createFileURI("test.ecore"));
		testResource.getContents().add(object);
		IResourceDescription resourceDescription = createResourceDescription(testResource);
		assertEquals("Only one external reference expected", 1, size(resourceDescription.getReferenceDescriptions()));
		IReferenceDescription referenceDescription = resourceDescription.getReferenceDescriptions().iterator().next();
		assertEquals(-1, referenceDescription.getIndexInList());
		assertEquals(EcoreUtil.getURI(object), referenceDescription.getSourceEObjectUri());
		assertEquals(eReference1, referenceDescription.getEReference());
		assertEquals(EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE), referenceDescription.getTargetEObjectUri());
		assertEquals(EcoreUtil.getURI(object), referenceDescription.getContainerEObjectURI());
	}
	
	protected IResourceDescription createResourceDescription(Resource testResource) {
		DefaultResourceDescriptionStrategy strategy = new DefaultResourceDescriptionStrategy();
		strategy.setQualifiedNameProvider(new IQualifiedNameProvider.AbstractImpl() {
			public QualifiedName getFullyQualifiedName(EObject obj) {
				String name = SimpleAttributeResolver.NAME_RESOLVER.apply(obj);
				return (name != null) ? QualifiedName.create(name) : null;
			}
		});
		IResourceDescription resourceDescription = new DefaultResourceDescription(testResource, strategy);
		return resourceDescription;
	}


}
