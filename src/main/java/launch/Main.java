package launch;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.EmptyResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanFilter;

public class Main {

	private static File getRootFolder() {
		try {
			File root;
			String runningJarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
					.replaceAll("\\\\", "/");
			System.out.println("runningJarPath: "+runningJarPath);
			
			int lastIndexOf = runningJarPath.lastIndexOf("/target/");
			if (lastIndexOf < 0) {
				root = new File("");
			} else {
				root = new File(runningJarPath.substring(0, lastIndexOf));
			}
			System.out.println("application resolved root folder: " + root.getAbsolutePath());
			return root;
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void main(String[] args) throws Exception {

		File root = getRootFolder();
		System.out.println("project root: " + root);

		System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
		Tomcat tomcat = new Tomcat();

		Path tempPath = Files.createTempDirectory("tomcat-base-dir");
		tomcat.setBaseDir(tempPath.toString());
		Files.createDirectories(Paths.get(tempPath+"/webapps"));
		System.out.println("BaseDir: " + tempPath);

		//The port that we should run on can be set into an environment variable
		//Look for that variable and default to 8080 if it isn't there.
		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8080";
		}
		tomcat.setPort(Integer.valueOf(webPort));
		
		File webContentFolder = new File(root.getAbsolutePath(), "src/main/webapp/");
		if (!webContentFolder.exists()) {
			webContentFolder = Files.createTempDirectory("default-doc-base").toFile();
		}
		
		System.out.println("/app webContentFolder: "+webContentFolder);
		
		StandardContext ctx = (StandardContext) tomcat.addWebapp("/app", webContentFolder.getAbsolutePath());
		//Set execution independent of current thread context classloader (compatibility with exec:java mojo)
		ctx.setParentClassLoader(Main.class.getClassLoader());

		System.out.println("configuring /app with basedir: " + webContentFolder.getAbsolutePath());

		// Declare an alternative location for your "WEB-INF/classes" dir
		// Servlet 3.0 annotation will work
		File additionWebInfClassesFolder = new File(root.getAbsolutePath(), "target/classes");
		WebResourceRoot resources = new StandardRoot(ctx);

		WebResourceSet resourceSet;
		if (additionWebInfClassesFolder.exists()) {
			resourceSet = new DirResourceSet(resources, "/WEB-INF/classes",
					additionWebInfClassesFolder.getAbsolutePath(), "/");
			System.out.println(
					"loading WEB-INF resources from as '" + additionWebInfClassesFolder.getAbsolutePath() + "'");
		} else {
			resourceSet = new EmptyResourceSet(resources);
		}
		resources.addPreResources(resourceSet);
		ctx.setResources(resources);

		tomcat.start();
		System.out.println("Tomcat started on " + tomcat.getHost() + ":" + tomcat.getConnector());

		System.out.println("Sleep 20 seconds before deploying /app1");
		System.out.println("while true; do curl http://localhost:8080/app1/; done");
		Thread.sleep(1000L * 20);
				
		Context appContext = tomcat.addWebapp("/app1", "/Users/xinsfang/demo/java/tomcat/staging/apache-tomcat-8.5.32/webapps/logServlet.war");
		System.out.println("Deployed "+appContext.getBaseName());

		tomcat.getServer().await();
	}
}
