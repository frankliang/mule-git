package org.mule.ide.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.mule.ide.core.MuleClasspathUtils;
import org.mule.ide.core.MuleCorePlugin;
import org.mule.ide.core.distribution.IMuleBundle;
import org.mule.ide.core.distribution.IMuleDistribution;
import org.mule.ide.core.distribution.IMuleSample;
import org.mule.ide.core.exception.MuleModelException;
import org.mule.ide.core.model.IMuleConfigSet;
import org.mule.ide.core.model.IMuleConfiguration;
import org.mule.ide.core.model.IMuleModel;
import org.mule.ide.core.nature.MuleNature;
import org.mule.ide.ui.IMuleImages;
import org.mule.ide.ui.MulePlugin;
import org.w3c.dom.Document;

/**
 * Wizard for creating a new Mule project.
 */
public class MuleProjectWizard extends Wizard implements INewWizard {

	private static final String MULE_MODULE_BUILDER_NAME = "module-builder";

	private static final String MULE_TRANSPORT_TCP_NAME = "transport-tcp";

	private static final String MULE_PREFIX = "mule-";

	/** The workbench handle */
	private IWorkbench workbench;

	/** Page for creating a new project */
	private MuleWizardProjectPage projectPage;

	/** Page for setting up java project capabilities */
	private NewJavaProjectWizardPage javaPage;

	/** Static constant for config folder name */
	private static final String CONFIG_FOLDER_NAME = "conf";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		setWindowTitle("New Mule Project");
		setDefaultPageImageDescriptor(MulePlugin.getDefault().getImageRegistry().getDescriptor(
				IMuleImages.KEY_MULE_WIZARD_BANNER));
		projectPage = new MuleWizardProjectPage();
		addPage(projectPage);
		javaPage = new NewJavaProjectWizardPage(ResourcesPlugin.getWorkspace().getRoot(), projectPage);
		addPage(javaPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		try {
			// Set up the Java project according to entries on Java page.
			getContainer().run(false, true, javaPage.getRunnable());

			// Add the Mule nature.
			MuleCorePlugin.getDefault().setMuleNature(projectPage.getProjectHandle(), true);

			// Add the Mule classpath container.
			IProject project = projectPage.getProjectHandle();
			IJavaProject javaProject = JavaCore.create(project);
			IMuleSample sampleChosen = projectPage.getSelectedSample();
			IMuleSample sample = sampleChosen;
			if (sample == null) sample = createEmptyProject(projectPage.getMuleDistribution());
			addMuleLibraries(javaProject, sampleChosen);
			addFromSample(sampleChosen, javaProject);
			return true;
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof CoreException) {
				MulePlugin.getDefault().showError("Unable to create project.",
						((CoreException) e.getCause()).getStatus());
			}
		} catch (InterruptedException e) {
		} catch (CoreException e) {
			MulePlugin.getDefault().showError("Unable to create project.", e.getStatus());
		}
		return false;
	}

	/**
	 * A sample that creates the content for an empty project for a given 
	 * 
	 * @return the sample
	 */
	protected IMuleSample createEmptyProject(final IMuleDistribution distribution) {
		return new IMuleSample() {

			public String getDescription() {
				return "Empty Mule project";
			}

			public IMuleBundle[] getMuleDependencies() throws IOException {
				return new IMuleBundle[] { }; // No core, always implied
			}

			public String getName() {
				return "Empty";
			}

			public String[] getOtherDependencies() throws IOException {
				return new String[0];
			}

			public File getSourcePath() {
				return null;
			}

			public File[] getConfigFiles() {
				return null;
			}

			public File[] getSourceFolders() {
				return null;
			}
		};
	}

	/**
	 * Add the Mule libraries to the project classpath.
	 * 
	 * @param muleProject the mule project
	 * @throws JavaModelException
	 */
	protected void addMuleLibraries(IJavaProject muleProject, IMuleSample sample) throws JavaModelException, MuleModelException {
		IClasspathEntry[] initial = muleProject.getRawClasspath();
		
		HashSet moduleNames = new HashSet();
		try {
			IMuleBundle[] bundles;
			bundles = sample.getMuleDependencies();
			for (int i=0; i<bundles.length; ++i)
				moduleNames.add(bundles[i].getName().substring(MULE_PREFIX.length())); // skip MULE prefix

			// Add mandatory modules (we can't launch without these)
			moduleNames.add(MULE_TRANSPORT_TCP_NAME);
			moduleNames.add(MULE_MODULE_BUILDER_NAME);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		IClasspathEntry[] entries = new IClasspathEntry[] { MuleClasspathUtils.createMuleClasspathContainer(projectPage.getDistributionHint(), moduleNames)};
		IClasspathEntry[] result = new IClasspathEntry[initial.length + entries.length];
		System.arraycopy(initial, 0, result, 0, initial.length);
		System.arraycopy(entries, 0, result, initial.length, entries.length);
		muleProject.setRawClasspath(result, new NullProgressMonitor());
	}

	/**
	 * Add the files from the sample project.
	 * 
	 * @param sample the sample
	 * @param project the Java project
	 */
	protected void addFromSample(IMuleSample sample, IJavaProject project) {
		// Copy source files.
		try {
			IContainer sourceFolder = getSourceContainer(project);
			File[] dirs = sample.getSourceFolders();
			for (int i=0; i<dirs.length; ++i) {
				File[] subs = dirs[i].listFiles();
				for (int j = 0; j < subs.length; ++j )
					copyIntoProject(subs[j], sourceFolder);
			}
		} catch (JavaModelException e) {
			MuleCorePlugin.getDefault().logException("Unable to find a source folder.", e);
		}

		// Copy configuration files
		try {
			copyConfigSets(sample, project.getProject());
		} catch (CoreException e) {
			MuleCorePlugin.getDefault().logException("Unable to create config folder.", e);
		}
	}

	/**
	 * Get the first source folder from the project.
	 * 
	 * @param project the Java project
	 * @return the folder
	 * @throws JavaModelException
	 */
	protected IContainer getSourceContainer(IJavaProject project) throws JavaModelException {
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
				return (IContainer) roots[i].getResource();
			}
		}
		return null;
	}

	/**
	 * Copy a file or directory from a URL into a file on the project.
	 * 
	 * @param input
	 * @param project
	 */
	protected void copyIntoProject(File input, IContainer parent) {
		try {
			IPath relative = new Path(input.getName());

			// Do not copy CVS entries.
			if (relative.toString().indexOf("CVS") != -1 || relative.toString().equals(".svn")) {
				return;
			}

			// Copy directories.
			if (input.isDirectory()) {
				IFolder folder = parent.getFolder(relative);
				folder.create(true, true, new NullProgressMonitor());
				
				File[] children = input.listFiles();
				for (int i=0; i < children.length; ++i) {
					copyIntoProject(children[i], folder);
				}
			} else if (input.isFile()) {
				// Copy files.
				IFile file = parent.getFile(relative);
				file.create(new FileInputStream(input), true, new NullProgressMonitor());
			}
		} catch (IOException e) {
			MuleCorePlugin.getDefault().logException("Unable to copy sample resource.", e);
		} catch (CoreException e) {
			MuleCorePlugin.getDefault().logException("Unable to create resource.", e);
		}
	}

	private IFolder createConfigFolder(IProject project) throws CoreException {
		IFolder configFolder = project.getFolder(CONFIG_FOLDER_NAME);
		configFolder.create(true, true, new NullProgressMonitor());
		
		return configFolder;
	}
	
	/**
	 * Add config sets based on the list specified in the sample extension.
	 * 
	 * @param sample the sample
	 * @param project the project
	 * @throws MuleModelException
	 */
	protected void copyConfigSets(IMuleSample sample, IProject project) throws MuleModelException {
		MuleNature nature = MuleCorePlugin.getDefault().getMuleNature(project);
		IMuleModel model = nature.getMuleModel().createWorkingCopy();
		File[] configs = sample.getConfigFiles();
		Collection addedConfigs = new LinkedList();
		
		try {
			IFolder configFolder = createConfigFolder(project);
			if (configs == null) {
				String emptyConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
						"\r\n" + 
						"<!DOCTYPE mule-configuration PUBLIC \"-//MuleSource //DTD mule-configuration XML V1.0//EN\"\r\n" + 
						"                                \"http://mule.mulesource.org/dtds/mule-configuration.dtd\">\r\n" + 
						"\r\n" + 
						"<!--  This is a blank configuration file for the " + project.getName() + " project -->\r\n" + 
						"\r\n" + 
						"<mule-configuration id=\"" + project.getName() + "-config\" version=\"1.0\">\r\n" + 
						"\r\n" + 
						"    <description>\r\n" +
						"        Configuration for the the \"" + project.getName() + "\" project" +
						"    </description>\r\n" + 
						"\r\n" + 
						"</mule-configuration";
				// Make an empty one...
				IFile blankConfigFile = configFolder.getFile("mule-config.xml");
					blankConfigFile.create(new ByteArrayInputStream(emptyConfig.getBytes("UTF-8")), true, new NullProgressMonitor());
			addedConfigs.add(blankConfigFile);
			} else {
				for (int i = 0; i < configs.length; i++) {
					File configFile = configs[i];
					IFile newConfigFile = configFolder.getFile(configFile.getName());
					newConfigFile.create(new FileInputStream(configFile), true, new NullProgressMonitor());
	
					if (configFile.getName().endsWith(".xml") && smellsLikeMuleConfigFile(configFile)) {
						addedConfigs.add(newConfigFile);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Iterator configIt = addedConfigs.iterator(); configIt.hasNext();) {
			IFile file = (IFile)configIt.next();
			IMuleConfiguration newConfig = model.createNewMuleConfiguration(file.getName(), file
					.getProjectRelativePath().toString());
			model.addMuleConfiguration(newConfig);
			IMuleConfigSet newSet = model.createNewMuleConfigSet(file.getName());
			newSet.addConfiguration(newConfig);
			model.addMuleConfigSet(newSet);
		}
		model.save();
	}

	private boolean smellsLikeMuleConfigFile(File configFile) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			Document doc = dbf.newDocumentBuilder().parse(configFile);
			return "mule-configuration".equals(doc.getDocumentElement().getLocalName());
		} catch (Throwable t) {
			// It's OK to ignore any old exception here
		}
		return false; // It's not a Mule Config file, then
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
	}

	/**
	 * Get the workbench that hosts the wizard.
	 * 
	 * @return the workbench
	 */
	protected IWorkbench getWorkbench() {
		return workbench;
	}

	/**
	 * Set the workbench that hosts the wizard.
	 * 
	 * @param workbench the workbench
	 */
	protected void setWorkbench(IWorkbench workbench) {
		this.workbench = workbench;
	}
}