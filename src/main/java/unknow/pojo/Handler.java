package unknow.pojo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class Handler extends DefaultHandler
	{
	private Path root;
	private Path out;
	private String pack;

	private String cpack;
	private Set<String> imports=new HashSet<String>();
	private List<Property> prop=new ArrayList<Property>();
	private String className;
	private Calendar start=Calendar.getInstance();
	private Map<String,List<Archetype>> arch=new HashMap<String,List<Archetype>>();
	private Archetype curArch=null;

	public Handler(Path out)
		{
		this.root=out;
		this.out=out;
		}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
		if("generator".equals(localName))
			{
			pack=attributes.getValue("package");
			out=root.resolve(pack.replace('.', '/'));
			try
				{
				Files.createDirectories(out);
				}
			catch (IOException e)
				{
				throw new SAXException(e);
				}
			}
		if("component".equals(localName))
			{
			if(curArch==null)
				{
				className=attributes.getValue("name");
				int i=className.lastIndexOf('.');
				if(i>0)
					{
					cpack=className.substring(0, i);
					className=className.substring(i+1);
					}
				else
					cpack=pack;
				}
			else
				curArch.classes.add(attributes.getValue("name"));

			}
		else if("property".equals(localName))
			prop.add(new Property(attributes));
		else if("archetype".equals(localName))
			{
			curArch=new Archetype(attributes.getValue("name"));
			String v=attributes.getValue("class");
			if(v==null)
				v="Archetype";
			List<Archetype> a=arch.get(v);
			if(a==null)
				{
				a=new ArrayList<Archetype>();
				arch.put(v, a);
				}
			a.add(curArch);
			}
		}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
		{
		if("archetype".equals(localName))
			curArch=null;
		if(!"component".equals(localName)||curArch!=null)
			return;
		try
			{
			Path j=out.resolve(className+".java");
			try (PrintStream w=new PrintStream(j.toFile()))
				{
				if(cpack!=null)
					{
					w.print("package ");
					w.print(cpack);
					w.println(';');
					w.println();
					}
				w.println("import javax.annotation.Generated;");
				w.println("import com.artemis.PooledComponent;");
				for(String i:imports)
					{
					w.print("import ");
					w.print(i);
					w.println(';');
					}
				w.println();
				w.format("@Generated(value=\"%s\", date=\"%tFT%tT%tz\")", Generator.class, start, start, start);
				w.println();
				w.print("public class ");
				w.print(className);
				w.println(" extends PooledComponent {");

				for(Property p:prop)
					{
					w.print("	public ");
					w.print(p.type);
					w.print(' ');
					w.print(p.name);
					if(p.value!=null)
						{
						w.print('=');
						w.print(p.value);
						}
					w.println(';');
					}
				w.println();
				w.println("	public void reset() {");
				for(Property p:prop)
					{
					if(p.value!=null)
						{
						w.print("\t\t");
						w.print(p.name);
						w.print('=');
						w.print(p.value);
						w.println(';');
						}
					}
				w.println("	}");
				w.print('}');
				}
			}
		catch (IOException e)
			{
			throw new SAXException(e);
			}
		imports.clear();
		prop.clear();
		className=null;
		}

	public void writeArchetype() throws IOException
		{
		for(Map.Entry<String,List<Archetype>> e:arch.entrySet())
			{
			String c=e.getKey();
			Path j;
			String p=pack;
			int id=c.lastIndexOf('.');
			if(id>0)
				{
				j=root.resolve(c.replace(".", "/")+".java");
				p=c.substring(0, id);
				c=c.substring(id);
				}
			else
				j=out.resolve(c+".java");
			Files.createDirectories(out);
			try (PrintStream w=new PrintStream(j.toFile()))
				{
				if(p!=null)
					{
					w.print("package ");
					w.print(p);
					w.println(";");
					w.println();
					}

				w.println("import com.artemis.ArchetypeBuilder;");
				w.println("import com.artemis.World;");
				w.println();
				w.print("public final class ");
				w.print(c);
				w.println(" {");
				for(Archetype a:e.getValue())
					{
					w.print("	private static final ArchetypeBuilder ");
					w.print(a.name);
					w.println("=new ArchetypeBuilder();");
					w.println("	static {");
					w.print("		");
					w.print(a.name);
					w.print(".add(");
					for(int i=0; i<a.classes.size(); i++)
						{
						if(i>0)
							w.print(',');
						w.print(a.classes.get(i));
						w.print(".class");
						}
					w.println(");");
					w.println("	}");
					w.print("	public static Archetype build");
					w.print(Character.toUpperCase(a.name.charAt(0)));
					w.print(a.name.substring(1));
					w.println("(World world) {");
					w.print("		return ");
					w.print(a.name);
					w.println(".build(world);");
					w.println("	}");
					}
				}
			}
		}

	private class Property
		{
		public String name;
		public String type;
		public String value;

		public Property(Attributes attr)
			{
			name=attr.getValue("name");
			type=attr.getValue("type");
			value=attr.getValue("value");
			if(value!=null&&(type.equals("java.lang.String")||type.equals("String")))
				value='"'+value.replace("\"", "\\\"")+'"';
			int i=type.lastIndexOf('.');
			if(i>0)
				{
				if(!"java.lang".equals(type.substring(0, i)))
					imports.add(type);
				type=type.substring(i+1);
				}
			}
		}

	private static class Archetype
		{
		public String name;
		public List<String> classes=new ArrayList<String>();

		public Archetype(String name)
			{
			this.name=name;
			}
		}
	}
