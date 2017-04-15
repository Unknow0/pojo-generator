package unknow.pojo;

import java.io.*;
import java.nio.file.*;

import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name="generate", defaultPhase=LifecyclePhase.GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo
	{
	@Parameter(property="out", required=true)
	private Path out;

	@Parameter(property="files", required=true)
	private File[] files;

	public void setOut(String s)
		{
		out=Paths.get(s);
		}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
		{
		try
			{
			Generator g=new Generator(out);
			for(File f:files)
				g.generate(f, false);
			g.buildArchetype();
			}
		catch (Exception e)
			{
			throw new MojoFailureException("", e);
			}
		}
	}
