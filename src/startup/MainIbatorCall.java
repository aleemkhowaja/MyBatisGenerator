package startup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

public class MainIbatorCall
{
	public static void main(String[] args) throws Exception
	{

		List<String> warnings1 = new ArrayList<String>();
		boolean overwrite1 = true;
		File configFile1 = new File("config/ibatorConfig.xml");
		ConfigurationParser cp1 = new ConfigurationParser(System.getProperties(), warnings1);
		Configuration config1 = cp1.parseConfiguration(configFile1);

		DefaultShellCallback callback1 = new DefaultShellCallback(overwrite1);
		MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config1, callback1, warnings1);
		myBatisGenerator.generate(null);

	}
}
