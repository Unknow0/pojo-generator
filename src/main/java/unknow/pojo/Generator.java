package unknow.pojo;

import java.io.*;
import java.nio.file.*;

import javax.xml.parsers.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.xml.sax.*;

/**
 * Build Actor from xml
 */
public class Generator
	{
	private static final Schema schema;
	static
		{
		SchemaFactory factory=SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		try
			{
			schema=factory.newSchema(new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("pojo-generator.xsd")));
			}
		catch (SAXException e)
			{
			throw new RuntimeException(e);
			}
		}

	private Handler handler;

	public Generator(Path out)
		{
		this.handler=new Handler(out);
		}

	public void generate(String resource) throws SAXException, IOException, ParserConfigurationException
		{
		generate(resource, true);
		}

	public void generate(String resource, boolean buildArchetype) throws SAXException, IOException, ParserConfigurationException
		{
		try (InputStream is=this.getClass().getClassLoader().getResourceAsStream(resource))
			{
			generate(new InputSource(is), buildArchetype);
			}
		}

	public void generate(File f) throws SAXException, IOException, ParserConfigurationException
		{
		generate(f, true);
		}

	public void generate(File f, boolean buildArchetype) throws SAXException, IOException, ParserConfigurationException
		{
		try (FileInputStream is=new FileInputStream(f))
			{
			generate(new InputSource(is), buildArchetype);
			}
		}

	/**
	 * parse and construct the tree
	 */
	public void generate(InputSource source, boolean buildArchetype) throws ParserConfigurationException, SAXException, IOException
		{
		SAXParserFactory factory=SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setSchema(schema);
		SAXParser parser=factory.newSAXParser();
		parser.parse(source, handler);
		if(buildArchetype)
			buildArchetype();
		}

	public void buildArchetype() throws IOException
		{
		handler.writeArchetype();
		}
	}
