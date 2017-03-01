package unknow.pojo;

import java.io.*;
import java.nio.file.*;

public class Main
	{
	public static void main(String[] arg) throws Exception
		{
		Generator gen=new Generator(Paths.get("/tmp/out"));

		gen.generate(new File("/tmp/test.xml"));
		}
	}
